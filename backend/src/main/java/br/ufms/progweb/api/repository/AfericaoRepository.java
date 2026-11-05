package br.ufms.progweb.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.ufms.progweb.api.model.Afericao;
import br.ufms.progweb.api.model.Paciente;

public interface AfericaoRepository extends JpaRepository<Afericao, Long>{

    Optional<Afericao> findBySessaoId(Long sessaoId);
    List<Afericao> findByPaciente(Paciente paciente);

    List<Afericao> findBySessaoIdIsNotNullAndStatusNotAndCriadoEmAfter(String status, LocalDateTime desde);

    @Query("select max(a.sessaoId) from Afericao a")
    Long maiorSessaoId();
}
