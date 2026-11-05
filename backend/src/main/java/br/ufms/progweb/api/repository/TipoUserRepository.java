package br.ufms.progweb.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufms.progweb.api.model.TipoUser;

public interface TipoUserRepository extends JpaRepository<TipoUser, Long> {

    Optional<TipoUser> findByNome(String nome);
}
