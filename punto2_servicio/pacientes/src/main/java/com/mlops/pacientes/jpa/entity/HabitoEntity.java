package com.mlops.pacientes.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "habito")
public class HabitoEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "habito_id", updatable = false)
    private Long idHabito;

    @Column(name = "habito", nullable = false)
    private String habito;
    @Column(name = "bueno")
    private boolean bueno;

}
