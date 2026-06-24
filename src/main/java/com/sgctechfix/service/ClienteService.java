package com.sgctechfix.service;

import com.sgctechfix.dto.ClienteDTO;
import com.sgctechfix.exception.RecursoNaoEncontradoException;
import com.sgctechfix.exception.RegraDeNegocioException;
import com.sgctechfix.model.Cliente;
import com.sgctechfix.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<ClienteDTO.Response> listarTodos() {
        return clienteRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClienteDTO.Response buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        return toResponse(cliente);
    }

    public List<ClienteDTO.Response> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO.Response criar(ClienteDTO.Request request) {
        if (request.getCpf() != null && clienteRepository.findByCpf(request.getCpf()).isPresent()) {
            throw new RegraDeNegocioException("CPF já cadastrado: " + request.getCpf());
        }
        if (request.getEmail() != null && clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegraDeNegocioException("E-mail já cadastrado: " + request.getEmail());
        }

        Cliente cliente = Cliente.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .cpf(request.getCpf())
                .ativo(true)
                .build();

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO.Response atualizar(Long id, ClienteDTO.Request request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));

        clienteRepository.findByEmail(request.getEmail())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new RegraDeNegocioException("E-mail já em uso por outro cliente.");
                    }
                });

        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setTelefone(request.getTelefone());
        cliente.setEndereco(request.getEndereco());
        cliente.setCpf(request.getCpf());

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void deletar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        // Soft delete
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    private ClienteDTO.Response toResponse(Cliente cliente) {
        return ClienteDTO.Response.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .cpf(cliente.getCpf())
                .ativo(cliente.getAtivo())
                .build();
    }
}
