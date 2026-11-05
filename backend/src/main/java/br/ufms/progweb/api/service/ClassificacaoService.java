package br.ufms.progweb.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.ufms.progweb.api.model.Afericao;
import br.ufms.progweb.api.model.Classificacao;
import br.ufms.progweb.api.model.Paciente;
import br.ufms.progweb.api.repository.ClassificacaoRepository;
import br.ufms.progweb.api.repository.UserRepository;

@Service
public class ClassificacaoService {

    private static final List<String> RISCOS = List.of("vermelho", "laranja", "amarelo", "verde", "azul");
    private static final List<String> STATUS = List.of("aguardando", "em_atendimento", "finalizada", "cancelada");
    private static final List<String> STATUS_ATIVOS = List.of("aguardando", "em_atendimento");
    private static final int QUEIXA_MAX = 500;

    private final ClassificacaoRepository repository;
    private final PacienteService pacienteService;
    private final AfericaoService afericaoService;
    private final UserRepository userRepository;

    public ClassificacaoService(ClassificacaoRepository repository,
                                PacienteService pacienteService,
                                AfericaoService afericaoService,
                                UserRepository userRepository) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.afericaoService = afericaoService;
        this.userRepository = userRepository;
    }

    /** Sem status lista todas; com status, só as desse status, da mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<Classificacao> listar(String status){
        if(status == null || status.isBlank()){
            return repository.findAll();
        }
        return repository.findByStatusOrderByCriadoEmDesc(valido(status, STATUS, "Status inválido. Use: " + STATUS));
    }

    @Transactional(readOnly = true)
    public List<Classificacao> filaClassificacao(){
        return repository.filaClassificacao();
    }

    @Transactional(readOnly = true)
    public List<Classificacao> filaAtendimento(){
        return repository.filaAtendimento();
    }

    /** Pacientes sendo atendidos agora, do que chegou primeiro para o último. */
    @Transactional(readOnly = true)
    public List<Classificacao> filaEmAtendimento(){
        return repository.findByStatusOrderByCriadoEm("em_atendimento");
    }

    /** Chama o paciente para o atendimento: sai da fila de atendimento e passa a "em_atendimento". */
    @Transactional
    public Classificacao atender(Long id){
        Classificacao classificacao = buscar(id);

        if(!"aguardando".equals(classificacao.getStatus()) || classificacao.getRisco() == null){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só é possível atender um paciente classificado que esteja aguardando atendimento");
        }

        classificacao.setStatus("em_atendimento");
        return repository.save(classificacao);
    }

    /** Encerra o atendimento. */
    @Transactional
    public Classificacao finalizar(Long id){
        Classificacao classificacao = buscar(id);

        if(!"em_atendimento".equals(classificacao.getStatus())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível finalizar um atendimento em andamento");
        }

        classificacao.setStatus("finalizada");
        return repository.save(classificacao);
    }

    @Transactional(readOnly = true)
    public Classificacao buscar(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classificação não encontrada"));
    }

    @Transactional
    public Classificacao criar(Long pacienteId, String queixa){
        String queixaLimpa = queixa == null ? "" : queixa.trim();
        if(queixaLimpa.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Queixa é obrigatória");
        }
        if(queixaLimpa.length() > QUEIXA_MAX){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Queixa deve ter no máximo " + QUEIXA_MAX + " caracteres");
        }

        Paciente paciente = pacienteService.buscar(pacienteId);

        if(repository.existsByPacienteIdPacienteAndStatusIn(pacienteId, STATUS_ATIVOS)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Paciente já possui classificação em andamento");
        }

        Classificacao classificacao = new Classificacao(paciente, "aguardando");
        classificacao.setQueixa(queixaLimpa);
        return repository.save(classificacao);
    }

    /** Define o risco e/ou o status. Quem define o risco fica como responsável pela classificação. */
    @Transactional
    public Classificacao atualizar(Long id, String risco, String status, String emailUsuario){
        Classificacao classificacao = buscar(id);

        if(risco != null){
            classificacao.setRisco(valido(risco, RISCOS, "Risco inválido. Use: " + RISCOS));
            classificacao.setUser(userRepository.findByEmail(emailUsuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado")));
        }

        if(status != null){
            classificacao.setStatus(valido(status, STATUS, "Status inválido. Use: " + STATUS));
        }

        return repository.save(classificacao);
    }

    /**
     * Inicia a sessão de coleta no integrador e vincula a aferição à classificação.
     * Sem @Transactional de propósito: há chamada HTTP no meio.
     */
    public Classificacao iniciarAfericao(Long id, String emailUsuario){
        Classificacao classificacao = buscar(id);

        if(classificacao.getAfericao() != null){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Classificação já possui aferição");
        }
        if(!"aguardando".equals(classificacao.getStatus())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível aferir classificação aguardando");
        }

        Afericao afericao = afericaoService.iniciar(classificacao.getPaciente().getIdPaciente(), emailUsuario);
        classificacao.setAfericao(afericao);
        return repository.save(classificacao);
    }

    @Transactional
    public void excluir(Long id){
        repository.delete(buscar(id));
    }

    private String valido(String valor, List<String> permitidos, String mensagem){
        String normalizado = valor.trim().toLowerCase();
        if(!permitidos.contains(normalizado)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return normalizado;
    }
}
