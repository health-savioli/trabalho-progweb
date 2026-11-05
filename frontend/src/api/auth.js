import http from './http'

export async function login(email, senha){
    const { data } = await http.post('/auth/login', {email, senha})
    return data
}