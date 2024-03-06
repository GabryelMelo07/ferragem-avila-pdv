package com.ferragem.avila.pdv.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.repository.ProdutoRepository;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Override
    public List<Produto> getAll() {
        return produtoRepository.findByAtivoTrue();
    }

    @Override
    public List<Produto> getAllInativos() {
        return produtoRepository.findByAtivoFalse();
    }

    @Override
    public List<Produto> getAllByDescricao(String descricao) {
        return produtoRepository.findByDescricaoContainingIgnoreCaseAndAtivoTrue(descricao);
    }
    
    @Override
    public Produto getById(long id) {
        return produtoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não existe."));
    }
    
    @Override
    public Produto getByCodigoBarras(String codigoBarras) {
        return produtoRepository.findByCodigoBarrasEAN13(codigoBarras);
    }
    
    @Override
    public Produto save(ProdutoDTO dto) {
        try {
            Long.valueOf(dto.codigoBarrasEAN13());
        } catch (Exception e) {
            throw new RuntimeException("Código de barras inválido." + e.getMessage());
        }
        
        Produto p = new Produto(dto);
        return produtoRepository.save(p);
    }

    @Override
    public Produto save(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Override
    public Produto update(long id, ProdutoDTO dto) {
        Produto p = getById(id);
        p.setDescricao(dto.descricao());
        p.setUnidadeMedida(dto.unidadeMedida());
        p.setEstoque(dto.estoque());
        p.setPreco(dto.preco());
        p.setCodigoBarrasEAN13(dto.codigoBarrasEAN13());
        return produtoRepository.save(p);
    }

    @Override
    public void delete(long id) {
        Produto p = getById(id);
        p.setAtivo(false);
        produtoRepository.save(p);
    }
    
}
