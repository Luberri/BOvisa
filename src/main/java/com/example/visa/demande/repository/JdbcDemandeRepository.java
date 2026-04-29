package com.example.visa.demande.repository;

import com.example.visa.demande.model.DemandeEditData;
import com.example.visa.demande.model.DemandeForm;
import com.example.visa.demande.model.DemandeListItem;
import com.example.visa.demande.model.OptionItem;
import com.example.visa.demande.model.PieceJustificativeItem;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDemandeRepository implements DemandeRepository {

    private static final String CATEGORIE_TRAVAILLEUR = "TRAVAILLEUR";
    private static final String CATEGORIE_INVESTISSEUR = "INVESTISSEUR";
    private static final String TYPE_NOUVEAU_TITRE = "NOUVEAU_TITRE";
    private static final String TYPE_DUPLICATA_RESIDENT = "DUPLICATA_RESIDENT";
    private static final String STATUT_DOSSIER_CREE = "DOSSIER_CREE";
    private static final String STATUT_VISA_APPROUVE = "VISA_APPROUVE";
    private static final String PREFIX_NUMERO_VISA = "VISA";
    private static final String PREFIX_NUMERO_CARTE = "CR";

    private final JdbcTemplate jdbcTemplate;

    public JdbcDemandeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<OptionItem> findSituationsFamille() {
        return jdbcTemplate.query(
                "select id, libelle from situation_de_famille order by libelle",
                (rs, rowNum) -> new OptionItem(rs.getInt("id"), rs.getString("libelle"))
        );
    }

    @Override
    public List<OptionItem> findNationalites() {
        return jdbcTemplate.query(
                "select id, libelle from nationalite order by libelle",
                (rs, rowNum) -> new OptionItem(rs.getInt("id"), rs.getString("libelle"))
        );
    }

    @Override
    public List<PieceJustificativeItem> findPiecesCommunes() {
        return jdbcTemplate.query(
                """
                select id, libelle, categorie_demande
                from piece_justificative
                where categorie_demande is null
                order by id
                """,
                (rs, rowNum) -> new PieceJustificativeItem(
                        rs.getInt("id"),
                        rs.getString("libelle"),
                        rs.getString("categorie_demande")
                )
        );
    }

    @Override
    public List<PieceJustificativeItem> findPiecesByCategorie(String categorieDemande) {
        return jdbcTemplate.query(
                """
                select id, libelle, categorie_demande
                from piece_justificative
                where categorie_demande = cast(? as categorie_demande_enum)
                order by id
                """,
                (rs, rowNum) -> new PieceJustificativeItem(
                        rs.getInt("id"),
                        rs.getString("libelle"),
                        rs.getString("categorie_demande")
                ),
                categorieDemande
        );
    }

    @Override
    public List<DemandeListItem> findDemandes() {
        return jdbcTemplate.query(
                """
                select d.id,
                       concat(dm.nom, ' ', coalesce(dm.prenoms, '')) as nom_complet,
                       d.categorie_demande,
                       d.type_demande,
                  coalesce(vf.numero_visa, vt.numero_visa) as numero_visa,
                       cr.numero_carte_resident,
                       d.statut,
                       d.date_creation
                from demande d
                join demandeur dm on dm.id = d.id_demandeur
              join visa_transformable vt on vt.id = d.id_visa_transformable
              left join visa vf on vf.id_demande = d.id
                left join carte_resident cr on cr.id_demande = d.id
                order by d.date_creation desc
                """,
                (rs, rowNum) -> new DemandeListItem(
                        rs.getInt("id"),
                        rs.getString("nom_complet").trim(),
                        rs.getString("categorie_demande"),
                    rs.getString("type_demande"),
                    rs.getString("numero_visa"),
                    rs.getString("numero_carte_resident"),
                        rs.getString("statut"),
                        rs.getObject("date_creation", LocalDateTime.class)
                )
        );
    }

    @Override
    public boolean existsCarteResident(String numeroCarteResident) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from carte_resident where numero_carte_resident = ?",
                Integer.class,
                numeroCarteResident
        );
        return count != null && count > 0;
    }

    @Override
    public Integer createDemande(DemandeForm form) {
        if (Boolean.TRUE.equals(form.getAvecDonneesAnterieures()) && TYPE_DUPLICATA_RESIDENT.equals(form.getTypeDemande())) {
            DuplicataSource source = findDuplicataSource(form.getNumeroCarteResident())
                    .orElseThrow(() -> new IllegalArgumentException("Carte resident introuvable"));
            Integer demandeId = insertDemande(
                source.demandeurId(),
                source.visaId(),
                form.getTypeDemande(),
                source.categorieDemande(),
                Boolean.TRUE
            );
            insertDemandePieces(demandeId, source.categorieDemande(), source.pieceIds());
            return demandeId;
        }

        Integer demandeurId = insertDemandeur(form);
        Integer passeportId = insertPasseport(demandeurId, form);
        Integer visaId = insertVisaTransformable(demandeurId, passeportId, form);
        Integer demandeId = insertDemande(demandeurId, visaId, form.getTypeDemande(), form.getCategorieDemande(), form.getAvecDonneesAnterieures());
        insertDemandePieces(demandeId, form.getCategorieDemande(), form.getPieceIds());
        insertCarteResidentIfNeeded(demandeId, demandeurId, form);
        if (shouldCreateVisaFinal(form)) {
            insertVisaFinal(demandeId, demandeurId, passeportId, form);
        }
        return demandeId;
    }

    @Override
    public Optional<DemandeEditData> findForEdit(Integer demandeId) {
        List<DemandeEditData> result = jdbcTemplate.query(
                """
                select d.id_demandeur,
                      v.id_passeport,
                       d.id_visa_transformable,
                         d.type_demande,
                                                 d.avec_donnees_anterieures,
                         cr.numero_carte_resident,
                       d.categorie_demande,
                       dm.nom,
                       dm.prenoms,
                       dm.nom_jeune_fille,
                       dm.date_naissance,
                       dm.lieu_naissance,
                       dm.situation_famille_id,
                       dm.nationalite_id,
                       dm.profession,
                       dm.telephone,
                       dm.email,
                       dm.adresse,
                       p.numero_passeport,
                       p.date_delivrance_passeport,
                       p.date_expiration_passeport,
                       v.reference_visa,
                       v.numero_visa,
                       v.date_entree_mada,
                       v.lieu_entree_mada,
                       v.date_expiration_visa
                from demande d
                join demandeur dm on dm.id = d.id_demandeur
                join visa_transformable v on v.id = d.id_visa_transformable
                left join passeport p on p.id = v.id_passeport
                left join carte_resident cr on cr.id_demande = d.id
                where d.id = ?
                """,
                (rs, rowNum) -> {
                    DemandeForm form = new DemandeForm();
                    form.setNom(rs.getString("nom"));
                    form.setPrenoms(rs.getString("prenoms"));
                    form.setNomJeuneFille(rs.getString("nom_jeune_fille"));
                    form.setDateNaissance(rs.getObject("date_naissance", java.time.LocalDate.class));
                    form.setLieuNaissance(rs.getString("lieu_naissance"));
                    form.setSituationFamilleId(rs.getInt("situation_famille_id"));
                    form.setNationaliteId(rs.getInt("nationalite_id"));
                    form.setProfession(rs.getString("profession"));
                    form.setTelephone(rs.getString("telephone"));
                    form.setEmail(rs.getString("email"));
                    form.setAdresse(rs.getString("adresse"));
                    form.setNumeroPasseport(rs.getString("numero_passeport"));
                    form.setDateDelivrancePasseport(rs.getObject("date_delivrance_passeport", java.time.LocalDate.class));
                    form.setDateExpirationPasseport(rs.getObject("date_expiration_passeport", java.time.LocalDate.class));
                    form.setTypeDemande(rs.getString("type_demande"));
                    form.setAvecDonneesAnterieures(rs.getObject("avec_donnees_anterieures", Boolean.class));
                    form.setNumeroCarteResident(rs.getString("numero_carte_resident"));
                    form.setCategorieDemande(rs.getString("categorie_demande"));
                    form.setReferenceVisa(rs.getString("reference_visa"));
                    form.setNumeroVisa(rs.getString("numero_visa"));
                    form.setDateEntreeMada(rs.getObject("date_entree_mada", java.time.LocalDate.class));
                    form.setLieuEntreeMada(rs.getString("lieu_entree_mada"));
                    form.setDateExpirationVisa(rs.getObject("date_expiration_visa", java.time.LocalDate.class));

                    Set<Integer> selectedPieces = new HashSet<>(jdbcTemplate.query(
                            """
                            select piece_id
                            from demande_piece
                            where demande_id = ? and coche = true
                            """,
                            (piecesRs, piecesRowNum) -> piecesRs.getInt("piece_id"),
                            demandeId
                    ));
                    form.setPieceIds(selectedPieces);

                    return new DemandeEditData(
                            rs.getInt("id_demandeur"),
                            rs.getObject("id_passeport", Integer.class),
                            rs.getInt("id_visa_transformable"),
                            form,
                            selectedPieces
                    );
                },
                demandeId
        );

        return result.stream().findFirst();
    }

    @Override
    public void updateDemande(Integer demandeId, DemandeForm form) {
        DemandeEditData editData = findForEdit(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        Integer passeportId = editData.passeportId();
        if (passeportId == null) {
            passeportId = insertPasseport(editData.demandeurId(), form);
        } else {
            jdbcTemplate.update(
                    """
                    update passeport
                    set numero_passeport = ?,
                        date_delivrance_passeport = ?,
                        date_expiration_passeport = ?
                    where id = ?
                    """,
                    form.getNumeroPasseport(),
                    toSqlDate(form.getDateDelivrancePasseport()),
                    toSqlDate(form.getDateExpirationPasseport()),
                    passeportId
            );
        }

        jdbcTemplate.update(
                """
                update demandeur
                set nom = ?,
                    prenoms = ?,
                    nom_jeune_fille = ?,
                    date_naissance = ?,
                    lieu_naissance = ?,
                    situation_famille_id = ?,
                    nationalite_id = ?,
                    profession = ?,
                    telephone = ?,
                    email = ?,
                    adresse = ?
                where id = ?
                """,
                form.getNom(),
                form.getPrenoms(),
                form.getNomJeuneFille(),
                toSqlDate(form.getDateNaissance()),
                form.getLieuNaissance(),
                form.getSituationFamilleId(),
                form.getNationaliteId(),
                form.getProfession(),
                form.getTelephone(),
                form.getEmail(),
                form.getAdresse(),
                editData.demandeurId()
        );

        jdbcTemplate.update(
                """
            update visa_transformable
                set id_passeport = ?,
                    reference_visa = ?,
                    numero_visa = ?,
                    nature_visa = cast(? as nature_visa_enum),
                    categorie_demande = cast(? as categorie_demande_enum),
                    date_entree_mada = ?,
                    lieu_entree_mada = ?,
                    date_expiration_visa = ?
                where id = ?
                """,
                passeportId,
                form.getReferenceVisa(),
                form.getNumeroVisa(),
                "TRANSFORMABLE",
                form.getCategorieDemande(),
                toSqlDate(form.getDateEntreeMada()),
                form.getLieuEntreeMada(),
                toSqlDate(form.getDateExpirationVisa()),
                editData.visaId()
        );

        jdbcTemplate.update(
            """
            update demande
            set type_demande = cast(? as type_demande_enum),
                categorie_demande = cast(? as categorie_demande_enum),
                statut = cast(? as statut_demande_enum),
                avec_donnees_anterieures = ?
            where id = ?
            """,
            form.getTypeDemande(),
            form.getCategorieDemande(),
            STATUT_DOSSIER_CREE,
            form.getAvecDonneesAnterieures(),
            demandeId
        );

        jdbcTemplate.update("delete from demande_piece where demande_id = ?", demandeId);
        insertDemandePieces(demandeId, form.getCategorieDemande(), form.getPieceIds());
        if (!Boolean.TRUE.equals(form.getAvecDonneesAnterieures())) {
            syncCarteResident(demandeId, editData.demandeurId(), form);
        }
    }

    @Override
    public void updateStatut(Integer demandeId, String statut) {
        jdbcTemplate.update(
                "update demande set statut = cast(? as statut_demande_enum) where id = ?",
                statut,
                demandeId
        );
    }

    private Integer insertDemandeur(DemandeForm form) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    insert into demandeur (
                        nom, prenoms, nom_jeune_fille, date_naissance, lieu_naissance,
                        situation_famille_id, nationalite_id, profession, telephone, email, adresse
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setString(1, form.getNom());
            ps.setString(2, form.getPrenoms());
            ps.setString(3, form.getNomJeuneFille());
            ps.setDate(4, toSqlDate(form.getDateNaissance()));
            ps.setString(5, form.getLieuNaissance());
            ps.setInt(6, form.getSituationFamilleId());
            ps.setInt(7, form.getNationaliteId());
            ps.setString(8, form.getProfession());
            ps.setString(9, form.getTelephone());
            ps.setString(10, form.getEmail());
            ps.setString(11, form.getAdresse());
            return ps;
        };
        jdbcTemplate.update(creator, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    private Integer insertPasseport(Integer demandeurId, DemandeForm form) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    insert into passeport (
                        id_demandeur, numero_passeport, date_delivrance_passeport, date_expiration_passeport
                    ) values (?, ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setInt(1, demandeurId);
            ps.setString(2, form.getNumeroPasseport());
            ps.setDate(3, toSqlDate(form.getDateDelivrancePasseport()));
            ps.setDate(4, toSqlDate(form.getDateExpirationPasseport()));
            return ps;
        };
        jdbcTemplate.update(creator, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    private Integer insertVisaTransformable(Integer demandeurId, Integer passeportId, DemandeForm form) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String numeroVisa = shouldAutoGenerateNumeroVisa(form)
            ? generateUniqueNumeroVisaTransformable()
            : form.getNumeroVisa();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    insert into visa_transformable (
                        id_demandeur, id_passeport, reference_visa, numero_visa, nature_visa,
                        categorie_demande, date_entree_mada, lieu_entree_mada, date_expiration_visa
                    ) values (?, ?, ?, ?, cast(? as nature_visa_enum), cast(? as categorie_demande_enum), ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setInt(1, demandeurId);
            ps.setInt(2, passeportId);
            ps.setString(3, form.getReferenceVisa());
            ps.setString(4, numeroVisa);
            ps.setString(5, "TRANSFORMABLE");
            ps.setString(6, form.getCategorieDemande());
            ps.setDate(7, toSqlDate(form.getDateEntreeMada()));
            ps.setString(8, form.getLieuEntreeMada());
            ps.setDate(9, toSqlDate(form.getDateExpirationVisa()));
            return ps;
        };
        jdbcTemplate.update(creator, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    private Integer insertVisaFinal(Integer demandeId, Integer demandeurId, Integer passeportId, DemandeForm form) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String numeroVisa = generateUniqueNumeroVisaFinal();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    insert into visa (
                        id_demande, id_demandeur, id_passeport, reference_visa, numero_visa, nature_visa,
                        categorie_demande, date_entree_mada, lieu_entree_mada, date_expiration_visa
                    ) values (?, ?, ?, ?, ?, cast(? as nature_visa_enum), cast(? as categorie_demande_enum), ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setInt(1, demandeId);
            ps.setInt(2, demandeurId);
            ps.setInt(3, passeportId);
            ps.setString(4, form.getReferenceVisa());
            ps.setString(5, numeroVisa);
            ps.setString(6, "LONG_SEJOUR");
            ps.setString(7, form.getCategorieDemande());
            ps.setDate(8, toSqlDate(form.getDateEntreeMada()));
            ps.setString(9, form.getLieuEntreeMada());
            ps.setDate(10, toSqlDate(form.getDateExpirationVisa()));
            return ps;
        };
        jdbcTemplate.update(creator, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    private Integer insertDemande(Integer demandeurId, Integer visaId, String typeDemande, String categorieDemande, Boolean avecDonneesAnterieures) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    insert into demande (
                        id_demandeur, id_visa_transformable, type_demande, categorie_demande,
                        avec_donnees_anterieures, statut
                    ) values (?, ?, cast(? as type_demande_enum), cast(? as categorie_demande_enum), cast(? as boolean), cast(? as statut_demande_enum))
                    """,
                    new String[]{"id"}
            );
            ps.setInt(1, demandeurId);
            ps.setInt(2, visaId);
            ps.setString(3, typeDemande);
            ps.setString(4, categorieDemande);
            if (avecDonneesAnterieures == null) {
                ps.setNull(5, java.sql.Types.BOOLEAN);
            } else {
                ps.setBoolean(5, avecDonneesAnterieures);
            }
            String statutToInsert = TYPE_DUPLICATA_RESIDENT.equals(typeDemande) ? STATUT_VISA_APPROUVE : STATUT_DOSSIER_CREE;
            ps.setString(6, statutToInsert);
            return ps;
        };
        jdbcTemplate.update(creator, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    private void insertCarteResidentIfNeeded(Integer demandeId, Integer demandeurId, DemandeForm form) {
        String numeroCarteResident;
        if (shouldAutoGenerateCarteResident(form)) {
            numeroCarteResident = generateUniqueNumeroCarteResident();
        } else if (TYPE_DUPLICATA_RESIDENT.equals(form.getTypeDemande())
                && !Boolean.TRUE.equals(form.getAvecDonneesAnterieures())
                && form.getNumeroCarteResident() != null
                && !form.getNumeroCarteResident().isBlank()) {
            numeroCarteResident = form.getNumeroCarteResident();
        } else {
            return;
        }

        jdbcTemplate.update(
                """
                insert into carte_resident (id_demandeur, id_demande, numero_carte_resident)
                values (?, ?, ?)
                """,
                demandeurId,
                demandeId,
                numeroCarteResident
        );
    }

    private String generateUniqueNumeroVisaTransformable() {
        return generateUniqueNumber("visa_transformable", "numero_visa", PREFIX_NUMERO_VISA);
    }

    private String generateUniqueNumeroVisaFinal() {
        return generateUniqueNumber("visa", "numero_visa", PREFIX_NUMERO_VISA);
    }

    private String generateUniqueNumeroCarteResident() {
        return generateUniqueNumber("carte_resident", "numero_carte_resident", PREFIX_NUMERO_CARTE);
    }

    private String generateUniqueNumber(String tableName, String columnName, String prefix) {
        long seed = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            String candidate = prefix + "-" + (seed + i);
            Integer count = jdbcTemplate.queryForObject(
                    "select count(1) from " + tableName + " where " + columnName + " = ?",
                    Integer.class,
                    candidate
            );
            if (count != null && count == 0) {
                return candidate;
            }
        }

        return prefix + "-" + seed + "-" + Math.abs(java.util.UUID.randomUUID().hashCode());
    }

    private boolean shouldCreateVisaFinal(DemandeForm form) {
        return TYPE_NOUVEAU_TITRE.equals(form.getTypeDemande()) || isDuplicataWithoutPreviousData(form);
    }

    private boolean shouldAutoGenerateNumeroVisa(DemandeForm form) {
        return TYPE_NOUVEAU_TITRE.equals(form.getTypeDemande()) || isDuplicataWithoutPreviousData(form);
    }

    private boolean shouldAutoGenerateCarteResident(DemandeForm form) {
        return TYPE_NOUVEAU_TITRE.equals(form.getTypeDemande()) || isDuplicataWithoutPreviousData(form);
    }

    private boolean isDuplicataWithoutPreviousData(DemandeForm form) {
        return TYPE_DUPLICATA_RESIDENT.equals(form.getTypeDemande()) && !Boolean.TRUE.equals(form.getAvecDonneesAnterieures());
    }

    private void syncCarteResident(Integer demandeId, Integer demandeurId, DemandeForm form) {
        jdbcTemplate.update("delete from carte_resident where id_demande = ?", demandeId);
        insertCarteResidentIfNeeded(demandeId, demandeurId, form);
    }

    private Optional<DuplicataSource> findDuplicataSource(String numeroCarteResident) {
        List<DuplicataSource> sources = jdbcTemplate.query(
                """
                select d.id_demandeur,
                       d.id_visa_transformable,
                       d.categorie_demande,
                       d.id as demande_id
                from carte_resident cr
                join demande d on d.id = cr.id_demande
                where cr.numero_carte_resident = ?
                """,
                (rs, rowNum) -> new DuplicataSource(
                        rs.getInt("id_demandeur"),
                        rs.getInt("id_visa_transformable"),
                        rs.getString("categorie_demande"),
                        findPieceIdsForDemande(rs.getInt("demande_id"))
                ),
                numeroCarteResident
        );

        return sources.stream().findFirst();
    }

    private Set<Integer> findPieceIdsForDemande(Integer demandeId) {
        return new HashSet<>(jdbcTemplate.query(
                "select piece_id from demande_piece where demande_id = ? and coche = true",
                (rs, rowNum) -> rs.getInt("piece_id"),
                demandeId
        ));
    }

    private record DuplicataSource(Integer demandeurId, Integer visaId, String categorieDemande, Set<Integer> pieceIds) {
    }

    private Integer extractGeneratedId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.intValue();
        }

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.get("id") instanceof Number id) {
            return id.intValue();
        }

        throw new IllegalStateException("Impossible de recuperer l'id genere");
    }

    private void insertDemandePieces(Integer demandeId, String categorieDemande, Set<Integer> selectedPieceIds) {
        List<Integer> pieceIds = new ArrayList<>(jdbcTemplate.query(
                """
                select id
                from piece_justificative
                where categorie_demande is null
                   or categorie_demande = cast(? as categorie_demande_enum)
                """,
                (rs, rowNum) -> rs.getInt("id"),
                categorieDemande
        ));

        for (Integer pieceId : pieceIds) {
            jdbcTemplate.update(
                    "insert into demande_piece(demande_id, piece_id, coche) values (?, ?, ?)",
                    demandeId,
                    pieceId,
                    selectedPieceIds != null && selectedPieceIds.contains(pieceId)
            );
        }
    }

    private Date toSqlDate(java.time.LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    public List<String> categories() {
        return List.of(CATEGORIE_TRAVAILLEUR, CATEGORIE_INVESTISSEUR);
    }
}
