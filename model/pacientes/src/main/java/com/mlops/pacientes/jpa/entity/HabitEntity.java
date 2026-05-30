package com.mlops.pacientes.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "habito")
public class HabitEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "habito_id", updatable = false)
    private Long habitId;

    @Column(name = "nombre_habito", nullable = false)
    private String habitName;

    @Column(name = "buen_habito")
    private boolean good;

}
