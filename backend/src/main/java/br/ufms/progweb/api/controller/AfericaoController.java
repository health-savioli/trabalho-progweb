package br.ufms.progweb.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ufms.progweb.api.dto.AfericaoResponse;
import br.ufms.progweb.api.dto.IniciarAfericaoRequest;
import br.ufms.progweb.api.service.AfericaoService;

@RestController
@RequestMapping("/api/afericao")
public class AfericaoController{

    private final AfericaoService service;

    public AfericaoController(AfericaoService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping
    public List<AfericaoResponse> listar(){
        return service.listar().stream().map(AfericaoResponse::de).toList();
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping("/{id}")
    public AfericaoResponse buscar(@PathVariable Long id){
        return AfericaoResponse.de(service.buscar(id));
    }

    @PreAuthorize("hasAuthority('visualizar_classificacao')")
    @GetMapping("/paciente/{pacienteId}")
    public List<AfericaoResponse> listarPorPaciente(@PathVariable Long pacienteId){
        return service.listarPorPaciente(pacienteId).stream().map(AfericaoResponse::de).toList();
    }

    @PreAuthorize("hasAuthority('cadastrar_classificacao')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AfericaoResponse iniciar(@RequestBody IniciarAfericaoRequest request, Authentication authentication){
        return AfericaoResponse.de(service.iniciar(request.pacienteId(), authentication.getName()));
    }

    @PreAuthorize("hasAuthority('editar_classificacao')")
    @PostMapping("/{id}/sincronizar")
    public AfericaoResponse sincronizar(@PathVariable Long id){
        return AfericaoResponse.de(service.sincronizar(id));
    }

    @PreAuthorize("hasAuthority('editar_classificacao')")
    @PostMapping("/{id}/fechar")
    public AfericaoResponse fechar(@PathVariable Long id){
        return AfericaoResponse.de(service.fechar(id));
    }

}
