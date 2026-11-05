package br.ufms.progweb.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class IntegradorConflitoException extends ResponseStatusException {

    public IntegradorConflitoException(String motivo) {
        super(HttpStatus.BAD_GATEWAY, motivo);
    }
}
