package com.ferragem.avila.pdv.service.interfaces;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.ferragem.avila.pdv.dto.ProdutoDto;
import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoService {
    void gerarRelatorioGeral(String redisRelatorioProdutosKey);
    
    Page<Produto> getAll(Pageable pageable);
    
    Page<Produto> getAllInativos(Pageable pageable);
    
    Page<Produto> findByParams(Pageable pageable, String parametro);

    Produto getById(long id);

    Produto getByCodigoBarras(String codigoBarras);

    List<Produto> getMaisVendidosMes(LocalDate data);

    Page<Produto> getProdutosBaixoEstoque(Pageable pageable);
    
    Produto save(ProdutoDto dto);
    
    Produto save(Produto produto);

    void importarProdutosCsv(MultipartFile file) throws IOException;
    
    Produto update(long id, ProdutoDto dto);

    void delete(long id);
}
