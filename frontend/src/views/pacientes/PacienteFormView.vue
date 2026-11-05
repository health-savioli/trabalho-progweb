<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { atualizar, buscar, criar } from '../../api/pacientes'
import {
  calcularIdade,
  cpfValido,
  mascararCep,
  mascararCpf,
  mascararTelefone,
  somenteDigitos,
} from '../../utils/mascaras'
import { useCep } from '../../composables/useCep'

const route = useRoute()
const router = useRouter()

// a mesma tela serve para cadastrar (/pacientes/novo) e editar (/pacientes/:id/editar)
const id = route.params.id
const editando = !!id

const carregando = ref(editando)
const erro = ref('') // falha ao abrir a tela
const aviso = ref('') // falha ao salvar
const salvando = ref(false)
const erroCpf = ref('')

const campoCpf = ref(null)

const hoje = new Date().toLocaleDateString('sv-SE') // AAAA-MM-DD no fuso local
let cpfOriginal = ''

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

onMounted(async () => {
  if (!editando) return
  try {
    const p = await buscar(id)
    cpfOriginal = somenteDigitos(p.cpf)
    Object.assign(form, {
      nome: p.nome,
      cpf: mascararCpf(p.cpf),
      dataNasc: p.dataNasc ?? '',
      sexo: p.sexo ?? '',
      email: p.email ?? '',
      telefone: mascararTelefone(p.telefone),
      cep: mascararCep(p.cep),
      endereco: p.endereco ?? '',
    })
  } catch (e) {
    erro.value = e.response?.data?.message || 'Não foi possível carregar o paciente.'
  } finally {
    carregando.value = false
  }
})

async function salvar() {
  aviso.value = ''
  erroCpf.value = ''

  // CPF só é conferido se mudou: pacientes antigos podem ter um CPF de teste
  const cpfMudou = somenteDigitos(form.cpf) !== cpfOriginal
  if (cpfMudou && !cpfValido(form.cpf)) {
    erroCpf.value = 'CPF inválido. Confira os números.'
    campoCpf.value?.focus()
    return
  }

  const dados = {
    nome: form.nome.trim(),
    cpf: form.cpf,
    dataNasc: form.dataNasc,
    idade: calcularIdade(form.dataNasc),
    sexo: form.sexo,
    email: form.email.trim(),
    telefone: form.telefone || null,
    cep: form.cep,
    endereco: form.endereco.trim() || null,
  }

  salvando.value = true
  try {
    if (editando) {
      await atualizar(id, dados)
    } else {
      await criar(dados)
    }
    router.push({ name: 'pacientes', query: { salvo: editando ? 'editado' : 'criado' } })
  } catch (e) {
    aviso.value = e.response?.data?.message || 'Não foi possível salvar. Tente novamente.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <RouterLink :to="{ name: 'pacientes' }" class="d-inline-block mb-3">&larr; Voltar aos pacientes</RouterLink>

  <h1 class="h3 mb-4">{{ editando ? 'Editar paciente' : 'Novo paciente' }}</h1>

  <p v-if="carregando" role="status" class="text-center py-5 mb-0">
    <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Carregando…
  </p>

  <div v-else-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

  <form v-else class="card shadow-sm" @submit.prevent="salvar">
    <div class="card-body p-4">
      <div v-if="aviso" class="alert alert-danger" role="alert">{{ aviso }}</div>

      <div class="row g-3">
        <div class="col-12">
          <label for="nome" class="form-label">Nome completo</label>
          <input id="nome" v-model="form.nome" type="text" class="form-control" required />
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
            minlength="14"
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
      </div>

      <div class="d-flex flex-wrap gap-2 mt-4">
        <button type="submit" class="btn btn-primary" :disabled="salvando">
          <span v-if="salvando" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          {{ salvando ? 'Salvando…' : 'Salvar' }}
        </button>
        <RouterLink :to="{ name: 'pacientes' }" class="btn btn-outline-secondary">Cancelar</RouterLink>
      </div>
    </div>
  </form>
</template>
