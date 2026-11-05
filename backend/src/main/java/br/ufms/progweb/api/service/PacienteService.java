package br.ufms.progweb.api.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.ufms.progweb.api.model.Paciente;
import br.ufms.progweb.api.repository.PacienteRepository;

@Service
public class PacienteService{

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository){
        this.repository = repository;
    }


    @Transactional(readOnly = true)
    public List<Paciente> listar(){
        return repository.findAll();
    }

    /** Procura por nome ou CPF (com ou sem máscara). Termo vazio devolve lista vazia. */
    @Transactional(readOnly = true)
    public List<Paciente> pesquisar(String termo){
        // % e _ são curingas do LIKE: tirados para o termo ser sempre literal
        String texto = termo == null ? "" : termo.replaceAll("[%_\\\\]", "").trim();
        if(texto.isEmpty()){
            return List.of();
        }
        // só procura por CPF se o termo parece um CPF; "Progweb3" não pode casar com o dígito 3
        String digitos = texto.matches("[\\d.\\-\\s]+") ? texto.replaceAll("\\D", "") : "";
        return repository.pesquisar(texto, digitos);
    }

    @Transactional(readOnly = true)
    public Paciente buscar(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado"));

    }

    @Transactional
    public Paciente criar(Paciente paciente){
        validarSexo(paciente.getSexo());
        validarCep(paciente.getCep());

        if(repository.existsByEmail(paciente.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        if(repository.existsByCpf(paciente.getCpf())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cpf já cadastrado");
        }

        return repository.save(paciente);
    }

    @Transactional
    public Paciente atualizar(Long id, Paciente dados){
        Paciente existente = buscar(id);
        validarSexo(dados.getSexo());
        validarCep(dados.getCep());

        if(repository.existsByEmailAndIdPacienteNot(dados.getEmail(), id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        if(repository.existsByCpfAndIdPacienteNot(dados.getCpf(), id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }

        existente.setNome(dados.getNome());
        existente.setTelefone(dados.getTelefone());
        existente.setEmail(dados.getEmail());
        existente.setCpf(dados.getCpf());
        existente.setDataNasc(dados.getDataNasc());
        existente.setCep(dados.getCep());
        existente.setEndereco(dados.getEndereco());
        existente.setIdade(dados.getIdade());
        existente.setSexo(dados.getSexo());
        return repository.save(existente);
    }

    private void validarCep(String cep){
        if(cep == null || cep.replaceAll("\\D", "").length() != 8){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP é obrigatório e deve ter 8 números");
        }
    }

    private void validarSexo(String sexo){
        if(sexo != null && !sexo.equals("M") && !sexo.equals("F")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sexo deve ser M ou F");
        }
    }

    /** Paciente com classificações ou aferições vinculadas não pode ser excluído. */
    @Transactional
    public void excluir(Long id) {
        Paciente paciente = buscar(id);
        try {
            repository.delete(paciente);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este paciente tem classificações ou aferições vinculadas e não pode ser excluído");
        }
    }
}


