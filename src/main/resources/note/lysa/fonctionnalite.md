# Context : 
Rehefa tonga eto ny vazaha d mangataka visa
Types de visa :
-Visa transformable
-Visa non transformable
-Visa long sejour
-Visa etudiant
-etc

Type de demande (misy statut) :
Nouveau titre, transfert de visa (tsy mikitika carte resident), duplicata du resident, tapitra d renouvellement, changement de statut du visa avant date expiration

transfert de visa (tsy mikitika carte resident), duplicata du resident manana type 'sans donnees anterieures'

date_delivrance

BASE :
table demandeur
table demande
table visa
champs obligatoires : nom, situation table, adresse mada, nationalite table
tsy misy num passport ao amin demandeur, fa id demandeur ao amin passport.
categorie_demande : travailleur, investisseur
demandeur ray manana visa transformable maromaro, 
table visa asiana id demandeur
# table infos_visa foreign key dans demandeur
# table carte resident table visa misy id demande, id demandeur, misy id passport
# id visa dans demande ou id demande dans visa