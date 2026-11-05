package br.ufms.progweb.api.dto.integrador;

import java.time.LocalDate;

public record CadastrarPacienteRequest(
        String fullName,
        String gender,
        LocalDate birthday,
        String rgOrCpf) {

}
