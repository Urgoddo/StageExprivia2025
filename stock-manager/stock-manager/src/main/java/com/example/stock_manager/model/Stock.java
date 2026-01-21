package com.example.stock_manager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter                       // Genera tutti i getter
@Setter                       // Genera tutti i setter
@NoArgsConstructor            // Costruttore vuoto per JPA
@AllArgsConstructor           // Costruttore con tutti i parametri
@Builder                      // Permette di creare oggetti con il pattern Builder
public class Stock {
    @Id
    private String symbol;
    private int quantity;
}