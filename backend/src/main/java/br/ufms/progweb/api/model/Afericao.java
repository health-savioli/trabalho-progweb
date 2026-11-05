package br.ufms.progweb.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "afericao")
@Getter
@Setter
public class Afericao{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id_afericao")
    private Long idAfericao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user", nullable = false)
    private User user;

    @Column(name = "sessao_id")
    private Long sessaoId;

    @Column(name = "atendimento_id", nullable = false)
    private String atendimentoId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "temperatura", precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(name = "saturacao_o2")
    private Integer saturacaoO2;

    @Column(name = "frequencia_cardiaca")
    private Integer frequenciaCardiaca;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Afericao(){}

    public Afericao(Paciente paciente, User user, String atendimentoId, String status){
        this.paciente = paciente;
        this.user = user;
        this.atendimentoId = atendimentoId;
        this.status = status;
    }

}
