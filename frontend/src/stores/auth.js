import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginRequest } from '../api/auth'
import { SESSAO_KEY } from '../api/http'


function lerSessao(){
    try{
        return JSON.parse(localStorage.getItem(SESSAO_KEY)) ?? {}
    }catch{
        return{}
    }
}

export const useAuthStore = defineStore('auth', ()=>{

    const sessao = lerSessao()
    const token = ref(sessao.token ?? null)
    const nome = ref(sessao.nome ?? '')
    const permissoes = ref(sessao.permissoes ?? [])

    const logado = computed(() => !!token.value)

    // o token (JWT) guarda o e-mail de quem logou no campo "sub"
    const email = computed(() => {
        try {
            const payload = token.value.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
            return JSON.parse(atob(payload)).sub ?? ''
        } catch {
            return ''
        }
    })

    function can(permissao){
        return permissoes.value.includes(permissao)
    }

    async function login(email, senha){
        const dados = await loginRequest(email, senha)
        token.value = dados.token
        nome.value = dados.nome
        permissoes.value = dados.permissoes
        localStorage.setItem(SESSAO_KEY, JSON.stringify(dados))
    }

    function logout(){
        token.value = null
        nome.value = ''
        permissoes.value = []
        localStorage.removeItem(SESSAO_KEY)
    }

    return{token, nome, email, permissoes, logado, can, login, logout}
})