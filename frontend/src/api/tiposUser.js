import http from './http'

// Perfis de acesso (Admin, Profissional...), cada um com seu conjunto de permissões.
export async function listar() {
  const { data } = await http.get('/tipos-user')
  return data
}
