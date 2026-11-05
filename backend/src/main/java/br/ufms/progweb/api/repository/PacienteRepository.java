package br.ufms.progweb.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ufms.progweb.api.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long>{
    Optional<Paciente> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailAndIdPacienteNot(String email, Long idPaciente);

    boolean existsByCpfAndIdPacienteNot(String cpf, Long idPaciente);

    /**
     * Busca por parte do nome ou por parte do CPF. O CPF é salvo com máscara, então a
     * comparação tira pontos e traço antes; digitos vazio desliga a busca por CPF.
     */
    @Query("""
            select p from Paciente p
            where lower(p.nome) like lower(concat('%', :texto, '%'))
               or (:digitos <> '' and replace(replace(p.cpf, '.', ''), '-', '') like concat('%', :digitos, '%'))
            order by p.nome
            """)
    List<Paciente> pesquisar(@Param("texto") String texto, @Param("digitos") String digitos);
}
