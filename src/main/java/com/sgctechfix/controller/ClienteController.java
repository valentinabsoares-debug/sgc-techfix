package com.sgctechfix.controller;

import com.sgctechfix.dto.ClienteDTO;
import com.sgctechfix.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDTO.Response>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO.Response> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteDTO.Response> criar(@RequestBody ClienteDTO.Request request) {
        return ResponseEntity.ok(clienteService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO.Response> atualizar(@PathVariable Long id,
                                                         @RequestBody ClienteDTO.Request request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}