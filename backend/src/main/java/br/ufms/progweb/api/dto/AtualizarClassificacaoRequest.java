package br.ufms.progweb.api.dto;

/** Campos nulos não são alterados. */
public record AtualizarClassificacaoRequest(String risco, String status) {

}
