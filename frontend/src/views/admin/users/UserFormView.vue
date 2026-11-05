<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { atualizar, buscar, criar } from '../../../api/users'
import { listar as listarTipos } from '../../../api/tiposUser'
import {
  cpfValido,
  mascararCep,
  mascararCpf,
  mascararTelefone,
  somenteDigitos,
} from '../../../utils/mascaras'
import { useCep } from '../../../composables/useCep'

const route = useRoute()
const router = useRouter()

// a mesma tela serve para criar (/usuarios/novo) e editar (/usuarios/:id/editar)
const id = route.params.id
const editando = !!id

const tipos = ref([])
const carregando = ref(true)
const erro = ref('') // falha ao abrir a tela
const aviso = ref('') // falha ao salvar
const salvando = ref(false)
const erroCpf = ref('')
const erroSenha = ref('')

const campoCpf = ref(null)
const campoConfirmar = ref(null)

const hoje = new Date().toLocaleDateString('sv-SE') // AAAA-MM-DD no fuso local
let cpfOriginal = ''

const form = reactive({
  nome: '',
  email: '',
  cpf: '',
  dataNasc: '',
  telefone: '',
  cep: '',
  endereco: '',
  tipoId: '',
  senha: '',
  confirmar: '',
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
  try {
    tipos.value = await listarTipos()

    if (editando) {
      const u = await buscar(id)
      cpfOriginal = somenteDigitos(u.cpf)
      Object.assign(form, {
        nome: u.nome,
        email: u.email,
        cpf: mascararCpf(u.cpf),
        dataNasc: u.dataNasc,
        telefone: mascararTelefone(u.telefone),
        cep: mascararCep(u.cep),
        endereco: u.endereco,
        tipoId: u.tipoUser.idTpUser,
      })
    }
  } catch (e) {
    erro.value = e.response?.data?.message || 'Não foi possível carregar os dados.'
  } finally {
    carregando.value = false
  }
})

async function salvar() {
  aviso.value = ''
  erroCpf.value = ''
  erroSenha.value = ''

  // CPF só é conferido se mudou: o do admin inicial, por exemplo, não é um CPF real
  const cpfMudou = somenteDigitos(form.cpf) !== cpfOriginal
  if (cpfMudou && !cpfValido(form.cpf)) {
    erroCpf.value = 'CPF inválido. Confira os números.'
    campoCpf.value?.focus()
    return
  }
  if (form.senha !== form.confirmar) {
    erroSenha.value = 'As senhas não conferem.'
    campoConfirmar.value?.focus()
    return
  }

  // no banco, CPF, telefone e CEP ficam só com números
  const dados = {
    nome: form.nome.trim(),
    email: form.email.trim(),
    cpf: somenteDigitos(form.cpf),
    dataNasc: form.dataNasc,
    telefone: somenteDigitos(form.telefone),
    cep: somenteDigitos(form.cep),
    endereco: form.endereco.trim(),
    tipoUser: { idTpUser: form.tipoId },
  }
  if (form.senha) {
    dados.senha = form.senha
  }

  salvando.value = true
  try {
    if (editando) {
      await atualizar(id, dados)
    } else {
      await criar(dados)
    }
    router.push({ name: 'usuarios', query: { salvo: editando ? 'editado' : 'criado' } })
  } catch (e) {
    aviso.value = e.response?.data?.message || 'Não foi possível salvar. Tente novamente.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <RouterLink :to="{ name: 'usuarios' }" class="d-inline-block mb-3">&larr; Voltar aos usuários</RouterLink>

  <h1 class="h3 mb-4">{{ editando ? 'Editar usuário' : 'Novo usuário' }}</h1>

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
          <label for="email" class="form-label">E-mail</label>
          <input id="email" v-model="form.email" type="email" class="form-control" autocomplete="off" required />
        </div>

        <div class="col-12 col-md-6">
          <label for="tipo" class="form-label">Perfil de acesso</label>
          <select id="tipo" v-model="form.tipoId" class="form-select" aria-describedby="tipo-ajuda" required>
            <option value="" disabled>Selecione um perfil</option>
            <option v-for="t in tipos" :key="t.idTpUser" :value="t.idTpUser">{{ t.nome }}</option>
          </select>
          <div id="tipo-ajuda" class="form-text">
            Uma mudança de perfil só vale para o usuário no próximo login dele.
          </div>
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
          <label for="telefone" class="form-label">Telefone</label>
          <input
            id="telefone"
            :value="form.telefone"
            type="tel"
            inputmode="numeric"
            class="form-control"
            placeholder="(00) 00000-0000"
            minlength="14"
            maxlength="15"
            required
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
          <label for="endereco" class="form-label">Endereço</label>
          <input id="endereco" v-model="form.endereco" type="text" class="form-control" required />
        </div>

        <div class="col-12 col-md-6">
          <label for="senha" class="form-label">{{ editando ? 'Nova senha' : 'Senha' }}</label>
          <input
            id="senha"
            v-model="form.senha"
            type="password"
            class="form-control"
            autocomplete="new-password"
            minlength="6"
            aria-describedby="senha-ajuda"
            :required="!editando"
          />
          <div id="senha-ajuda" class="form-text">
            {{ editando ? 'Deixe em branco para manter a senha atual.' : 'Mínimo de 6 caracteres.' }}
          </div>
        </div>

        <div class="col-12 col-md-6">
          <label for="confirmar" class="form-label">Confirmar senha</label>
          <input
            id="confirmar"
            ref="campoConfirmar"
            v-model="form.confirmar"
            type="password"
            class="form-control"
            :class="{ 'is-invalid': erroSenha }"
            :aria-invalid="!!erroSenha"
            aria-describedby="senha-erro"
            autocomplete="new-password"
            :required="!editando || !!form.senha"
          />
          <div id="senha-erro" class="invalid-feedback">{{ erroSenha }}</div>
        </div>
      </div>

      <div class="d-flex flex-wrap gap-2 mt-4">
        <button type="submit" class="btn btn-primary" :disabled="salvando">
          <span v-if="salvando" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          {{ salvando ? 'Salvando…' : 'Salvar' }}
        </button>
        <RouterLink :to="{ name: 'usuarios' }" class="btn btn-outline-secondary">Cancelar</RouterLink>
      </div>
    </div>
  </form>
</template>
