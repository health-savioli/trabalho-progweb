package br.ufms.progweb.api.dto.integrador;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PacienteIntegradorResponse(Long id, String fullName, String rgOrCpf) {

}
