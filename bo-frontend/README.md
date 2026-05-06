# BO Frontend

App Vue.js pour la recherche des demandes par ID demande + ID passeport.

## Lancer en local

```bash
cd bo-frontend
npm install
npm run dev
npm run dev -- --host 0.0.0.0 --port 5173
```

## Endpoint attendu

Par defaut, l'app appelle :

```
GET /api/demandes/search?demandeId=123&passeportId=44
```

- Base URL configurable via `VITE_API_BASE_URL`
- Endpoint configurable via `VITE_DEMANDES_ENDPOINT`

## Format JSON attendu

```json
[
  {
    "id": 125,
    "passeportId": 44,
    "titre": "Demande de nouveau titre",
    "type": "Nouveau titre",
    "statutActuel": "En cours",
    "dateCreation": "2026-05-05",
    "demandeur": "Rabe Paul",
    "historique": [
      { "id": 1, "statut": "Creee", "date": "2026-04-12", "note": "Depot en ligne" },
      { "id": 2, "statut": "En cours", "date": "2026-04-15" }
    ]
  }
]
```
