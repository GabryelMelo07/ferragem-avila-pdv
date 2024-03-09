package com.ferragem.avila.pdv.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoService {
    Page<Produto> getAll(Pageable pageable);
    
    Page<Produto> getAllInativos(Pageable pageable);
    
    Page<Produto> getAllByDescricao(Pageable pageable, String descricao);

    Produto getById(long id);

    Produto getByCodigoBarras(String codigoBarras);
    
    Produto save(ProdutoDTO dto);

    Produto update(long id, ProdutoDTO dto);

    void delete(long id);
    
    Produto save(Produto produto);
}
