package br.ufms.progweb.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ufms.progweb.api.dto.AtualizarClassificacaoRequest;
import br.ufms.progweb.api.dto.ClassificacaoResponse;
import br.ufms.progweb.api.dto.CriarClassificacaoRequest;
import br.ufms.progweb.api.service.ClassificacaoService;

@RestController
@RequestMapping("/api/classificacao")
public class ClassificacaoController{

    private final ClassificacaoService service;

    public ClassificacaoController(ClassificacaoService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping
    public List<ClassificacaoResponse> listar(@RequestParam(required = false) String status){
        return service.listar(status).stream().map(ClassificacaoResponse::de).toList();
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping("/fila/classificacao")
    public List<ClassificacaoResponse> filaClassificacao(){
        return service.filaClassificacao().stream().map(ClassificacaoResponse::de).toList();
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping("/fila/atendimento")
    public List<ClassificacaoResponse> filaAtendimento(){
        return service.filaAtendimento().stream().map(ClassificacaoResponse::de).toList();
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping("/fila/em-atendimento")
    public List<ClassificacaoResponse> filaEmAtendimento(){
        return service.filaEmAtendimento().stream().map(ClassificacaoResponse::de).toList();
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping("/{id}")
    public ClassificacaoResponse buscar(@PathVariable Long id){
        return ClassificacaoResponse.de(service.buscar(id));
    }

    @PreAuthorize("hasAuthority('cadastrar_classificacao')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassificacaoResponse criar(@RequestBody CriarClassificacaoRequest request){
        return ClassificacaoResponse.de(service.criar(request.pacienteId(), request.queixa()));
    }

    @PreAuthorize("hasAuthority('editar_classificacao')")
    @PutMapping("/{id}")
    public ClassificacaoResponse atualizar(@PathVariable Long id,
                                           @RequestBody AtualizarClassificacaoRequest request,
                                           Authentication authentication){
        return ClassificacaoResponse.de(
                service.atualizar(id, request.risco(), request.status(), authentication.getName()));
    }

    @PreAuthorize("hasAuthority('editar_classificacao')")
    @PostMapping("/{id}/afericao")
    public ClassificacaoResponse iniciarAfericao(@PathVariable Long id, Authentication authentication){
        return ClassificacaoResponse.de(service.iniciarAfericao(id, authentication.getName()));
    }

    @PreAuthorize("hasAuthority('atender_classificacao')")
    @PostMapping("/{id}/atender")
    public ClassificacaoResponse atender(@PathVariable Long id){
        return ClassificacaoResponse.de(service.atender(id));
    }

    @PreAuthorize("hasAuthority('atender_classificacao')")
    @PostMapping("/{id}/finalizar")
    public ClassificacaoResponse finalizar(@PathVariable Long id){
        return ClassificacaoResponse.de(service.finalizar(id));
    }

    @PreAuthorize("hasAuthority('excluir_classificacao')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id){
        service.excluir(id);
    }

}
