<template>
  <div class="page">
    <header class="hero">
      <div class="hero-header">
        <div class="brand">
          <div class="brand-badge">BV</div>
          <h1>Suivi des demandes</h1>
        </div>
        <span class="status-pill">Backoffice Visa</span>
      </div>
      <p>
        Saisissez l'identifiant de la demande et du passeport pour afficher toutes les demandes
        liees, avec leur historique de statuts.
      </p>
    </header>

    <main class="layout">
      <section class="card">
        <form class="form-grid" @submit.prevent="handleSearch">
          <div>
            <label for="demandeId">ID demande</label>
            <input
              id="demandeId"
              v-model.trim="form.demandeId"
              type="text"
              placeholder="Ex: 125"
              inputmode="numeric"
              autocomplete="off"
              required
            />
          </div>
          <div>
            <label for="passeportId">ID passeport</label>
            <input
              id="passeportId"
              v-model.trim="form.passeportId"
              type="text"
              placeholder="Ex: 44"
              inputmode="numeric"
              autocomplete="off"
              required
            />
          </div>
          <div class="actions">
            <button type="submit" :disabled="loading">
              {{ loading ? "Recherche..." : "Afficher les demandes" }}
            </button>
            <button type="button" class="secondary" @click="resetForm">Reinitialiser</button>
          </div>
        </form>
      </section>

      <section v-if="notice" class="notice">{{ notice }}</section>

      <section v-if="results.length" class="card">
        <div class="result-header">
          <h2>Resultats</h2>
          <span class="status-pill">{{ results.length }} demande(s)</span>
        </div>
        <div class="list">
          <article v-for="demande in results" :key="demande.id" class="demande">
            <h3>{{ demande.titre }}</h3>
            <div class="meta">
              <div><strong>ID demande:</strong> {{ demande.id }}</div>
              <div><strong>ID passeport:</strong> {{ demande.passeportId }}</div>
              <div><strong>Type:</strong> {{ demande.type }}</div>
              <div><strong>Statut actuel:</strong> {{ demande.statutActuel }}</div>
              <div><strong>Date creation:</strong> {{ demande.dateCreation }}</div>
              <div><strong>Demandeur:</strong> {{ demande.demandeur }}</div>
            </div>
            <div class="timeline">
              <div v-for="item in demande.historique" :key="item.id" class="timeline-item">
                <strong>{{ item.statut }}</strong>
                <span>{{ item.date }}</span>
                <span v-if="item.note">{{ item.note }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>
    </main>

    <footer class="footer">BO Visa - Vue.js app interne.</footer>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { API_CONFIG } from "./config";

const form = ref({
  demandeId: "",
  passeportId: ""
});
const results = ref([]);
const loading = ref(false);
const notice = ref("");

const handleSearch = async () => {
  notice.value = "";
  results.value = [];

  if (!form.value.demandeId || !form.value.passeportId) {
    notice.value = "Veuillez saisir les deux identifiants.";
    return;
  }

  loading.value = true;
  try {
    const params = new URLSearchParams({
      demandeId: form.value.demandeId,
      passeportId: form.value.passeportId
    });
    const url = `${API_CONFIG.baseUrl}${API_CONFIG.searchEndpoint}?${params.toString()}`;
    const response = await fetch(url, { headers: { Accept: "application/json" } });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const payload = await response.json();
    results.value = Array.isArray(payload) ? payload : payload.data || [];

    if (!results.value.length) {
      notice.value = "Aucune demande trouvee pour ces identifiants.";
    }
  } catch (error) {
    notice.value =
      "Impossible de charger les demandes. Verifie l'endpoint et le backend (" +
      error.message +
      ").";
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  form.value.demandeId = "";
  form.value.passeportId = "";
  results.value = [];
  notice.value = "";
};

onMounted(() => {
  const params = new URLSearchParams(window.location.search);
  const demandeId = params.get("demandeId");
  const passeportId = params.get("passeportId");

  if (demandeId && passeportId) {
    form.value.demandeId = demandeId;
    form.value.passeportId = passeportId;
    handleSearch();
  }
});
</script>
