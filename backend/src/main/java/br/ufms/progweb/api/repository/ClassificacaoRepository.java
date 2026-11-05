package br.ufms.progweb.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.ufms.progweb.api.model.Classificacao;

public interface ClassificacaoRepository extends JpaRepository<Classificacao, Long>{

    boolean existsByPacienteIdPacienteAndStatusIn(Long idPaciente, Collection<String> status);

    List<Classificacao> findByStatusOrderByCriadoEm(String status);

    List<Classificacao> findByStatusOrderByCriadoEmDesc(String status);

    /** Ainda sem risco, esperando para serem classificados: ordem de chegada. */
    @Query("""
            select c from Classificacao c
            where c.status = 'aguardando' and c.risco is null
            order by c.criadoEm
            """)
    List<Classificacao> filaClassificacao();

    /** Já classificados e aguardando atendimento: mais urgente primeiro, e por chegada dentro da mesma cor. */
    @Query("""
            select c from Classificacao c
            where c.status = 'aguardando' and c.risco is not null
            order by case c.risco
                when 'vermelho' then 1
                when 'laranja' then 2
                when 'amarelo' then 3
                when 'verde' then 4
                else 5 end,
            c.criadoEm
            """)
    List<Classificacao> filaAtendimento();
}
