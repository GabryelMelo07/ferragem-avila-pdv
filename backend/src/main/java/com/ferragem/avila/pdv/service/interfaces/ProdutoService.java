package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoService {
    List<Produto> getAll();

    Produto getById(long id);

    Produto save(ProdutoDTO dto);

    Produto update(long id, ProdutoDTO dto);

    void delete(long id);
    
    Produto save(Produto produto);
    
    List<Produto> getAllInativos();
    
    List<Produto> getAllByDescricao(String descricao);
}
