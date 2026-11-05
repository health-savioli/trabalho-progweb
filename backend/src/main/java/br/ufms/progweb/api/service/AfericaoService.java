package br.ufms.progweb.api.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.ufms.progweb.api.client.IntegradorClient;
import br.ufms.progweb.api.dto.integrador.AbrirSessaoRequest;
import br.ufms.progweb.api.dto.integrador.CadastrarPacienteRequest;
import br.ufms.progweb.api.dto.integrador.ResultadoResponse;
import br.ufms.progweb.api.dto.integrador.SessaoResponse;
import br.ufms.progweb.api.model.Afericao;
import br.ufms.progweb.api.model.Paciente;
import br.ufms.progweb.api.model.User;
import br.ufms.progweb.api.repository.AfericaoRepository;
import br.ufms.progweb.api.repository.UserRepository;

@Service
public class AfericaoService {

    private final AfericaoRepository repository;
    private final PacienteService pacienteService;
    private final UserRepository userRepository;
    private final IntegradorClient integrador;

    private final String cabineId;
    private final String equipamento;
    private final Integer duracaoSegundos;

    public AfericaoService(AfericaoRepository repository,
                           PacienteService pacienteService,
                           UserRepository userRepository,
                           IntegradorClient integrador,
                           @Value("${app.integrador.cabine-id}") String cabineId,
                           @Value("${app.integrador.equipamento}") String equipamento,
                           @Value("${app.integrador.duracao-segundos}") Integer duracaoSegundos) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.userRepository = userRepository;
        this.integrador = integrador;
        this.cabineId = cabineId;
        this.equipamento = equipamento;
        this.duracaoSegundos = duracaoSegundos;
    }

    @Transactional(readOnly = true)
    public List<Afericao> listar(){
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Afericao> listarPorPaciente(Long pacienteId){
        return repository.findByPaciente(pacienteService.buscar(pacienteId));
    }

    @Transactional(readOnly = true)
    public Afericao buscar(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aferição não encontrada"));
    }

    /**
     * Abre a sessão de coleta no integrador e registra a aferição. Sem
     * @Transactional de propósito: não segura conexão do banco durante a chamada HTTP.
     */
    public Afericao iniciar(Long pacienteId, String emailUsuario){
        Paciente paciente = pacienteService.buscar(pacienteId);
        User user = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        String cpf = paciente.getCpf().replaceAll("\\D", "");
        garantirPacienteNoIntegrador(paciente, cpf);

        String atendimentoId = "atendimento-" + UUID.randomUUID();

        SessaoResponse resposta = integrador.abrirSessao(new AbrirSessaoRequest(
                atendimentoId,
                "paciente-" + paciente.getIdPaciente(),
                cpf,
                cabineId,
                equipamento,
                Instant.now(),
                duracaoSegundos));

        if (resposta == null || resposta.sessao() == null || resposta.sessao().id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Integrador não retornou a sessão");
        }

        Afericao afericao = new Afericao(paciente, user, atendimentoId,
                statusOu(resposta.sessao(), "aberta"));
        afericao.setSessaoId(resposta.sessao().id());
        return repository.save(afericao);
    }

    /** Busca no integrador o status da sessão e, se já houver resultado, grava os sinais vitais. */
    public Afericao sincronizar(Long id){
        Afericao afericao = buscar(id);
        ResultadoResponse resposta = integrador.buscarResultados(sessaoIdDe(afericao));

        if (resposta.sessao() != null) {
            afericao.setStatus(statusOu(resposta.sessao(), afericao.getStatus()));
        }

        if (resposta.resultados() != null) {
            resposta.resultados().stream()
                    .max(Comparator.comparing(ResultadoResponse.Resultado::recebidoEm,
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .ifPresent(r -> {
                        afericao.setTemperatura(r.temperatura());
                        afericao.setSaturacaoO2(r.saturacaoO2());
                        afericao.setFrequenciaCardiaca(r.frequenciaCardiaca());
                    });
        }

        return repository.save(afericao);
    }

    public Afericao fechar(Long id){
        Afericao afericao = buscar(id);
        Long sessaoId = sessaoIdDe(afericao);

        integrador.fecharSessao(sessaoId);

        SessaoResponse resposta = integrador.consultarSessao(sessaoId);
        if (resposta != null && resposta.sessao() != null) {
            afericao.setStatus(statusOu(resposta.sessao(), afericao.getStatus()));
        }
        return repository.save(afericao);
    }

    /** Cadastra o paciente no integrador só se o CPF ainda não existir lá. */
    private void garantirPacienteNoIntegrador(Paciente paciente, String cpf){
        if (!integrador.buscarPacientesPorCpf(cpf).isEmpty()) {
            return;
        }

        if (paciente.getSexo() == null || paciente.getDataNasc() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Paciente precisa ter sexo e data de nascimento para ser cadastrado no integrador");
        }

        integrador.cadastrarPaciente(new CadastrarPacienteRequest(
                paciente.getNome(),
                paciente.getSexo().equals("M") ? "Male" : "Female",
                paciente.getDataNasc(),
                cpf));
    }

    private Long sessaoIdDe(Afericao afericao){
        if (afericao.getSessaoId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aferição sem sessão no integrador");
        }
        return afericao.getSessaoId();
    }

    private String statusOu(SessaoResponse.Sessao sessao, String padrao){
        return sessao.status() != null ? sessao.status() : padrao;
    }
}
