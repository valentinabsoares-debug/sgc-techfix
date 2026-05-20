package com.sgctechfix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ClienteDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "Nome é obrigatório")
        private String nome;

        @Email(message = "E-mail inválido")
        private String email;

        private String telefone;
        private String endereco;
        private String cpf;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String nome;
        private String email;
        private String telefone;
        private String endereco;
        private String cpf;
        private Boolean ativo;
    }
}