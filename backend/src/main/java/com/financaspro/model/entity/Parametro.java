package com.financaspro.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "parametro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String chave;

    @Column(columnDefinition = "TEXT")
    private String valor;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, length = 30)
    private String tipo; // STRING, NUMBER, BOOLEAN, PASSWORD

    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
        if (this.tipo == null) {
            this.tipo = "STRING";
        }
    }
}
