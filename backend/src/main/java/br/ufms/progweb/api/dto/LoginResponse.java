package br.ufms.progweb.api.dto;

import java.util.List;

public record LoginResponse(String token, String nome, List<String> permissoes) {
    
}
