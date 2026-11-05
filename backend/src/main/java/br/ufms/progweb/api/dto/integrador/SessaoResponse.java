package br.ufms.progweb.api.dto.integrador;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessaoResponse(Sessao sessao) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sessao(Long id, String status) {
    }
}
