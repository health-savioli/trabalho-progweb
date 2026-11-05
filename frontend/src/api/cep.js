// ViaCEP: API pública e gratuita, que aceita chamadas direto do navegador (CORS liberado).
// Usa fetch, e não o axios de api/http.js, para o token de login não ser enviado a um serviço externo.
// Devolve o endereço montado ("Rua, Bairro, Cidade/UF") ou null se o CEP não existir.
export async function buscarCep(cep) {
  const resposta = await fetch(`https://viacep.com.br/ws/${cep}/json/`)
  if (!resposta.ok) {
    throw new Error(`ViaCEP respondeu ${resposta.status}`)
  }

  const dados = await resposta.json()
  if (dados.erro) {
    return null
  }

  const cidade = [dados.localidade, dados.uf].filter(Boolean).join('/')
  return [dados.logradouro, dados.bairro, cidade].filter(Boolean).join(', ')
}
