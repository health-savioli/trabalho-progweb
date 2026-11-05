package br.ufms.progweb.api.dto.integrador;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessaoResponse(Sessao sessao) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sessao(
            Long id,
            String status,
            @JsonProperty("cabine_id") String cabineId,
            String equipamento,
            @JsonProperty("expira_em") Instant expiraEm,
            @JsonProperty("encerrado_em") Instant encerradoEm) {
    }
}
