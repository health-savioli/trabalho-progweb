<script setup>
import { computed, nextTick, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { pesquisar, criar as criarPaciente } from '../../api/pacientes'
import { criar as criarClassificacao } from '../../api/classificacoes'
import { useAuthStore } from '../../stores/auth'
import {
  calcularIdade,
  cpfValido,
  mascararCep,
  mascararCpf,
  mascararTelefone,
  somenteDigitos,
} from '../../utils/mascaras'
import { useCep } from '../../composables/useCep'

const router = useRouter()
const auth = useAuthStore()

const aviso = ref('') // falha ao salvar
const salvando = ref(false)

const busca = ref('')
const resultados = ref([])
const buscando = ref(false)
const ultimaBusca = ref('') // termo a que `resultados` se refere
const erroBusca = ref('')
const selecionado = ref(null)
const cadastrando = ref(false)
const erroCpf = ref('')

const QUEIXA_MAX = 500
const queixa = ref('') // o que o paciente relata; vale para quem foi achado na busca e para o recém-cadastrado

const campoNome = ref(null)
const campoCpf = ref(null)

const hoje = new Date().toLocaleDateString('sv-SE') // AAAA-MM-DD no fuso local

const form = reactive({
  nome: '',
  cpf: '',
  dataNasc: '',
  sexo: '',
  email: '',
  telefone: '',
  cep: '',
  endereco: '',
})

const { statusCep, cepComProblema, consultarCep } = useCep()
let enderecoPorCep = '' // último endereço preenchido automaticamente

async function aoDigitarCep(valor) {
  form.cep = mascararCep(valor)
  const endereco = await consultarCep(form.cep)
  // não sobrescreve um endereço que a pessoa já digitou
  if (endereco && (!form.endereco.trim() || form.endereco === enderecoPorCep)) {
    form.endereco = endereco
    enderecoPorCep = endereco
  }
}

// A busca roda no backend, 300 ms depois de a pessoa parar de digitar.
// `sequencia` descarta respostas antigas que cheguem atrasadas.
let timer
let sequencia = 0

watch(busca, () => {
  const termo = busca.value.trim()
  if (termo === ultimaBusca.value) return // mudança feita pelo código, não pela pessoa

  selecionado.value = null
  sequencia++
  clearTimeout(timer)
  erroBusca.value = ''

  if (!termo) {
    resultados.value = []
    ultimaBusca.value = ''
    buscando.value = false
    return
  }
  buscando.value = true
  timer = setTimeout(() => buscar(termo), 300)
})

async function buscar(termo) {
  const minha = sequencia
  try {
    const dados = await pesquisar(termo)
    if (minha !== sequencia) return
    resultados.value = dados
    ultimaBusca.value = termo
  } catch (e) {
    if (minha !== sequencia) return
    resultados.value = []
    ultimaBusca.value = ''
    erroBusca.value = e.response?.data?.message || 'Não foi possível buscar os pacientes.'
  } finally {
    if (minha === sequencia) buscando.value = false
  }
}

onUnmounted(() => clearTimeout(timer))

const naoEncontrado = computed(
  () =>
    busca.value.trim() !== '' &&
    ultimaBusca.value === busca.value.trim() &&
    !buscando.value &&
    !erroBusca.value &&
    resultados.value.length === 0,
)

const statusBusca = computed(() => {
  const termo = busca.value.trim()
  if (!termo) return 'Digite o nome ou o CPF para procurar.'
  if (erroBusca.value) return ''
  if (buscando.value || ultimaBusca.value !== termo) return 'Buscando…'
  const n = resultados.value.length
  if (n === 0) return 'Nenhum paciente encontrado.'
  return `${n} ${n === 1 ? 'paciente encontrado' : 'pacientes encontrados'}. Selecione um.`
})

function formatarData(iso) {
  return new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR')
}

async function adicionarNaFila(paciente) {
  aviso.value = ''
  salvando.value = true
  try {
    await criarClassificacao(paciente.idPaciente, queixa.value.trim())
    router.push({ name: 'filas' })
  } catch (e) {
    aviso.value = e.response?.data?.message || 'Não foi possível adicionar à fila. Tente novamente.'
  } finally {
    salvando.value = false
  }
}

// Abre o cadastro já com o que foi digitado na busca (CPF ou nome).
async function abrirCadastro() {
  const termo = busca.value.trim()
  if (/^[\d.\-\s]+$/.test(termo)) {
    form.cpf = mascararCpf(termo)
  } else {
    form.nome = termo
  }
  cadastrando.value = true
  await nextTick()
  campoNome.value?.focus()
}

function cpfJaCadastrado(existentes) {
  return existentes.some((p) => somenteDigitos(p.cpf) === somenteDigitos(form.cpf))
}

async function cadastrar() {
  aviso.value = ''
  erroCpf.value = ''

  if (!cpfValido(form.cpf)) {
    erroCpf.value = 'CPF inválido. Confira os números.'
    campoCpf.value?.focus()
    return
  }

  salvando.value = true

  try {
    // confere no backend se o CPF já existe (a busca de cima pode ter sido por nome)
    if (cpfJaCadastrado(await pesquisar(form.cpf))) {
      erroCpf.value = 'Este CPF já está cadastrado. Procure o paciente na busca.'
      campoCpf.value?.focus()
      salvando.value = false
      return
    }
  } catch {
    // sem a checagem, o backend ainda recusa CPF repetido (409)
  }

  let novo
  try {
    novo = await criarPaciente({
      nome: form.nome.trim(),
      cpf: form.cpf,
      dataNasc: form.dataNasc,
      idade: calcularIdade(form.dataNasc),
      sexo: form.sexo,
      email: form.email.trim(),
      telefone: form.telefone || null,
      cep: form.cep,
      endereco: form.endereco.trim() || null,
    })
  } catch (e) {
    aviso.value = e.response?.data?.message || 'Não foi possível cadastrar o paciente.'
    salvando.value = false
    return
  }

  try {
    await criarClassificacao(novo.idPaciente, queixa.value.trim())
    router.push({ name: 'filas' })
  } catch (e) {
    // o paciente já existe: volta para a busca com ele selecionado
    const motivo = e.response?.data?.message || 'erro desconhecido'
    aviso.value = `Paciente cadastrado, mas não foi possível adicioná-lo à fila: ${motivo}`
    ultimaBusca.value = novo.nome.trim()
    resultados.value = [novo]
    busca.value = novo.nome
    selecionado.value = resultados.value[0]
    cadastrando.value = false
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <RouterLink :to="{ name: 'filas' }" class="d-inline-block mb-3">&larr; Voltar às filas</RouterLink>

  <h1 class="h3 mb-4">Adicionar paciente à fila</h1>

  <div v-if="aviso" class="alert alert-danger" role="alert">{{ aviso }}</div>

  <section class="card shadow-sm mb-4" aria-labelledby="titulo-busca">
    <div class="card-body">
      <h2 id="titulo-busca" class="h5 mb-3">1. Buscar paciente</h2>

      <label for="busca" class="form-label">Nome ou CPF</label>
      <input
        id="busca"
        v-model="busca"
        type="search"
        class="form-control"
        autocomplete="off"
        aria-describedby="busca-status"
      />
      <p id="busca-status" class="small mt-2 mb-0" role="status">{{ statusBusca }}</p>

      <div v-if="erroBusca" class="alert alert-danger mt-3 mb-0" role="alert">{{ erroBusca }}</div>

      <fieldset v-if="resultados.length" class="mt-3">
        <legend class="visually-hidden">Pacientes encontrados</legend>
        <div class="list-group">
          <label
            v-for="p in resultados"
            :key="p.idPaciente"
            class="list-group-item list-group-item-action d-flex align-items-center gap-3"
          >
            <input v-model="selecionado" class="form-check-input mt-0" type="radio" name="paciente" :value="p" />
            <span>
              <span class="fw-semibold d-block">{{ p.nome }}</span>
              <span class="small">
                CPF {{ p.cpf }}<template v-if="p.dataNasc"> · nascimento {{ formatarData(p.dataNasc) }}</template>
              </span>
            </span>
          </label>
        </div>
      </fieldset>

      <form v-if="selecionado" class="mt-3" @submit.prevent="adicionarNaFila(selecionado)">
        <label for="queixa-paciente" class="form-label">Queixa do paciente</label>
        <textarea
          id="queixa-paciente"
          v-model="queixa"
          class="form-control"
          rows="3"
          :maxlength="QUEIXA_MAX"
          aria-describedby="queixa-paciente-ajuda"
          required
        ></textarea>
        <div id="queixa-paciente-ajuda" class="form-text">
          O que o paciente relata, com as palavras dele. {{ queixa.length }}/{{ QUEIXA_MAX }}
        </div>

        <button type="submit" class="btn btn-primary mt-3" :disabled="salvando">
          <span v-if="salvando" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          Adicionar {{ selecionado.nome }} à fila
        </button>
      </form>

      <div v-if="naoEncontrado && !cadastrando" class="alert alert-warning mt-3 mb-0">
        <p class="mb-2">Este paciente ainda não está cadastrado.</p>
        <button
          v-if="auth.can('cadastrar_paciente')"
          type="button"
          class="btn btn-outline-primary"
          @click="abrirCadastro"
        >
          Cadastrar novo paciente
        </button>
        <p v-else class="mb-0">Você não tem permissão para cadastrar pacientes. Peça a um usuário autorizado.</p>
      </div>
    </div>
  </section>

  <form v-if="cadastrando" class="card shadow-sm" aria-labelledby="titulo-cadastro" @submit.prevent="cadastrar">
    <div class="card-body">
      <h2 id="titulo-cadastro" class="h5 mb-3">2. Cadastrar novo paciente</h2>

      <div class="row g-3">
        <div class="col-12">
          <label for="nome" class="form-label">Nome completo</label>
          <input id="nome" ref="campoNome" v-model="form.nome" type="text" class="form-control" required />
        </div>

        <div class="col-12 col-md-6">
          <label for="cpf" class="form-label">CPF</label>
          <input
            id="cpf"
            ref="campoCpf"
            :value="form.cpf"
            type="text"
            inputmode="numeric"
            class="form-control"
            :class="{ 'is-invalid': erroCpf }"
            :aria-invalid="!!erroCpf"
            aria-describedby="cpf-erro"
            placeholder="000.000.000-00"
            maxlength="14"
            required
            @input="form.cpf = mascararCpf($event.target.value)"
          />
          <div id="cpf-erro" class="invalid-feedback">{{ erroCpf }}</div>
        </div>

        <div class="col-12 col-md-6">
          <label for="dataNasc" class="form-label">Data de nascimento</label>
          <input id="dataNasc" v-model="form.dataNasc" type="date" class="form-control" :max="hoje" required />
        </div>

        <div class="col-12 col-md-6">
          <fieldset>
            <legend class="form-label fs-6">Sexo</legend>
            <div class="form-check form-check-inline">
              <input id="sexo-m" v-model="form.sexo" class="form-check-input" type="radio" name="sexo" value="M" required />
              <label class="form-check-label" for="sexo-m">Masculino</label>
            </div>
            <div class="form-check form-check-inline">
              <input id="sexo-f" v-model="form.sexo" class="form-check-input" type="radio" name="sexo" value="F" required />
              <label class="form-check-label" for="sexo-f">Feminino</label>
            </div>
          </fieldset>
        </div>

        <div class="col-12 col-md-6">
          <label for="email" class="form-label">E-mail</label>
          <input id="email" v-model="form.email" type="email" class="form-control" required />
        </div>

        <div class="col-12 col-md-6">
          <label for="telefone" class="form-label">Telefone <span class="small">(opcional)</span></label>
          <input
            id="telefone"
            :value="form.telefone"
            type="tel"
            inputmode="numeric"
            class="form-control"
            placeholder="(00) 00000-0000"
            @input="form.telefone = mascararTelefone($event.target.value)"
          />
        </div>

        <div class="col-12 col-md-6">
          <label for="cep" class="form-label">CEP</label>
          <input
            id="cep"
            :value="form.cep"
            type="text"
            inputmode="numeric"
            class="form-control"
            placeholder="00000-000"
            minlength="9"
            maxlength="9"
            aria-describedby="cep-status"
            required
            @input="aoDigitarCep($event.target.value)"
          />
          <div id="cep-status" class="form-text" :class="{ 'text-danger-emphasis': cepComProblema }" role="status">
            {{ statusCep }}
          </div>
        </div>

        <div class="col-12">
          <label for="endereco" class="form-label">Endereço <span class="small">(opcional)</span></label>
          <input id="endereco" v-model="form.endereco" type="text" class="form-control" />
        </div>

        <div class="col-12">
          <label for="queixa-cadastro" class="form-label">Queixa do paciente</label>
          <textarea
            id="queixa-cadastro"
            v-model="queixa"
            class="form-control"
            rows="3"
            :maxlength="QUEIXA_MAX"
            aria-describedby="queixa-cadastro-ajuda"
            required
          ></textarea>
          <div id="queixa-cadastro-ajuda" class="form-text">
            O que o paciente relata, com as palavras dele. {{ queixa.length }}/{{ QUEIXA_MAX }}
          </div>
        </div>
      </div>

      <div class="d-flex flex-wrap gap-2 mt-4">
        <button type="submit" class="btn btn-primary" :disabled="salvando">
          <span v-if="salvando" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          {{ salvando ? 'Salvando…' : 'Cadastrar e adicionar à fila' }}
        </button>
        <button type="button" class="btn btn-outline-secondary" :disabled="salvando" @click="cadastrando = false">
          Cancelar
        </button>
      </div>
    </div>
  </form>
</template>
