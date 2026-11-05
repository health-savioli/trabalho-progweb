package br.ufms.progweb.api.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "Users")
@Getter
@Setter
public class User{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_tp_user", nullable = false)
    private TipoUser tipoUser;

    @Column(name = "nome")
    private String nome;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "email")
    private String email;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "data_nasc")
    private LocalDate dataNasc;

    @Column(name = "cep")
    private String cep;

    @Column(name = "endereco")
    private String endereco;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "senha")
    private String senha;


    protected User(){}

    public User(String nome, String telefone, String email, String cpf, LocalDate dataNasc, String cep, String endereco, String senha){
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.cpf = cpf;
        this.dataNasc = dataNasc;
        this.cep = cep;
        this.endereco = endereco;
        this.senha = senha;
    }

}