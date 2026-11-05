package br.ufms.progweb.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufms.progweb.api.model.TipoUser;
import br.ufms.progweb.api.service.TipoUserService;

@RestController
@RequestMapping("/api/tipos-user")
public class TipoUserController {

    private final TipoUserService service;

    public TipoUserController(TipoUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoUser> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public TipoUser buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
