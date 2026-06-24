package com.sgctechfix.service;

import com.sgctechfix.dto.ProdutoDTO;
import com.sgctechfix.exception.RecursoNaoEncontradoException;
import com.sgctechfix.exception.RegraDeNegocioException;
import com.sgctechfix.model.Produto;
import com.sgctechfix.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<ProdutoDTO.Response> listarTodos() {
        return produtoRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProdutoDTO.Response buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
        return toResponse(produto);
    }

    public List<ProdutoDTO.Response> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProdutoDTO.Response> buscarPorCategoria(String categoria) {
        return produtoRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoDTO.Response criar(ProdutoDTO.Request request) {
        Produto produto = Produto.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .preco(request.getPreco())
                .estoque(request.getEstoque() != null ? request.getEstoque() : 0)
                .categoria(request.getCategoria())
                .ativo(true)
                .build();

        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoDTO.Response atualizar(Long id, ProdutoDTO.Request request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));

        produto.setNome(request.getNome());
        produto.setDescricao(request.getDescricao());
        produto.setPreco(request.getPreco());
        produto.setEstoque(request.getEstoque() != null ? request.getEstoque() : produto.getEstoque());
        produto.setCategoria(request.getCategoria());

        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
        // Soft delete
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional
    public ProdutoDTO.Response atualizarEstoque(Long id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));

        int novoEstoque = produto.getEstoque() + quantidade;
        if (novoEstoque < 0) {
            throw new RegraDeNegocioException("Estoque insuficiente. Disponível: " + produto.getEstoque());
        }

        produto.setEstoque(novoEstoque);
        return toResponse(produtoRepository.save(produto));
    }

    private ProdutoDTO.Response toResponse(Produto produto) {
        return ProdutoDTO.Response.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .estoque(produto.getEstoque())
                .categoria(produto.getCategoria())
                .ativo(produto.getAtivo())
                .build();
    }
}
