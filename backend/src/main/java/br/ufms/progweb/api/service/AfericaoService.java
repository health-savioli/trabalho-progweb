package br.ufms.progweb.api.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import br.ufms.progweb.api.exception.IntegradorConflitoException;
import br.ufms.progweb.api.model.Afericao;
import br.ufms.progweb.api.model.Paciente;
import br.ufms.progweb.api.model.User;
import br.ufms.progweb.api.repository.AfericaoRepository;
import br.ufms.progweb.api.repository.UserRepository;

@Service
public class AfericaoService {

    private static final Logger log = LoggerFactory.getLogger(AfericaoService.class);
    private static final int LIMITE_BUSCA_SESSOES_SEM_REGISTRO = 20;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("America/Campo_Grande"));

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

        Instant ocupadaAte = encerrarSessoesAbertas();

        String atendimentoId = "atendimento-" + UUID.randomUUID();

        SessaoResponse resposta = abrirSessao(new AbrirSessaoRequest(
                atendimentoId,
                "paciente-" + paciente.getIdPaciente(),
                cpf,
                cabineId,
                equipamento,
                Instant.now(),
                duracaoSegundos), ocupadaAte);

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

    private SessaoResponse abrirSessao(AbrirSessaoRequest pedido, Instant ocupadaAte){
        try {
            return integrador.abrirSessao(pedido);
        } catch (IntegradorConflitoException e) {
            log.warn("Cabine ocupada no integrador; procurando sessões abertas sem registro no banco");
            Instant semRegistro = encerrarSessoesSemRegistro();
            try {
                return integrador.abrirSessao(pedido);
            } catch (IntegradorConflitoException segunda) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        mensagemCabineOcupada(maior(ocupadaAte, semRegistro)));
            }
        }
    }

    /**
     * Cuida das sessões deste sistema que ainda constam no integrador. A aberta de verdade é fechada,
     * guardando antes os resultados. A "concluida" sem encerramento o integrador não deixa fechar:
     * só expira, então devolvemos a hora em que a cabine é liberada.
     */
    private Instant encerrarSessoesAbertas(){
        Instant ocupadaAte = null;
        LocalDateTime desde = LocalDateTime.now().minusHours(2);

        for (Afericao a : repository.findBySessaoIdIsNotNullAndStatusNotAndCriadoEmAfter("encerrada", desde)) {
            try {
                SessaoResponse resposta = integrador.consultarSessao(a.getSessaoId());
                if (resposta == null || resposta.sessao() == null) {
                    continue;
                }
                SessaoResponse.Sessao s = resposta.sessao();
                boolean encerrada = s.encerradoEm() != null;
                boolean concluida = "concluida".equalsIgnoreCase(s.status());

                if (!encerrada && !concluida) {
                    log.warn("Sessão {} (aferição {}) ficou aberta no integrador; encerrando", a.getSessaoId(), a.getIdAfericao());
                    sincronizar(a.getIdAfericao());
                    fechar(a.getIdAfericao());
                } else {
                    if (!encerrada && s.expiraEm() != null && s.expiraEm().isAfter(Instant.now())) {
                        ocupadaAte = maior(ocupadaAte, s.expiraEm());
                    }
                    if (a.getTemperatura() == null || !Objects.equals(a.getStatus(), s.status())) {
                        sincronizar(a.getIdAfericao());
                    }
                }
            } catch (ResponseStatusException e) {
                log.warn("Não consegui conferir a sessão {}: {}", a.getSessaoId(), e.getReason());
            }
        }
        return ocupadaAte;
    }

    /**
     * Sessões desta cabine e equipamento que o banco não conhece (abertas por outro programa ou perdidas
     * num erro): têm número maior que o da última registrada, então a busca segue a sequência até acabar.
     */
    private Instant encerrarSessoesSemRegistro(){
        Long ultima = repository.maiorSessaoId();
        if (ultima == null) {
            return null;
        }

        Instant ocupadaAte = null;
        for (long id = ultima + 1; id <= ultima + LIMITE_BUSCA_SESSOES_SEM_REGISTRO; id++) {
            try {
                SessaoResponse resposta = integrador.buscarSessaoSeExistir(id);
                if (resposta == null || resposta.sessao() == null) {
                    break;
                }
                SessaoResponse.Sessao s = resposta.sessao();
                boolean destaCabine = cabineId.equals(s.cabineId()) && equipamento.equals(s.equipamento());
                if (!destaCabine || s.encerradoEm() != null) {
                    continue;
                }

                if ("concluida".equalsIgnoreCase(s.status())) {
                    if (s.expiraEm() != null && s.expiraEm().isAfter(Instant.now())) {
                        ocupadaAte = maior(ocupadaAte, s.expiraEm());
                    }
                } else {
                    log.warn("Sessão {} está aberta nesta cabine sem registro no banco; encerrando", id);
                    integrador.fecharSessao(id);
                }
            } catch (ResponseStatusException e) {
                log.warn("Parei a busca de sessões abertas na sessão {}: {}", id, e.getReason());
                break;
            }
        }
        return ocupadaAte;
    }

    private Instant maior(Instant a, Instant b){
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private String mensagemCabineOcupada(Instant ocupadaAte){
        String quando = ocupadaAte != null && ocupadaAte.isAfter(Instant.now())
                ? "Ela é liberada às " + HORA.format(ocupadaAte) + "."
                : "Tente de novo em alguns minutos.";
        return "A cabine está com uma sessão aberta no integrador que não pode ser encerrada agora. " + quando;
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
