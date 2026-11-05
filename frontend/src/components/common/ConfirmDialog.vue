<script setup>
import { ref } from 'vue'

// Janela de confirmação. Usa o <dialog> nativo: o navegador prende o foco dentro
// dela e fecha com Esc. Quem usa chama abrir()/fechar() pela ref.
defineProps({
  titulo: { type: String, required: true },
  confirmarTexto: { type: String, default: 'Excluir' },
  erro: { type: String, default: '' },
  ocupado: { type: Boolean, default: false },
})
const emit = defineEmits(['confirmar', 'fechou'])

const dialogo = ref(null)
const idTitulo = `dialogo-${Math.random().toString(36).slice(2)}`

defineExpose({
  abrir: () => dialogo.value.showModal(),
  fechar: () => dialogo.value.close(),
})
</script>

<template>
  <dialog
    ref="dialogo"
    class="border-0 rounded shadow p-0"
    style="width: calc(100% - 2rem); max-width: 28rem"
    :aria-labelledby="idTitulo"
    @close="emit('fechou')"
  >
    <div class="p-4">
      <h2 :id="idTitulo" class="h5">{{ titulo }}</h2>

      <slot />

      <div v-if="erro" class="alert alert-danger" role="alert">{{ erro }}</div>

      <div class="d-flex justify-content-end gap-2">
        <!-- o foco começa em Cancelar, a opção segura -->
        <button type="button" class="btn btn-outline-secondary" autofocus :disabled="ocupado" @click="dialogo.close()">
          Cancelar
        </button>
        <button type="button" class="btn btn-danger" :disabled="ocupado" @click="emit('confirmar')">
          <span v-if="ocupado" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
          {{ confirmarTexto }}
        </button>
      </div>
    </div>
  </dialog>
</template>
