package br.ufms.progweb.api.model;

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
@Table(name = "classificacao")
@Getter
@Setter
public class Classificacao{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id_classificacao")
    private Long idClassificacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_paciente", nullable = false)
    private Paciente paciente;

    /** Profissional que definiu o risco. Nulo até a classificação ser feita. */
    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    /** Nula até a aferição ser iniciada. */
    @ManyToOne
    @JoinColumn(name = "fk_afericao")
    private Afericao afericao;

    /** vermelho | laranja | amarelo | verde | azul. Nulo até o profissional classificar. */
    @Column(name = "risco")
    private String risco;

    /** aguardando | em_atendimento | finalizada | cancelada. */
    @Column(name = "status", nullable = false)
    private String status;

    /** Reclamação do paciente ao chegar. Nula nas classificações criadas antes do campo existir. */
    @Column(name = "queixa", length = 500)
    private String queixa;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Classificacao(){}

    public Classificacao(Paciente paciente, String status){
        this.paciente = paciente;
        this.status = status;
    }

}
