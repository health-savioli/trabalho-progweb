package br.ufms.progweb.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufms.progweb.api.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailAndIdUserNot(String email, Long idUser);

    boolean existsByCpfAndIdUserNot(String cpf, Long idUser);
}
