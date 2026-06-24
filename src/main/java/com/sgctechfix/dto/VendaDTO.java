package com.sgctechfix.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VendaDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotNull(message = "Cliente é obrigatório")
        private Long clienteId;

        @NotNull(message = "Usuário é obrigatório")
        private Long usuarioId;

        @NotEmpty(message = "A venda deve ter pelo menos um item")
        private List<ItemRequest> itens;

        private String observacao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRequest {

        @NotNull(message = "Produto é obrigatório")
        private Long produtoId;

        @NotNull
        @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
        private Integer quantidade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private LocalDateTime data;
        private BigDecimal valorTotal;
        private String clienteNome;
        private Long clienteId;
        private String usuarioNome;
        private String observacao;
        private List<ItemResponse> itens;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private Long id;
        private Long produtoId;
        private String produtoNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
    }
}
