package com.example.stock_manager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockRequest {

    @NotBlank(message = "Symbol cannot be blank")
    @Size(min = 1, max = 10, message = "Symbol must be between 1 and 10 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Symbol must contain only letters and numbers")
    private String symbol;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
