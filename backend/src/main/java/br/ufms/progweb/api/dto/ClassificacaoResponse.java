package br.ufms.progweb.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.ufms.progweb.api.model.Afericao;
import br.ufms.progweb.api.model.Classificacao;
import br.ufms.progweb.api.model.User;

public record ClassificacaoResponse(
        Long idClassificacao,
        Long pacienteId,
        String pacienteNome,
        Long userId,
        String userNome,
        String risco,
        String status,
        String queixa,
        LocalDateTime criadoEm,
        Long afericaoId,
        String afericaoStatus,
        BigDecimal temperatura,
        Integer saturacaoO2,
        Integer frequenciaCardiaca) {

    public static ClassificacaoResponse de(Classificacao c) {
        User user = c.getUser();
        Afericao a = c.getAfericao();
        return new ClassificacaoResponse(
                c.getIdClassificacao(),
                c.getPaciente().getIdPaciente(),
                c.getPaciente().getNome(),
                user != null ? user.getIdUser() : null,
                user != null ? user.getNome() : null,
                c.getRisco(),
                c.getStatus(),
                c.getQueixa(),
                c.getCriadoEm(),
                a != null ? a.getIdAfericao() : null,
                a != null ? a.getStatus() : null,
                a != null ? a.getTemperatura() : null,
                a != null ? a.getSaturacaoO2() : null,
                a != null ? a.getFrequenciaCardiaca() : null);
    }
}
