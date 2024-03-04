package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoService extends CrudService<Produto, ProdutoDTO> {
    Produto save(Produto produto);
    
    List<Produto> getAllInativos();
    
    List<Produto> getAllByDescricao(String descricao);
}
