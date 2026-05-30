package com.mlops.pacientes.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "paciente")
public class PatientEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paciente_id", updatable = false)
    private Long patientId;

    @Column(name = "primer_nombre", nullable = false)
    private String firstName;

    @Column(name = "segundo_nombre", nullable = false)
    private String secondName;

    @Column(name = "primer_apellido", nullable = false)
    private String firstLastName;

    @Column(name = "segundo_apellido", nullable = false)
    private String secondLastName;

    @Column(name = "edad", nullable = false)
    private Integer age;

    @Column(name = "genero", nullable = false)
    private String gender;

}
