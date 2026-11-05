import http from './http'

export async function listar() {
  const { data } = await http.get('/paciente')
  return data
}

// Procura por parte do nome ou do CPF (com ou sem máscara).
export async function pesquisar(termo) {
  const { data } = await http.get('/paciente/busca', { params: { termo } })
  return data
}

export async function buscar(id) {
  const { data } = await http.get(`/paciente/${id}`)
  return data
}

export async function criar(paciente) {
  const { data } = await http.post('/paciente', paciente)
  return data
}

export async function atualizar(id, paciente) {
  const { data } = await http.put(`/paciente/${id}`, paciente)
  return data
}

export async function excluir(id) {
  await http.delete(`/paciente/${id}`)
}
