<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { atualizar, buscar, iniciarAfericao } from '../../api/classificacoes'
import { fechar, sincronizar } from '../../api/afericoes'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const classificacao = ref(null)
const carregando = ref(true)
const erro = ref('') // falha ao abrir a tela
const aviso = ref('') // falha de uma ação (iniciar, sincronizar, fechar, salvar)
const acao = ref('') // ação em andamento: 'iniciar' | 'sincronizar' | 'fechar' | 'salvar'
const risco = ref('')

const riscos = [
  { valor: 'vermelho', descricao: 'Emergência' },
  { valor: 'laranja', descricao: 'Muito urgente' },
  { valor: 'amarelo', descricao: 'Urgente' },
  { valor: 'verde', descricao: 'Pouco urgente' },
  { valor: 'azul', descricao: 'Não urgente' },
]

const temAfericao = computed(() => !!classificacao.value?.afericaoId)
const temResultado = computed(() => {
  const c = classificacao.value
  return c?.temperatura != null && c?.saturacaoO2 != null && c?.frequenciaCardiaca != null
})

async function recarregar() {
  classificacao.value = await buscar(id)
  if (!risco.value && classificacao.value.risco) {
    risco.value = classificacao.value.risco
  }
}

// Roda uma ação mostrando o spinner no botão e o erro do backend, se houver.
async function executar(nome, fn) {
  aviso.value = ''
  acao.value = nome
  try {
    await fn()
  } catch (e) {
    aviso.value = e.response?.data?.message || 'Não foi possível concluir a ação. Tente novamente.'
  } finally {
    acao.value = ''
  }
}

const iniciar = () =>
  executar('iniciar', async () => {
    await iniciarAfericao(id)
    await recarregar()
  })

const atualizarResultados = () =>
  executar('sincronizar', async () => {
    await sincronizar(classificacao.value.afericaoId)
    await recarregar()
  })

const encerrar = () =>
  executar('fechar', async () => {
    await fechar(classificacao.value.afericaoId)
    await recarregar()
  })

const confirmar = () =>
  executar('salvar', async () => {
    await atualizar(id, { risco: risco.value })
    router.push({ name: 'filas' })
  })

// Enquanto a aferição não trouxe os três sinais vitais, busca de novo a cada 10 s.
let timer
onMounted(async () => {
  try {
    await recarregar()
  } catch (e) {
    erro.value = e.response?.data?.message || 'Não foi possível carregar a classificação.'
  } finally {
    carregando.value = false
  }

  timer = setInterval(async () => {
    if (!temAfericao.value || temResultado.value || acao.value) return
    try {
      await sincronizar(classificacao.value.afericaoId)
      await recarregar()
    } catch {
      // tenta de novo no próximo ciclo
    }
  }, 10000)
})
onUnmounted(() => clearInterval(timer))

function valor(v, unidade) {
  return v == null ? 'Aguardando…' : `${v} ${unidade}`
}
</script>

<template>
  <RouterLink :to="{ name: 'filas' }" class="d-inline-block mb-3">&larr; Voltar às filas</RouterLink>

  <p v-if="carregando" role="status" class="text-center py-5 mb-0">
    <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Carregando…
  </p>

  <div v-else-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

  <template v-else>
    <h1 class="h3 mb-1">Classificar paciente</h1>
    <p class="fs-5 fw-semibold mb-2">{{ classificacao.pacienteNome }}</p>
    <div class="alert alert-secondary" role="note">
      <strong>Queixa:</strong> {{ classificacao.queixa ?? 'não informada' }}
    </div>

    <div v-if="aviso" class="alert alert-danger" role="alert">{{ aviso }}</div>

    <div class="row g-4">
      <section class="col-12 col-lg-6" aria-labelledby="titulo-afericao">
        <div class="card shadow-sm h-100">
          <div class="card-header">
            <h2 id="titulo-afericao" class="h5 mb-0">1. Aferição</h2>
          </div>
          <div class="card-body">
            <template v-if="!temAfericao">
              <p>Inicie a aferição para que o paciente faça a medição dos sinais vitais na cabine.</p>
              <button type="button" class="btn btn-primary" :disabled="acao !== ''" @click="iniciar">
                <span v-if="acao === 'iniciar'" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                {{ acao === 'iniciar' ? 'Iniciando…' : 'Iniciar aferição' }}
              </button>
            </template>

            <template v-else>
              <p class="mb-3">
                Status da sessão:
                <span class="badge text-bg-secondary">{{ classificacao.afericaoStatus }}</span>
              </p>

              <dl class="row mb-3" aria-live="polite">
                <dt class="col-7">Temperatura</dt>
                <dd class="col-5">{{ valor(classificacao.temperatura, '°C') }}</dd>
                <dt class="col-7">Saturação de O₂</dt>
                <dd class="col-5">{{ valor(classificacao.saturacaoO2, '%') }}</dd>
                <dt class="col-7">Frequência cardíaca</dt>
                <dd class="col-5">{{ valor(classificacao.frequenciaCardiaca, 'bpm') }}</dd>
              </dl>

              <p v-if="!temResultado" class="small mb-3">
                Aguardando a medição do paciente. Esta tela busca os resultados sozinha a cada 10 segundos.
              </p>

              <div class="d-flex flex-wrap gap-2">
                <button type="button" class="btn btn-outline-secondary" :disabled="acao !== ''" @click="atualizarResultados">
                  <span v-if="acao === 'sincronizar'" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                  Atualizar resultados
                </button>
                <button type="button" class="btn btn-outline-secondary" :disabled="acao !== ''" @click="encerrar">
                  <span v-if="acao === 'fechar'" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                  Encerrar aferição
                </button>
              </div>
            </template>
          </div>
        </div>
      </section>

      <section class="col-12 col-lg-6" aria-labelledby="titulo-risco">
        <form class="card shadow-sm h-100" @submit.prevent="confirmar">
          <div class="card-header">
            <h2 id="titulo-risco" class="h5 mb-0">2. Classificação de risco</h2>
          </div>
          <div class="card-body">
            <fieldset :disabled="!temAfericao" aria-describedby="risco-ajuda">
              <legend class="visually-hidden">Nível de risco</legend>
              <div v-for="r in riscos" :key="r.valor" class="form-check mb-2">
                <input
                  :id="`risco-${r.valor}`"
                  v-model="risco"
                  class="form-check-input"
                  type="radio"
                  name="risco"
                  :value="r.valor"
                  required
                />
                <label class="form-check-label" :for="`risco-${r.valor}`">
                  <span class="badge text-capitalize me-2" :class="`risco-${r.valor}`">{{ r.valor }}</span>
                  {{ r.descricao }}
                </label>
              </div>
            </fieldset>

            <p id="risco-ajuda" class="small mt-3 mb-3">
              {{ temAfericao ? 'Escolha o risco com base nos sinais vitais e na avaliação clínica.' : 'Inicie a aferição antes de classificar.' }}
            </p>

            <button type="submit" class="btn btn-primary" :disabled="!temAfericao || !risco || acao !== ''">
              <span v-if="acao === 'salvar'" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
              {{ acao === 'salvar' ? 'Salvando…' : 'Confirmar classificação' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </template>
</template>
