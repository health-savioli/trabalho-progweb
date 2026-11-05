<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { excluir, listar, pesquisar } from '../../api/pacientes'
import { useAuthStore } from '../../stores/auth'
import { calcularIdade, mascararTelefone } from '../../utils/mascaras'
import ConfirmDialog from '../../components/common/ConfirmDialog.vue'

const route = useRoute()
const auth = useAuthStore()

const pacientes = ref([])
const carregando = ref(true) // só na primeira carga; depois a lista continua visível
const buscando = ref(false)
const erro = ref('')
const termo = ref('')
const ultimaBusca = ref('')

// mensagem de sucesso vinda do formulário (?salvo=criado|editado) ou de uma exclusão
const mensagens = { criado: 'Paciente cadastrado com sucesso.', editado: 'Paciente atualizado com sucesso.' }
const sucesso = ref(mensagens[route.query.salvo] ?? '')

const dialogo = ref(null)
const alvo = ref(null)
const excluindo = ref(false)
const erroExclusao = ref('')

// Sem texto: lista todos. Com texto: busca por nome ou CPF no backend.
// `sequencia` descarta respostas antigas que cheguem atrasadas.
let timer
let sequencia = 0

async function carregar() {
  const minha = ++sequencia
  const busca = termo.value.trim()
  buscando.value = true
  erro.value = ''
  try {
    const dados = busca ? await pesquisar(busca) : await listar()
    if (minha !== sequencia) return
    pacientes.value = dados
    ultimaBusca.value = busca
  } catch (e) {
    if (minha !== sequencia) return
    erro.value = e.response?.data?.message || 'Não foi possível carregar os pacientes.'
  } finally {
    if (minha === sequencia) {
      buscando.value = false
      carregando.value = false
    }
  }
}

onMounted(carregar)
onUnmounted(() => clearTimeout(timer))

watch(termo, () => {
  sequencia++
  clearTimeout(timer)
  buscando.value = true
  timer = setTimeout(carregar, 300)
})

function formatarData(iso) {
  return new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR')
}

function pedirConfirmacao(paciente) {
  alvo.value = paciente
  erroExclusao.value = ''
  dialogo.value.abrir()
}

async function confirmarExclusao() {
  excluindo.value = true
  erroExclusao.value = ''
  try {
    await excluir(alvo.value.idPaciente)
    pacientes.value = pacientes.value.filter((p) => p.idPaciente !== alvo.value.idPaciente)
    sucesso.value = `Paciente ${alvo.value.nome} excluído.`
    dialogo.value.fechar()
  } catch (e) {
    erroExclusao.value = e.response?.data?.message || 'Não foi possível excluir o paciente.'
  } finally {
    excluindo.value = false
  }
}
</script>

<template>
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-4">
    <h1 class="h3 mb-0">Pacientes</h1>

    <RouterLink v-if="auth.can('cadastrar_paciente')" :to="{ name: 'paciente-novo' }" class="btn btn-primary">
      Novo paciente
    </RouterLink>
  </div>

  <div v-if="sucesso" class="alert alert-success" role="status">{{ sucesso }}</div>

  <div class="mb-3" style="max-width: 28rem">
    <label for="busca" class="form-label">Buscar por nome ou CPF</label>
    <input id="busca" v-model="termo" type="search" class="form-control" autocomplete="off" />
  </div>

  <p v-if="carregando" role="status" class="text-center py-5 mb-0">
    <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Carregando…
  </p>

  <div v-else-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

  <div v-else-if="pacientes.length === 0 && !buscando" class="card shadow-sm">
    <div class="card-body text-center py-5">
      <p class="mb-0" role="status">
        {{ ultimaBusca ? `Nenhum paciente encontrado para “${ultimaBusca}”.` : 'Nenhum paciente cadastrado.' }}
      </p>
    </div>
  </div>

  <div v-else class="card shadow-sm" :aria-busy="buscando">
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <caption class="visually-hidden">Lista de pacientes</caption>
        <thead class="table-light">
          <tr>
            <th scope="col">Nome</th>
            <th scope="col">CPF</th>
            <th scope="col">Nascimento</th>
            <th scope="col">Telefone</th>
            <th scope="col" class="text-end">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pacientes" :key="p.idPaciente">
            <td class="fw-semibold">{{ p.nome }}</td>
            <td>{{ p.cpf }}</td>
            <td>
              <template v-if="p.dataNasc">
                {{ formatarData(p.dataNasc) }} ({{ calcularIdade(p.dataNasc) }} anos)
              </template>
              <template v-else>Não informado</template>
            </td>
            <td>{{ p.telefone ? mascararTelefone(p.telefone) : 'Não informado' }}</td>
            <td>
              <div class="d-flex justify-content-end gap-2">
                <RouterLink
                  v-if="auth.can('editar_paciente')"
                  :to="{ name: 'paciente-editar', params: { id: p.idPaciente } }"
                  class="btn btn-outline-secondary btn-sm"
                  :aria-label="`Editar ${p.nome}`"
                >
                  Editar
                </RouterLink>
                <button
                  v-if="auth.can('excluir_paciente')"
                  type="button"
                  class="btn btn-outline-danger btn-sm"
                  :aria-label="`Excluir ${p.nome}`"
                  @click="pedirConfirmacao(p)"
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
    titulo="Excluir paciente"
    :erro="erroExclusao"
    :ocupado="excluindo"
    @confirmar="confirmarExclusao"
    @fechou="alvo = null"
  >
    <p v-if="alvo">
      Tem certeza que deseja excluir <strong>{{ alvo.nome }}</strong>? Esta ação não pode ser desfeita.
    </p>
  </ConfirmDialog>
</template>
