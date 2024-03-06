package com.ferragem.avila.pdv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();
    List<Produto> findByAtivoFalse();
    List<Produto> findByDescricaoContainingIgnoreCaseAndAtivoTrue(String nome);
    Produto findByCodigoBarrasEAN13(String codigoBarras);
}
