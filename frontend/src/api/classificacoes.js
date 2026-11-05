import http from "./http";

// Sem `status` traz todas; com ele ('finalizada', 'aguardando'...), só as desse status.
export async function listar(status) {
    const { data } = await http.get('/classificacao', { params: status ? { status } : {} })
    return data
}

export async function buscar(id){
    const { data } = await http.get(`/classificacao/${id}`)
    return data
}

export async function filaClassificacao() {
    const { data } = await http.get('/classificacao/fila/classificacao')
    return data
}

export async function filaAtendimento() {
    const { data } = await http.get('/classificacao/fila/atendimento')
    return data
}

export async function filaEmAtendimento() {
    const { data } = await http.get('/classificacao/fila/em-atendimento')
    return data
}

// Chama o paciente para o atendimento (aguardando -> em_atendimento).
export async function atender(id) {
    const { data } = await http.post(`/classificacao/${id}/atender`)
    return data
}

// Encerra o atendimento (em_atendimento -> finalizada).
export async function finalizar(id) {
    const { data } = await http.post(`/classificacao/${id}/finalizar`)
    return data
}

export async function criar(pacienteId) {
    const { data } = await http.post('/classificacao', { pacienteId })
    return data
}

// A queixa (o que o paciente relata) é obrigatória, tem até 500 caracteres e só se registra uma vez.
export async function registrarQueixa(id, queixa) {
    const { data } = await http.put(`/classificacao/${id}/queixa`, { queixa })
    return data
}

export async function iniciarAfericao(id) {
    const { data } = await http.post(`/classificacao/${id}/afericao`)
    return data
}

export async function atualizar(id, dados) {
  const { data } = await http.put(`/classificacao/${id}`, dados)
  return data
}

export async function excluir(id) {
  await http.delete(`/classificacao/${id}`)
}