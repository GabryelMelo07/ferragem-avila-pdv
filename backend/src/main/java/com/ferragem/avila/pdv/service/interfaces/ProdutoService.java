package com.ferragem.avila.pdv.service.interfaces;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.utils.ProdutosFromCsv;

public interface ProdutoService {
    Page<Produto> getAll(Pageable pageable);
    
    Page<Produto> getAllInativos(Pageable pageable);
    
    Page<Produto> getAllByDescricao(Pageable pageable, String descricao);

    Produto getById(long id);

    Produto getByCodigoBarras(String codigoBarras);
    
    Produto save(ProdutoDTO dto);
    
    Produto save(Produto produto);

    ProdutosFromCsv saveProductsFromCsv(MultipartFile file) throws IOException;
    
    Produto update(long id, ProdutoDTO dto);

    void delete(long id);
}
