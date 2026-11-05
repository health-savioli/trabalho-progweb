import axios from 'axios'

export const SESSAO_KEY = 'progweb.sessao'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  try {
    const sessao = JSON.parse(localStorage.getItem(SESSAO_KEY))
    if (sessao?.token) {
      config.headers.Authorization = `Bearer ${sessao.token}`
    }
  } catch {
    // sessão corrompida: segue sem token
  }
  return config
})

http.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const ehLogin = erro.config?.url === '/auth/login'
    if (erro.response?.status === 401 && !ehLogin) {
      localStorage.removeItem(SESSAO_KEY)
      window.location.assign('/login')
    }
    return Promise.reject(erro)
  },
)

export default http


