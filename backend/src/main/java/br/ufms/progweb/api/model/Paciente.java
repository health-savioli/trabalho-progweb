package br.ufms.progweb.api.model;

import java.time.LocalDate;


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
@Table(name = "pacientes")
@Getter
@Setter
public class Paciente{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;

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

    @Column(name = "idade")
    private Integer idade;

    /** "M" ou "F". Exigido para cadastrar o paciente no integrador. */
    @Column(name = "sexo")
    private String sexo;

    protected Paciente(){}

    public Paciente(String nome, String telefone, String email, String cpf, LocalDate dataNasc, String cep, String endereco, Integer idade){
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.cpf = cpf;
        this.dataNasc = dataNasc;
        this.cep = cep;
        this.endereco = endereco;
        this.idade = idade;
    }
}