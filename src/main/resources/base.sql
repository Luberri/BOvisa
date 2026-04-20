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

CREATE TYPE type_visa_enum AS ENUM (
    'TRAVAILLEUR',
    'INVESTISSEUR'
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

    adresse TEXT NOT NULL,

    -- PASSEPORT
    numero_passeport VARCHAR(100) NOT NULL,
    date_delivrance_passeport DATE,
    date_expiration_passeport DATE
);

-- =========================
-- DEMANDE
-- =========================

CREATE TABLE demande (
    id SERIAL PRIMARY KEY,

    demandeur_id INT NOT NULL REFERENCES demandeur(id) ON DELETE CASCADE,

    type_demande type_demande_enum NOT NULL,
    type_visa type_visa_enum NOT NULL,

    -- seulement pour certains types
    avec_donnees_anterieures BOOLEAN DEFAULT NULL,

    -- INFOS VISA TRANSFORMABLE
    reference_visa VARCHAR(100),
    numero_visa VARCHAR(100),
    date_entree_mada DATE,
    lieu_entree_mada VARCHAR(150),
    date_expiration_visa DATE,

    statut statut_demande_enum DEFAULT 'DOSSIER_CREE',

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- PIECES JUSTIFICATIVES
-- =========================

CREATE TABLE piece_justificative (
    id SERIAL PRIMARY KEY,

    libelle TEXT NOT NULL,

    -- NULL = commune
    type_visa type_visa_enum,

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

INSERT INTO piece_justificative (libelle, type_visa, statut) VALUES
('02 photos d’identité', NULL, 'OBLIGATOIRE'),
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

INSERT INTO piece_justificative (libelle, type_visa, statut) VALUES
('Statut de la société', 'INVESTISSEUR', 'OBLIGATOIRE'),
('Inscription registre de commerce', 'INVESTISSEUR', 'OBLIGATOIRE'),
('Carte fiscale', 'INVESTISSEUR', 'OBLIGATOIRE');

-- =========================
-- PIECES TRAVAILLEUR
-- =========================

INSERT INTO piece_justificative (libelle, type_visa, statut) VALUES
('Autorisation d’emploi délivrée par le ministère', 'TRAVAILLEUR', 'OBLIGATOIRE'),
('Attestation d’emploi (original)', 'TRAVAILLEUR', 'OBLIGATOIRE');