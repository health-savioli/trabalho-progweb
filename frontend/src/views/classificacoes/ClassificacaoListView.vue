<script setup>
import { onMounted, ref } from 'vue'
import { excluir, listar } from '../../api/classificacoes'
import { useAuthStore } from '../../stores/auth'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'

const auth = useAuthStore()

const classificacoes = ref([])
const carregando = ref(true)
const erro = ref('')
const sucesso = ref('')

const dialogo = ref(null)
const alvo = ref(null)
const excluindo = ref(false)
const erroExclusao = ref('')

// Esta tela é o histórico: só as classificações já finalizadas, da mais recente para a mais antiga.
async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    classificacoes.value = await listar('finalizada')
  } catch (e) {
    erro.value = e.response?.data?.message || 'Não foi possível carregar as classificações.'
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

function formatarData(iso) {
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

const semAfericao = 'Não aferido'

function temperatura(valor) {
  return valor == null
    ? semAfericao
    : `${valor.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} °C`
}
const saturacao = (valor) => (valor == null ? semAfericao : `${valor} %`)
const frequencia = (valor) => (valor == null ? semAfericao : `${valor} bpm`)

function pedirConfirmacao(classificacao) {
  alvo.value = classificacao
  erroExclusao.value = ''
  dialogo.value.abrir()
}

async function confirmarExclusao() {
  excluindo.value = true
  erroExclusao.value = ''
  try {
    await excluir(alvo.value.idClassificacao)
    classificacoes.value = classificacoes.value.filter((c) => c.idClassificacao !== alvo.value.idClassificacao)
    sucesso.value = `Classificação de ${alvo.value.pacienteNome} excluída.`
    dialogo.value.fechar()
  } catch (e) {
    erroExclusao.value = e.response?.data?.message || 'Não foi possível excluir a classificação.'
  } finally {
    excluindo.value = false
  }
}
</script>

<template>
  <h1 class="h3 mb-1">Classificações finalizadas</h1>
  <p class="mb-4">Histórico dos atendimentos concluídos, com os sinais vitais medidos na aferição.</p>

  <div v-if="sucesso" class="alert alert-success" role="status">{{ sucesso }}</div>

  <p v-if="carregando" role="status" class="text-center py-5 mb-0">
    <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Carregando…
  </p>

  <div v-else-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

  <div v-else-if="classificacoes.length === 0" class="card shadow-sm">
    <div class="card-body text-center py-5">
      <p class="mb-0">Nenhuma classificação finalizada ainda.</p>
    </div>
  </div>

  <div v-else class="card shadow-sm">
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <caption class="visually-hidden">Classificações finalizadas, com os resultados da aferição</caption>
        <thead class="table-light">
          <tr>
            <th scope="col">Paciente</th>
            <th scope="col">Queixa</th>
            <th scope="col">Risco</th>
            <th scope="col">Temperatura</th>
            <th scope="col">Saturação O₂</th>
            <th scope="col">Freq. cardíaca</th>
            <th scope="col">Chegada</th>
            <th scope="col">Classificado por</th>
            <th v-if="auth.can('excluir_classificacao')" scope="col" class="text-end">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in classificacoes" :key="c.idClassificacao">
            <td class="fw-semibold">{{ c.pacienteNome }}</td>
            <td style="min-width: 16rem">{{ c.queixa ?? 'Não informada' }}</td>
            <td>
              <span v-if="c.risco" class="badge text-capitalize" :class="`risco-${c.risco}`">{{ c.risco }}</span>
              <span v-else class="badge text-bg-secondary">Não classificado</span>
            </td>
            <td>{{ temperatura(c.temperatura) }}</td>
            <td>{{ saturacao(c.saturacaoO2) }}</td>
            <td>{{ frequencia(c.frequenciaCardiaca) }}</td>
            <td>{{ formatarData(c.criadoEm) }}</td>
            <td>{{ c.userNome ?? 'Não definido' }}</td>
            <td v-if="auth.can('excluir_classificacao')">
              <div class="d-flex justify-content-end">
                <button
                  type="button"
                  class="btn btn-outline-danger btn-sm"
                  :aria-label="`Excluir classificação de ${c.pacienteNome}`"
                  @click="pedirConfirmacao(c)"
                >
                  Excluir
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <ConfirmDialog
    ref="dialogo"
    titulo="Excluir classificação"
    :erro="erroExclusao"
    :ocupado="excluindo"
    @confirmar="confirmarExclusao"
    @fechou="alvo = null"
  >
    <p v-if="alvo">
      Tem certeza que deseja excluir a classificação de <strong>{{ alvo.pacienteNome }}</strong>? Esta ação não
      pode ser desfeita.
    </p>
  </ConfirmDialog>
</template>
