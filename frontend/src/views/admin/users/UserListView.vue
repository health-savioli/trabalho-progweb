<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listar, excluir } from '../../../api/users'
import { useAuthStore } from '../../../stores/auth'
import { mascararTelefone } from '../../../utils/mascaras'
import ConfirmDialog from '../../../components/common/ConfirmDialog.vue'

const route = useRoute()
const auth = useAuthStore()

const usuarios = ref([])
const carregando = ref(true)
const erro = ref('')

// mensagem de sucesso vinda do formulário (?salvo=criado|editado) ou de uma exclusão
const mensagens = { criado: 'Usuário cadastrado com sucesso.', editado: 'Usuário atualizado com sucesso.' }
const sucesso = ref(mensagens[route.query.salvo] ?? '')

const dialogo = ref(null)
const alvo = ref(null)
const excluindo = ref(false)
const erroExclusao = ref('')

async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    usuarios.value = await listar()
  } catch (e) {
    erro.value = e.response?.data?.message || 'Não foi possível carregar os usuários.'
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

function pedirConfirmacao(usuario) {
  alvo.value = usuario
  erroExclusao.value = ''
  dialogo.value.abrir()
}

async function confirmarExclusao() {
  excluindo.value = true
  erroExclusao.value = ''
  try {
    await excluir(alvo.value.idUser)
    usuarios.value = usuarios.value.filter((u) => u.idUser !== alvo.value.idUser)
    sucesso.value = `Usuário ${alvo.value.nome} excluído.`
    dialogo.value.fechar()
  } catch (e) {
    erroExclusao.value = e.response?.data?.message || 'Não foi possível excluir o usuário.'
  } finally {
    excluindo.value = false
  }
}
</script>

<template>
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-4">
    <h1 class="h3 mb-0">Usuários</h1>

    <RouterLink v-if="auth.can('cadastrar_user')" :to="{ name: 'usuario-novo' }" class="btn btn-primary">
      Novo usuário
    </RouterLink>
  </div>

  <div v-if="sucesso" class="alert alert-success" role="status">{{ sucesso }}</div>

  <p v-if="carregando" role="status" class="text-center py-5 mb-0">
    <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Carregando…
  </p>

  <div v-else-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

  <div v-else-if="usuarios.length === 0" class="card shadow-sm">
    <div class="card-body text-center py-5">
      <p class="mb-0">Nenhum usuário cadastrado.</p>
    </div>
  </div>

  <div v-else class="card shadow-sm">
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <caption class="visually-hidden">Lista de usuários do sistema</caption>
        <thead class="table-light">
          <tr>
            <th scope="col">Nome</th>
            <th scope="col">E-mail</th>
            <th scope="col">Perfil</th>
            <th scope="col">Telefone</th>
            <th scope="col" class="text-end">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in usuarios" :key="u.idUser">
            <td class="fw-semibold">
              {{ u.nome }}
              <span v-if="u.email === auth.email" class="badge text-bg-secondary ms-1">Você</span>
            </td>
            <td>{{ u.email }}</td>
            <td><span class="badge text-bg-light border">{{ u.tipoUser?.nome }}</span></td>
            <td>{{ mascararTelefone(u.telefone) }}</td>
            <td>
              <div class="d-flex justify-content-end gap-2">
                <RouterLink
                  v-if="auth.can('editar_user')"
                  :to="{ name: 'usuario-editar', params: { id: u.idUser } }"
                  class="btn btn-outline-secondary btn-sm"
                  :aria-label="`Editar ${u.nome}`"
                >
                  Editar
                </RouterLink>
                <button
                  v-if="auth.can('excluir_user') && u.email !== auth.email"
                  type="button"
                  class="btn btn-outline-danger btn-sm"
                  :aria-label="`Excluir ${u.nome}`"
                  @click="pedirConfirmacao(u)"
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
    titulo="Excluir usuário"
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
