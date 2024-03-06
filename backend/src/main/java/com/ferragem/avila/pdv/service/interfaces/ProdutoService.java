package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoService {
    List<Produto> getAll();
    
    List<Produto> getAllInativos();
    
    List<Produto> getAllByDescricao(String descricao);

    Produto getById(long id);

    Produto getByCodigoBarras(String codigoBarras);
    
    Produto save(ProdutoDTO dto);

    Produto update(long id, ProdutoDTO dto);

    void delete(long id);
    
    Produto save(Produto produto);
}
