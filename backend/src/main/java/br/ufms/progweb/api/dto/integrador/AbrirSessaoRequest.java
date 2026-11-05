package br.ufms.progweb.api.dto.integrador;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AbrirSessaoRequest(
        @JsonProperty("atendimento_id") String atendimentoId,
        @JsonProperty("paciente_id") String pacienteId,
        String cpf,
        @JsonProperty("cabine_id") String cabineId,
        String equipamento,
        @JsonProperty("iniciado_em") Instant iniciadoEm,
        @JsonProperty("duracao_segundos") Integer duracaoSegundos) {

}
