import { ref } from 'vue'
import { buscarCep } from '../api/cep'
import { somenteDigitos } from '../utils/mascaras'

// Consulta o CEP assim que ele fica completo e guarda a mensagem para a pessoa.
// A consulta nunca bloqueia o cadastro: se o CEP não for achado ou a API estiver fora do ar,
// a pessoa preenche o endereço na mão.
export function useCep() {
  const statusCep = ref('')
  const cepComProblema = ref(false)
  let sequencia = 0 // descarta respostas antigas quando o CEP muda no meio da consulta

  // Devolve o endereço encontrado, ou null (CEP incompleto, inexistente, API fora do ar).
  async function consultarCep(cep) {
    const minha = ++sequencia
    statusCep.value = ''
    cepComProblema.value = false

    const digitos = somenteDigitos(cep)
    if (digitos.length !== 8) return null

    statusCep.value = 'Buscando endereço…'
    try {
      const endereco = await buscarCep(digitos)
      if (minha !== sequencia) return null

      if (!endereco) {
        statusCep.value = 'CEP não encontrado. Confira os números ou preencha o endereço manualmente.'
        cepComProblema.value = true
        return null
      }
      statusCep.value = 'CEP encontrado. Confira o endereço e complete com o número.'
      return endereco
    } catch {
      if (minha !== sequencia) return null
      statusCep.value = 'Não foi possível consultar o CEP agora. Preencha o endereço manualmente.'
      cepComProblema.value = true
      return null
    }
  }

  return { statusCep, cepComProblema, consultarCep }
}
