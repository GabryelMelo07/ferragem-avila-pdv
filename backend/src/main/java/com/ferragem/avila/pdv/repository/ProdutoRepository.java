package com.ferragem.avila.pdv.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoRepository extends PagingAndSortingRepository<Produto, Long> {
    Produto save(Produto produto);
    
    Optional<Produto> findById(Long id);
    
    Page<Produto> findByAtivoTrue(Pageable pageable);

    Page<Produto> findByAtivoFalse(Pageable pageable);

    Page<Produto> findByDescricaoContainingIgnoreCaseAndAtivoTrue(Pageable pageable, String nome);

    Produto findByCodigoBarrasEAN13(String codigoBarras);
}
