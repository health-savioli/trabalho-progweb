package br.ufms.progweb.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.ufms.progweb.api.model.TipoUser;
import br.ufms.progweb.api.repository.TipoUserRepository;

@Service
public class TipoUserService {

    private final TipoUserRepository repository;

    public TipoUserService(TipoUserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TipoUser> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public TipoUser buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<String> permissoes(Long id){
        return buscar(id).permissoes();
    }
}
