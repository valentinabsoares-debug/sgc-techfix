package com.sgctechfix.service;

import com.sgctechfix.dto.VendaDTO;
import com.sgctechfix.exception.RecursoNaoEncontradoException;
import com.sgctechfix.exception.RegraDeNegocioException;
import com.sgctechfix.model.*;
import com.sgctechfix.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public List<VendaDTO.Response> listarTodas() {
        return vendaRepository.findAllByOrderByDataDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VendaDTO.Response buscarPorId(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
        return toResponse(venda);
    }

    public List<VendaDTO.Response> buscarPorCliente(Long clienteId) {
        return vendaRepository.findByClienteIdOrderByDataDesc(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<VendaDTO.Response> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByDataBetween(inicio, fim)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VendaDTO.Response criar(VendaDTO.Request request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.getClienteId()));

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", request.getUsuarioId()));

        Venda venda = Venda.builder()
                .cliente(cliente)
                .usuario(usuario)
                .observacao(request.getObservacao())
                .build();

        for (VendaDTO.ItemRequest itemReq : request.getItens()) {
            Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", itemReq.getProdutoId()));

            if (!produto.getAtivo()) {
                throw new RegraDeNegocioException("Produto inativo: " + produto.getNome());
            }
            if (produto.getEstoque() < itemReq.getQuantidade()) {
                throw new RegraDeNegocioException(
                        "Estoque insuficiente para: " + produto.getNome() +
                        ". Disponível: " + produto.getEstoque());
            }

            // Baixa no estoque
            produto.setEstoque(produto.getEstoque() - itemReq.getQuantidade());
            produtoRepository.save(produto);

            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemReq.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();

            venda.getItens().add(item);
        }

        venda.calcularTotal();
        return toResponse(vendaRepository.save(venda));
    }

    private VendaDTO.Response toResponse(Venda venda) {
        List<VendaDTO.ItemResponse> itens = venda.getItens().stream()
                .map(item -> VendaDTO.ItemResponse.builder()
                        .id(item.getId())
                        .produtoId(item.getProduto().getId())
                        .produtoNome(item.getProduto().getNome())
                        .quantidade(item.getQuantidade())
                        .precoUnitario(item.getPrecoUnitario())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return VendaDTO.Response.builder()
                .id(venda.getId())
                .data(venda.getData())
                .valorTotal(venda.getValorTotal())
                .clienteId(venda.getCliente().getId())
                .clienteNome(venda.getCliente().getNome())
                .usuarioNome(venda.getUsuario().getNome())
                .observacao(venda.getObservacao())
                .itens(itens)
                .build();
    }
}
