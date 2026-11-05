import http from './http'

export async function listar() {
  const { data } = await http.get('/user')
  return data
}

export async function buscar(id) {
  const { data } = await http.get(`/user/${id}`)
  return data
}

export async function criar(user) {
  const { data } = await http.post('/user', user)
  return data
}

// Se `senha` não for enviada, o backend mantém a senha atual.
export async function atualizar(id, user) {
  const { data } = await http.put(`/user/${id}`, user)
  return data
}

export async function excluir(id) {
  await http.delete(`/user/${id}`)
}
