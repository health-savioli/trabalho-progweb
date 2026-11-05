package br.ufms.progweb.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.ufms.progweb.api.model.Paciente;

import br.ufms.progweb.api.model.Afericao;
import java.util.List;
import java.util.Optional;

public interface AfericaoRepository extends JpaRepository<Afericao, Long>{
    
    Optional<Afericao> findBySessaoId(Long sessaoId);
    List<Afericao> findByPaciente(Paciente paciente);
}