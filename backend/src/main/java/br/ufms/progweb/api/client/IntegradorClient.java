package br.ufms.progweb.api.client;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import br.ufms.progweb.api.dto.integrador.AbrirSessaoRequest;
import br.ufms.progweb.api.dto.integrador.CadastrarPacienteRequest;
import br.ufms.progweb.api.dto.integrador.PacienteIntegradorResponse;
import br.ufms.progweb.api.dto.integrador.ResultadoResponse;
import br.ufms.progweb.api.dto.integrador.SessaoResponse;

/**
 * Chamadas à API do integrador (sessões de coleta). Só conhece HTTP: não toca
 * no banco. Falhas do serviço externo viram 502/504 para o chamador.
 */
@Component
public class IntegradorClient {

    private static final Logger log = LoggerFactory.getLogger(IntegradorClient.class);

    private final RestClient http;

    public IntegradorClient(@Value("${app.integrador.url}") String url,
                            @Value("${app.integrador.token}") String token) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(20));

        this.http = RestClient.builder()
                .requestFactory(fabrica)
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<PacienteIntegradorResponse> buscarPacientesPorCpf(String cpf) {
        return executar(() -> http.get()
                .uri("/ilhealth/pacientes?cpf={cpf}", cpf)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PacienteIntegradorResponse>>() {}));
    }

    public void cadastrarPaciente(CadastrarPacienteRequest request) {
        executar(() -> http.post()
                .uri("/ilhealth/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity());
    }

    public SessaoResponse abrirSessao(AbrirSessaoRequest request) {
        return executar(() -> http.post()
                .uri("/sessoes-coleta")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SessaoResponse.class));
    }

    public SessaoResponse consultarSessao(Long sessaoId) {
        return executar(() -> http.get()
                .uri("/sessoes-coleta/{id}", sessaoId)
                .retrieve()
                .body(SessaoResponse.class));
    }

    public ResultadoResponse buscarResultados(Long sessaoId) {
        return executar(() -> http.get()
                .uri("/sessoes-coleta/{id}/resultados", sessaoId)
                .retrieve()
                .body(ResultadoResponse.class));
    }

    public void fecharSessao(Long sessaoId) {
        executar(() -> http.post()
                .uri("/sessoes-coleta/{id}/fechar", sessaoId)
                .retrieve()
                .toBodilessEntity());
    }

    private <T> T executar(Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (RestClientResponseException e) {
            log.warn("Integrador respondeu {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Integrador respondeu " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.warn("Integrador inacessível: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Integrador indisponível");
        }
    }
}
