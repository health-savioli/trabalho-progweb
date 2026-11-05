package br.ufms.progweb.api.dto.integrador;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultadoResponse(SessaoResponse.Sessao sessao, List<Resultado> resultados) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resultado(
            @JsonProperty("recebido_em") Instant recebidoEm,
            BigDecimal temperatura,
            @JsonProperty("saturacao_o2") Integer saturacaoO2,
            @JsonProperty("frequencia_cardiaca") Integer frequenciaCardiaca) {
    }
}
