package br.ufms.progweb.api.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Devolve o motivo das ResponseStatusException no corpo da resposta ({"status", "message"}),
 * para o front mostrar mensagens como "Cpf já cadastrado". O corpo padrão do Spring Boot
 * só traz timestamp/status/error/path.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> responseStatus(ResponseStatusException e) {
        String mensagem = e.getReason() != null ? e.getReason() : "";
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", e.getStatusCode().value(), "message", mensagem));
    }
}
