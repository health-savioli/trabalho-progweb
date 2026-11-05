export const somenteDigitos = (valor) => String(valor ?? '').replace(/\D/g, '')

// 12345678901 -> 123.456.789-01
export function mascararCpf(valor) {
  return somenteDigitos(valor)
    .slice(0, 11)
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1-$2')
}

// 67912345678 -> (67) 91234-5678
export function mascararTelefone(valor) {
  const d = somenteDigitos(valor).slice(0, 11)
  if (d.length <= 2) return d
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
}

// 79000000 -> 79000-000
export function mascararCep(valor) {
  const d = somenteDigitos(valor).slice(0, 8)
  return d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d
}

// Confere os dois dígitos verificadores do CPF.
export function cpfValido(cpf) {
  const d = somenteDigitos(cpf)
  if (d.length !== 11 || /^(\d)\1{10}$/.test(d)) return false

  const digito = (tamanho) => {
    let soma = 0
    for (let i = 0; i < tamanho; i++) {
      soma += Number(d[i]) * (tamanho + 1 - i)
    }
    const resto = (soma * 10) % 11
    return resto === 10 ? 0 : resto
  }
  return digito(9) === Number(d[9]) && digito(10) === Number(d[10])
}

// Recebe 'AAAA-MM-DD' (valor de <input type="date">) e devolve a idade em anos.
export function calcularIdade(iso) {
  const nascimento = new Date(`${iso}T00:00:00`)
  const hoje = new Date()
  const idade = hoje.getFullYear() - nascimento.getFullYear()
  const jaFezAniversario =
    hoje.getMonth() > nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() && hoje.getDate() >= nascimento.getDate())
  return jaFezAniversario ? idade : idade - 1
}
