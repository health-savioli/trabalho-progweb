package br.ufms.progweb.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import br.ufms.progweb.api.model.User;
import br.ufms.progweb.api.service.UserService;


@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService service;

    public UserController(UserService service){
        this.service = service;
    }

    @PreAuthorize("hasAuthority('visualizar_user')")
    @GetMapping
    public List<User> listar(){
        return service.listar();
    }

    @PreAuthorize("hasAuthority('visualizar_user')")
    @GetMapping("/{id}")
    public User buscar(@PathVariable Long id){
        return service.buscar(id);
    }

    @PreAuthorize("hasAuthority('cadastrar_user')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User criar(@RequestBody User user){
        return service.criar(user);
    }

    @PreAuthorize("hasAuthority('editar_user')")
    @PutMapping("/{id}")
    public User atualizar(@PathVariable Long id, @RequestBody User user){
        return service.atualizar(id, user);
    }

    @PreAuthorize("hasAuthority('excluir_user')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, Authentication authentication){
        service.excluir(id, authentication.getName());
    }

}
