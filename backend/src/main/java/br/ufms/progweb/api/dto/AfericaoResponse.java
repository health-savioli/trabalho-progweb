package br.ufms.progweb.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.ufms.progweb.api.model.Afericao;

public record AfericaoResponse(
        Long idAfericao,
        Long pacienteId,
        String pacienteNome,
        Long userId,
        String userNome,
        Long sessaoId,
        String atendimentoId,
        String status,
        BigDecimal temperatura,
        Integer saturacaoO2,
        Integer frequenciaCardiaca,
        LocalDateTime criadoEm) {

    public static AfericaoResponse de(Afericao a) {
        return new AfericaoResponse(
                a.getIdAfericao(),
                a.getPaciente().getIdPaciente(),
                a.getPaciente().getNome(),
                a.getUser().getIdUser(),
                a.getUser().getNome(),
                a.getSessaoId(),
                a.getAtendimentoId(),
                a.getStatus(),
                a.getTemperatura(),
                a.getSaturacaoO2(),
                a.getFrequenciaCardiaca(),
                a.getCriadoEm());
    }
}
