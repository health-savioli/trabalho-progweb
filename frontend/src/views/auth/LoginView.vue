<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)

async function entrar() {
    erro.value = ''
    carregando.value = true
    try {
        await auth.login(email.value, senha.value)
        router.push(route.query.redirect || { name: 'home' })
    } catch (e) {
        erro.value = e.response?.status === 401
            ? 'Email ou senha inválidos.'
            : 'Não foi possível entrar. Tente novamente.'
    } finally {
        carregando.value = false
    }
}
</script>

<template>
    <main class="login-page">
        <form class="login-form" @submit.prevent="entrar">
            <h1 class="login-titulo">Entrar</h1>

            <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input
                    id="email"
                    v-model="email"
                    type="email"
                    class="form-control"
                    placeholder="nome@exemplo.com"
                    autocomplete="username"
                    required
                />
            </div>

            <div class="mb-3">
                <label for="senha" class="form-label">Senha</label>
                <input
                    id="senha"
                    v-model="senha"
                    type="password"
                    class="form-control"
                    autocomplete="current-password"
                    required
                />
            </div>

            <p v-if="erro" class="login-erro" role="alert">{{ erro }}</p>

            <button type="submit" class="btn btn-primary w-100" :disabled="carregando">
                {{ carregando ? 'Entrando...' : 'Entrar' }}
            </button>
        </form>
    </main>
</template>

<style scoped>
.login-page {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 16px;
}

.login-form {
    width: 100%;
    max-width: 400px;
    padding: 32px;
    background: var(--cor-superficie);
    border: 1px solid var(--cor-borda);
    border-radius: 8px;
}

.login-titulo {
    margin: 0 0 24px;
    font-size: 1.5rem;
    text-align: center;
}

.login-erro {
    margin: 0 0 16px;
    color: var(--cor-erro);
    font-size: 0.875rem;
}
</style>
