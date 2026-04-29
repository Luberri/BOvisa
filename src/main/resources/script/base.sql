\c postgres;
DROP DATABASE IF EXISTS gestion_visa;
CREATE DATABASE gestion_visa;
\c gestion_visa;
-- =========================
-- TYPES ENUM
-- =========================

CREATE TYPE type_demande_enum AS ENUM (
    'NOUVEAU_TITRE',
    'TRANSFERT_VISA',
    'DUPLICATA_RESIDENT'
);

CREATE TYPE statut_demande_enum AS ENUM (
    'DOSSIER_CREE',
    'DOSSIER_SCANNE',
    'VISA_APPROUVE',
    'VISA_REFUSE'
);

CREATE TYPE categorie_demande_enum AS ENUM (
    'TRAVAILLEUR',
    'INVESTISSEUR'
);

CREATE TYPE nature_visa_enum AS ENUM (
    'TRANSFORMABLE',
    'LONG_SEJOUR'
);

CREATE TYPE statut_piece_enum AS ENUM (
    'OBLIGATOIRE',
    'OPTIONNELLE'
);

-- =========================
-- TABLES DE REFERENCE
-- =========================

CREATE TABLE situation_de_famille (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE nationalite (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE
);

-- =========================
-- DEMANDEUR
-- =========================

CREATE TABLE demandeur (
    id SERIAL PRIMARY KEY,

    nom VARCHAR(100) NOT NULL,
    prenoms VARCHAR(150),

    nom_jeune_fille VARCHAR(100),

    date_naissance DATE,
    lieu_naissance VARCHAR(150),

    situation_famille_id INT NOT NULL REFERENCES situation_de_famille(id),
    nationalite_id INT NOT NULL REFERENCES nationalite(id),

    profession VARCHAR(150),

    telephone VARCHAR(50),
    email VARCHAR(150),

    adresse TEXT NOT NULL
);

-- =========================
-- PASSEPORT (1 demandeur -> n passeports)
-- =========================

CREATE TABLE passeport (
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL REFERENCES demandeur(id) ON DELETE CASCADE,

    numero_passeport VARCHAR(100) NOT NULL UNIQUE,
    date_delivrance_passeport DATE,
    date_expiration_passeport DATE,

    est_actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- VISA (1 demandeur -> n visas transformables)
-- =========================

-- TABLE: visa_transformable (visas sources, transformables)

CREATE TABLE visa_transformable (
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL REFERENCES demandeur(id) ON DELETE CASCADE,
    id_passeport INT REFERENCES passeport(id) ON DELETE SET NULL,

    reference_visa VARCHAR(100) NOT NULL,
    numero_visa VARCHAR(100) NOT NULL UNIQUE,
    nature_visa nature_visa_enum NOT NULL DEFAULT 'TRANSFORMABLE',
    categorie_demande categorie_demande_enum,

    date_entree_mada DATE,
    lieu_entree_mada VARCHAR(150),
    date_expiration_visa DATE NOT NULL,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABLE: visa (visas finaux / générés)

CREATE TABLE visa (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL UNIQUE,
    id_demandeur INT NOT NULL REFERENCES demandeur(id) ON DELETE CASCADE,
    id_passeport INT REFERENCES passeport(id) ON DELETE SET NULL,

    reference_visa VARCHAR(100) NOT NULL,
    numero_visa VARCHAR(100) NOT NULL UNIQUE,
    nature_visa nature_visa_enum NOT NULL DEFAULT 'LONG_SEJOUR',
    categorie_demande categorie_demande_enum,

    date_entree_mada DATE,
    lieu_entree_mada VARCHAR(150),
    date_expiration_visa DATE NOT NULL,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- DEMANDE
-- =========================

CREATE TABLE demande (
    id SERIAL PRIMARY KEY,

    id_demandeur INT NOT NULL REFERENCES demandeur(id) ON DELETE CASCADE,
    id_visa_transformable INT NOT NULL REFERENCES visa_transformable(id) ON DELETE RESTRICT,

    type_demande type_demande_enum NOT NULL,
    categorie_demande categorie_demande_enum NOT NULL,

    -- seulement pour certains types
    avec_donnees_anterieures BOOLEAN DEFAULT NULL,

    statut statut_demande_enum DEFAULT 'DOSSIER_CREE',

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_avec_donnees_anterieures
    CHECK (
        (type_demande = 'NOUVEAU_TITRE' AND avec_donnees_anterieures IS NULL)
        OR
        (type_demande IN ('TRANSFERT_VISA', 'DUPLICATA_RESIDENT') AND avec_donnees_anterieures IS NOT NULL)
    )
);

CREATE OR REPLACE FUNCTION verifier_demande_visa_transformable()
RETURNS TRIGGER AS $$
BEGIN
        IF NOT EXISTS (
                SELECT 1
                FROM visa_transformable v
                WHERE v.id = NEW.id_visa_transformable
                    AND v.id_demandeur = NEW.id_demandeur
                    AND v.nature_visa = 'TRANSFORMABLE'
        ) THEN
        RAISE EXCEPTION 'Le visa de la demande doit etre TRANSFORMABLE et appartenir au meme demandeur';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_verifier_demande_visa_transformable
BEFORE INSERT OR UPDATE ON demande
FOR EACH ROW
EXECUTE FUNCTION verifier_demande_visa_transformable();

ALTER TABLE visa
    ADD CONSTRAINT fk_visa_demande
    FOREIGN KEY (id_demande) REFERENCES demande(id) ON DELETE CASCADE;

-- =========================
-- CARTE RESIDENT
-- =========================

CREATE TABLE carte_resident (
    id SERIAL PRIMARY KEY,

    id_demandeur INT NOT NULL REFERENCES demandeur(id) ON DELETE CASCADE,
    id_demande INT UNIQUE REFERENCES demande(id) ON DELETE SET NULL,

    numero_carte_resident VARCHAR(100) UNIQUE,
    date_delivrance DATE,
    date_expiration DATE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- PIECES JUSTIFICATIVES
-- =========================

CREATE TABLE piece_justificative (
    id SERIAL PRIMARY KEY,

    libelle TEXT NOT NULL,

    -- NULL = commune
    categorie_demande categorie_demande_enum,

    statut statut_piece_enum NOT NULL
);

-- =========================
-- LIAISON DEMANDE <-> PIECES
-- =========================

CREATE TABLE demande_piece (
    id SERIAL PRIMARY KEY,

    demande_id INT REFERENCES demande(id) ON DELETE CASCADE,
    piece_id INT REFERENCES piece_justificative(id) ON DELETE CASCADE,

    coche BOOLEAN DEFAULT FALSE,

    UNIQUE (demande_id, piece_id)
);

CREATE INDEX idx_passeport_demandeur ON passeport(id_demandeur);
CREATE INDEX idx_visa_demandeur ON visa_transformable(id_demandeur);
CREATE INDEX idx_visa_demande ON visa(id_demande);
CREATE INDEX idx_demande_demandeur ON demande(id_demandeur);
CREATE INDEX idx_demande_visa_transformable ON demande(id_visa_transformable);

-- =========================
-- DONNEES INITIALES
-- =========================

INSERT INTO situation_de_famille (libelle) VALUES
('Célibataire'),
('Marié(e)'),
('Divorcé(e)'),
('Veuf(ve)');

INSERT INTO nationalite (libelle) VALUES
('Malgache'),
('Française'),
('Comorienne'),
('Chinoise');

-- =========================
-- PIECES COMMUNES
-- =========================

INSERT INTO piece_justificative (libelle, categorie_demande, statut) VALUES
('02 photos d''identité', NULL, 'OBLIGATOIRE'),
('Notice de renseignement', NULL, 'OBLIGATOIRE'),
('Demande adressée au Ministère (email + téléphone)', NULL, 'OBLIGATOIRE'),
('Photocopie certifiée du visa en cours de validité', NULL, 'OBLIGATOIRE'),
('Photocopie première page passeport', NULL, 'OBLIGATOIRE'),
('Photocopie carte résident valide', NULL, 'OBLIGATOIRE'),
('Certificat de résidence à Madagascar', NULL, 'OBLIGATOIRE'),
('Extrait de casier judiciaire < 3 mois', NULL, 'OBLIGATOIRE');

-- =========================
-- PIECES INVESTISSEUR
-- =========================

INSERT INTO piece_justificative (libelle, categorie_demande, statut) VALUES
('Statut de la société', 'INVESTISSEUR', 'OBLIGATOIRE'),
('Inscription registre de commerce', 'INVESTISSEUR', 'OBLIGATOIRE'),
('Carte fiscale', 'INVESTISSEUR', 'OBLIGATOIRE');

-- =========================
-- PIECES TRAVAILLEUR
-- =========================

INSERT INTO piece_justificative (libelle, categorie_demande, statut) VALUES
('Autorisation d''emploi délivrée par le ministère', 'TRAVAILLEUR', 'OBLIGATOIRE'),
('Attestation d''emploi (original)', 'TRAVAILLEUR', 'OBLIGATOIRE');