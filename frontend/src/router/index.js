import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/auth/LoginView.vue'
import ForbiddenView from '../views/errors/ForbiddenView.vue'
import NotFoundView from '../views/errors/NotFoundView.vue'
import { useAuthStore } from '../stores/auth'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import ClassificacaoListView from '../views/classificacoes/ClassificacaoListView.vue'
import FilasView from '../views/classificacoes/FilasView.vue'
import ClassificarView from '../views/classificacoes/ClassificarView.vue'
import AdicionarFilaView from '../views/classificacoes/AdicionarFilaView.vue'
import PacienteListView from '../views/pacientes/PacienteListView.vue'
import PacienteFormView from '../views/pacientes/PacienteFormView.vue'
import UserListView from '../views/admin/users/UserListView.vue'
import UserFormView from '../views/admin/users/UserFormView.vue'

const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { publica: true } },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        // página inicial: Filas, ou a primeira tela que a pessoa tem permissão de ver
        path: '',
        name: 'home',
        redirect: () => {
          const auth = useAuthStore()
          if (auth.can('visualizar_classificacao')) return { name: 'filas' }
          if (auth.can('visualizar_paciente')) return { name: 'pacientes' }
          if (auth.can('visualizar_user')) return { name: 'usuarios' }
          return { name: 'forbidden' }
        },
      },
      { path: 'acesso-negado', name: 'forbidden', component: ForbiddenView },
      {
        path: 'filas',
        name: 'filas',
        component: FilasView,
        meta: { permissao: 'visualizar_classificacao' },
      },
      {
      path: 'classificacoes',
      name: 'classificacoes',
      component: ClassificacaoListView,
      meta: { permissao: 'visualizar_classificacao' },
      },
      {
        path: 'filas/adicionar',
        name: 'filas-adicionar',
        component: AdicionarFilaView,
        meta: { permissao: 'cadastrar_classificacao' },
      },
      {
        path: 'classificacoes/:id/classificar',
        name: 'classificar',
        component: ClassificarView,
        meta: { permissao: 'editar_classificacao' },
      },
      {
        path: 'pacientes',
        name: 'pacientes',
        component: PacienteListView,
        meta: { permissao: 'visualizar_paciente' },
      },
      {
        path: 'pacientes/novo',
        name: 'paciente-novo',
        component: PacienteFormView,
        meta: { permissao: 'cadastrar_paciente' },
      },
      {
        path: 'pacientes/:id/editar',
        name: 'paciente-editar',
        component: PacienteFormView,
        meta: { permissao: 'editar_paciente' },
      },
      {
        path: 'usuarios',
        name: 'usuarios',
        component: UserListView,
        meta: { permissao: 'visualizar_user' },
      },
      {
        path: 'usuarios/novo',
        name: 'usuario-novo',
        component: UserFormView,
        meta: { permissao: 'cadastrar_user' },
      },
      {
        path: 'usuarios/:id/editar',
        name: 'usuario-editar',
        component: UserFormView,
        meta: { permissao: 'editar_user' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView, meta: { publica: true } },
]


const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.name === 'login' && auth.logado) {
    return { name: 'home' }
  }
  if (!to.meta.publica && !auth.logado) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.permissao && !auth.can(to.meta.permissao)) {
    return { name: 'forbidden' }
  }

  
})




export default router
