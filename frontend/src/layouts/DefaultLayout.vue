<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const itens = [
    { to: { name: 'filas' }, rotulo: 'Filas', permissao: 'visualizar_classificacao' },
    { to: { name: 'classificacoes' }, rotulo: 'Classificações', permissao: 'visualizar_classificacao' },
    { to: { name: 'pacientes' }, rotulo: 'Pacientes', permissao: 'visualizar_paciente' },
    { to: { name: 'usuarios' }, rotulo: 'Usuários', permissao: 'visualizar_user' },
]
const menu = computed(() => itens.filter((i) => !i.permissao || auth.can(i.permissao)))
const inicial = computed(() => auth.nome.charAt(0).toUpperCase())

function sair() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <a class="visually-hidden-focusable" href="#conteudo">Ir para o conteúdo</a>

  <div class="layout">
    <header class="sidebar bg-dark d-flex flex-wrap flex-md-column align-items-center align-items-md-stretch gap-3 p-3">
      <RouterLink class="marca" :to="{ name: 'home' }">ProgWeb</RouterLink>

      <nav aria-label="Navegação principal">
        <ul class="nav nav-pills flex-md-column gap-1">
          <li v-for="item in menu" :key="item.rotulo" class="nav-item">
            <RouterLink :to="item.to" class="nav-link" exact-active-class="active">
              {{ item.rotulo }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <div class="usuario d-flex align-items-center gap-2 ms-auto ms-md-0 mt-md-auto">
        <span class="avatar" aria-hidden="true">{{ inicial }}</span>
        <span class="nome text-white text-truncate">{{ auth.nome }}</span>
        <button type="button" class="btn btn-outline-light btn-sm ms-auto" @click="sair">Sair</button>
      </div>
    </header>

    <div class="area">
      <main id="conteudo" class="conteudo px-3 px-md-4 py-4" tabindex="-1">
        <RouterView />
      </main>

      <footer class="bg-white border-top py-3">
        <p class="text-center small mb-0">Trabalho de Programação Web · UFMS</p>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* a partir do tablet: menu fixo na lateral, conteúdo ao lado */
@media (min-width: 768px) {
  .layout {
    flex-direction: row;
  }
  .sidebar {
    position: sticky;
    top: 0;
    width: 16rem;
    height: 100vh;
    flex-shrink: 0;
  }
}

.marca {
  font-size: 1.35rem;
  font-weight: 700;
  color: #fff;
  text-decoration: none;
}

.sidebar .nav-link {
  color: #e9ecef;
}
.sidebar .nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}
.sidebar .nav-link.active {
  color: #fff;
  background-color: #0d6efd;
}

.avatar {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  font-weight: 700;
  color: #1a1a1a;
  background-color: #e9ecef;
}
.nome {
  max-width: 7rem;
}

.area {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  min-width: 0; /* deixa a tabela rolar em vez de empurrar a página */
}
.conteudo {
  flex-grow: 1;
  width: 100%;
}
</style>
