package br.ufms.progweb.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import org.springframework.security.access.prepost.PreAuthorize;

import br.ufms.progweb.api.model.Paciente;
import br.ufms.progweb.api.service.PacienteService;


@RestController
@RequestMapping("/api/paciente")
public class PacienteController{

    private final PacienteService service;

    public PacienteController(PacienteService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('visualizar_paciente')")
    @GetMapping
    public List<Paciente> listar(){
        return service.listar();
    }

    @PreAuthorize("hasAuthority('visualizar_paciente')")
    @GetMapping("/busca")
    public List<Paciente> pesquisar(@RequestParam String termo){
        return service.pesquisar(termo);
    }

    @PreAuthorize("hasAuthority('visualizar_paciente')")
    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable Long id){
        return service.buscar(id);
    }


    @PreAuthorize("hasAuthority('cadastrar_paciente')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Paciente criar(@RequestBody Paciente paciente){
        return service.criar(paciente);
    }

    @PreAuthorize("hasAuthority('editar_paciente')")
    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable Long id, @RequestBody Paciente paciente){
        return service.atualizar(id, paciente);
    }

    @PreAuthorize("hasAuthority('excluir_paciente')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id){
        service.excluir(id);
    }

}