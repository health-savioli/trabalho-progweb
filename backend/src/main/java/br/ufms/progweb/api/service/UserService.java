package br.ufms.progweb.api.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.ufms.progweb.api.model.TipoUser;
import br.ufms.progweb.api.model.User;
import br.ufms.progweb.api.repository.TipoUserRepository;
import br.ufms.progweb.api.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;
    private final TipoUserRepository tipoUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, TipoUserRepository tipoUserRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.tipoUserRepository = tipoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public User buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @Transactional
    public User criar(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        if (repository.existsByCpf(user.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }
        if (user.getSenha() == null || user.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha é obrigatória");
        }

        user.setTipoUser(buscarTipo(user.getTipoUser()));
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        return repository.save(user);
    }

    @Transactional
    public User atualizar(Long id, User dados) {
        User existente = buscar(id);

        if (repository.existsByEmailAndIdUserNot(dados.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        if (repository.existsByCpfAndIdUserNot(dados.getCpf(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }

        existente.setTipoUser(buscarTipo(dados.getTipoUser()));
        existente.setNome(dados.getNome());
        existente.setTelefone(dados.getTelefone());
        existente.setEmail(dados.getEmail());
        existente.setCpf(dados.getCpf());
        existente.setDataNasc(dados.getDataNasc());
        existente.setCep(dados.getCep());
        existente.setEndereco(dados.getEndereco());

        // senha vazia = mantém a atual
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        return repository.save(existente);
    }

    /** Não deixa o usuário excluir a si mesmo nem quem já tem classificações/aferições vinculadas. */
    @Transactional
    public void excluir(Long id, String emailLogado) {
        User user = buscar(id);

        if (user.getEmail().equalsIgnoreCase(emailLogado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você não pode excluir o seu próprio usuário");
        }

        try {
            repository.delete(user);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este usuário tem classificações ou aferições vinculadas e não pode ser excluído");
        }
    }

    private TipoUser buscarTipo(TipoUser tipo) {
        if (tipo == null || tipo.getIdTpUser() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de usuário é obrigatório");
        }
        return tipoUserRepository.findById(tipo.getIdTpUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de usuário inexistente"));
    }
}
