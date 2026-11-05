import http from './http'

// Busca no integrador os sinais vitais da sessão e grava na aferição.
export async function sincronizar(id) {
  const { data } = await http.post(`/afericao/${id}/sincronizar`)
  return data
}

export async function fechar(id) {
  const { data } = await http.post(`/afericao/${id}/fechar`)
  return data
}
