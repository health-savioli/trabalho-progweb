<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import {
  atender,
  filaAtendimento,
  filaClassificacao,
  filaEmAtendimento,
  finalizar,
} from '../../api/classificacoes'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

const paraClassificar = ref([])
const paraAtender = ref([])
const emAtendimento = ref([])
const carregando = ref(true)
const erro = ref('')
const aviso = ref('') // falha ao atender/finalizar
const sucesso = ref('')
const ocupadoId = ref(null) // classificação com uma ação em andamento
let timer

async function carregar() {
  erro.value = ''
  try {
    const [a, b, c] = await Promise.all([filaClassificacao(), filaAtendimento(), filaEmAtendimento()])
    paraClassificar.value = a
    paraAtender.value = b
    emAtendimento.value = c
  } catch (e) {
    erro.value = e.response?.data?.message || 'Não foi possível carregar as filas.'
  } finally {
    carregando.value = false
  }
}

onMounted(() => {
  carregar()
  timer = setInterval(carregar, 30000)
})
onUnmounted(() => clearInterval(timer))

const textoQueixa = (c) => (c.queixa ? `Queixa: ${c.queixa}` : 'Queixa não informada')

function hora(iso) {
  return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

// Roda a ação e recarrega as filas mesmo se falhar: um 409 quer dizer que outra
// pessoa já mexeu naquele paciente, e a lista na tela está desatualizada.
async function executar(c, acao, mensagem) {
  aviso.value = ''
  sucesso.value = ''
  ocupadoId.value = c.idClassificacao
  try {
    await acao(c.idClassificacao)
    sucesso.value = mensagem
  } catch (e) {
    aviso.value = e.response?.data?.message || 'Não foi possível concluir a ação. Tente novamente.'
  } finally {
    ocupadoId.value = null
    await carregar()
  }
}

const atenderPaciente = (c) => executar(c, atender, `${c.pacienteNome} foi chamado para atendimento.`)
const finalizarAtendimento = (c) => executar(c, finalizar, `Atendimento de ${c.pacienteNome} finalizado.`)
</script>

<template>
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-4">
    <h1 class="h3 mb-0">Filas</h1>
    <div class="d-flex flex-wrap gap-2">
      <button type="button" class="btn btn-outline-secondary" @click="carregar">Atualizar</button>
      <RouterLink
        v-if="auth.can('cadastrar_classificacao')"
        :to="{ name: 'filas-adicionar' }"
        class="btn btn-primary"
      >
        Adicionar paciente à fila
      </RouterLink>
    </div>
  </div>

  <div v-if="sucesso" class="alert alert-success" role="status">{{ sucesso }}</div>
  <div v-if="aviso" class="alert alert-danger" role="alert">{{ aviso }}</div>

  <p v-if="carregando" role="status" class="text-center py-5 mb-0">
    <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Carregando…
  </p>

  <div v-else-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

  <div v-else class="row g-4">
    <section class="col-12 col-xl-6" aria-labelledby="titulo-classificar">
      <div class="card shadow-sm h-100">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h2 id="titulo-classificar" class="h5 mb-0">Aguardando classificação</h2>
          <span class="badge text-bg-secondary">{{ paraClassificar.length }}</span>
        </div>
        <p v-if="paraClassificar.length === 0" class="card-body mb-0">Ninguém na fila.</p>
        <ol v-else class="list-group list-group-flush list-group-numbered">
          <li
            v-for="c in paraClassificar"
            :key="c.idClassificacao"
            class="list-group-item d-flex justify-content-between align-items-center gap-2"
          >
            <span class="ms-2 me-auto">
              <span class="fw-semibold d-block">{{ c.pacienteNome }}</span>
              <span class="small d-block">{{ textoQueixa(c) }}</span>
            </span>
            <span class="small">chegou às {{ hora(c.criadoEm) }}</span>
            <RouterLink
              v-if="auth.can('editar_classificacao')"
              :to="{ name: 'classificar', params: { id: c.idClassificacao } }"
              class="btn btn-primary btn-sm"
              :aria-label="`Classificar ${c.pacienteNome}`"
            >
              Classificar
            </RouterLink>
          </li>
        </ol>
      </div>
    </section>

    <section class="col-12 col-xl-6" aria-labelledby="titulo-atender">
      <div class="card shadow-sm h-100">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h2 id="titulo-atender" class="h5 mb-0">Aguardando atendimento médico</h2>
          <span class="badge text-bg-secondary">{{ paraAtender.length }}</span>
        </div>
        <p v-if="paraAtender.length === 0" class="card-body mb-0">Ninguém na fila.</p>
        <ol v-else class="list-group list-group-flush list-group-numbered">
          <li
            v-for="c in paraAtender"
            :key="c.idClassificacao"
            class="list-group-item d-flex justify-content-between align-items-center gap-2"
          >
            <span class="ms-2 me-auto">
              <span class="fw-semibold d-block">{{ c.pacienteNome }}</span>
              <span class="small d-block">{{ textoQueixa(c) }}</span>
            </span>
            <span class="badge text-capitalize" :class="`risco-${c.risco}`">{{ c.risco }}</span>
            <span class="small">chegou às {{ hora(c.criadoEm) }}</span>
            <button
              v-if="auth.can('atender_classificacao')"
              type="button"
              class="btn btn-primary btn-sm"
              :disabled="ocupadoId !== null"
              :aria-label="`Atender ${c.pacienteNome}`"
              @click="atenderPaciente(c)"
            >
              <span
                v-if="ocupadoId === c.idClassificacao"
                class="spinner-border spinner-border-sm me-1"
                aria-hidden="true"
              ></span>
              Atender
            </button>
          </li>
        </ol>
      </div>
    </section>

    <section class="col-12" aria-labelledby="titulo-em-atendimento">
      <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h2 id="titulo-em-atendimento" class="h5 mb-0">Em atendimento</h2>
          <span class="badge text-bg-secondary">{{ emAtendimento.length }}</span>
        </div>
        <p v-if="emAtendimento.length === 0" class="card-body mb-0">Nenhum atendimento em andamento.</p>
        <ul v-else class="list-group list-group-flush">
          <li
            v-for="c in emAtendimento"
            :key="c.idClassificacao"
            class="list-group-item d-flex justify-content-between align-items-center gap-2"
          >
            <span class="me-auto">
              <span class="fw-semibold d-block">{{ c.pacienteNome }}</span>
              <span class="small d-block">{{ textoQueixa(c) }}</span>
            </span>
            <span class="badge text-capitalize" :class="`risco-${c.risco}`">{{ c.risco }}</span>
            <span class="small">chegou às {{ hora(c.criadoEm) }}</span>
            <button
              v-if="auth.can('atender_classificacao')"
              type="button"
              class="btn btn-outline-primary btn-sm"
              :disabled="ocupadoId !== null"
              :aria-label="`Finalizar atendimento de ${c.pacienteNome}`"
              @click="finalizarAtendimento(c)"
            >
              <span
                v-if="ocupadoId === c.idClassificacao"
                class="spinner-border spinner-border-sm me-1"
                aria-hidden="true"
              ></span>
              Finalizar
            </button>
          </li>
        </ul>
      </div>
    </section>
  </div>
</template>
