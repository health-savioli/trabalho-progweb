package br.ufms.progweb.api.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipo_user")
@Getter
@Setter
public class TipoUser {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id_tp_user")
    private Long idTpUser;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cadastrar_user")
    private Boolean cadastrarUser;

    @Column(name = "editar_user")
    private Boolean editarUser;

    @Column(name = "excluir_user")
    private Boolean excluirUser;
    
    @Column(name = "visualizar_user")
    private Boolean visualizarUser;

    @Column(name = "cadastrar_classificacao")
    private Boolean cadastrarClassificacao;

    @Column(name = "editar_classificacao")
    private Boolean editarClassificacao;

    @Column(name = "excluir_classificacao")
    private Boolean excluirClassificacao;

    @Column(name = "visualizar_classificacao")
    private Boolean visualizarClassificacao;

    @Column(name = "cadastrar_paciente")
    private Boolean cadastrarPaciente;

    @Column(name = "editar_paciente")
    private Boolean editarPaciente;

    @Column(name = "excluir_paciente")
    private Boolean excluirPaciente;

    @Column(name = "visualizar_paciente")
    private Boolean visualizarPaciente;

    /** Atender os pacientes da fila de atendimento (médico). */
    @Column(name = "atender_classificacao")
    private Boolean atenderClassificacao;


    protected TipoUser() {}

    public TipoUser(String nome,
                    Boolean cadastrarUser, Boolean editarUser, Boolean excluirUser, Boolean visualizarUser,
                    Boolean cadastrarClassificacao, Boolean editarClassificacao, Boolean excluirClassificacao, Boolean visualizarClassificacao,
                    Boolean cadastrarPaciente, Boolean editarPaciente, Boolean excluirPaciente, Boolean visualizarPaciente){
        this.nome = nome;
        this.cadastrarUser = cadastrarUser;
        this.editarUser = editarUser;
        this.excluirUser = excluirUser;
        this.visualizarUser = visualizarUser;
        this.cadastrarClassificacao = cadastrarClassificacao;
        this.editarClassificacao = editarClassificacao;
        this.excluirClassificacao = excluirClassificacao;
        this.visualizarClassificacao = visualizarClassificacao;
        this.cadastrarPaciente = cadastrarPaciente;
        this.editarPaciente = editarPaciente;
        this.excluirPaciente = excluirPaciente;
        this.visualizarPaciente = visualizarPaciente;
    }

    public List<String> permissoes() {
        List<String> lista = new ArrayList<>();
        if (Boolean.TRUE.equals(cadastrarUser)) lista.add("cadastrar_user");
        if (Boolean.TRUE.equals(editarUser)) lista.add("editar_user");
        if (Boolean.TRUE.equals(excluirUser)) lista.add("excluir_user");
        if (Boolean.TRUE.equals(visualizarUser)) lista.add("visualizar_user");
        if (Boolean.TRUE.equals(cadastrarPaciente)) lista.add("cadastrar_paciente");
        if (Boolean.TRUE.equals(editarPaciente)) lista.add("editar_paciente");
        if (Boolean.TRUE.equals(excluirPaciente)) lista.add("excluir_paciente");
        if (Boolean.TRUE.equals(visualizarPaciente)) lista.add("visualizar_paciente");
        if (Boolean.TRUE.equals(cadastrarClassificacao)) lista.add("cadastrar_classificacao");
        if (Boolean.TRUE.equals(editarClassificacao)) lista.add("editar_classificacao");
        if (Boolean.TRUE.equals(excluirClassificacao)) lista.add("excluir_classificacao");
        if (Boolean.TRUE.equals(visualizarClassificacao)) lista.add("visualizar_classificacao");
        if (Boolean.TRUE.equals(atenderClassificacao)) lista.add("atender_classificacao");
        return lista;
    }

}