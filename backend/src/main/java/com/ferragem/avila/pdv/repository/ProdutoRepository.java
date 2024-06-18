package com.ferragem.avila.pdv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoRepository extends PagingAndSortingRepository<Produto, Long> {
    Produto save(Produto produto);
    
    Optional<Produto> findByIdAndAtivoTrue(Long id);
    
    Page<Produto> findByAtivoTrue(Pageable pageable);

    Page<Produto> findByAtivoFalse(Pageable pageable);

    Page<Produto> findByDescricaoContainingIgnoreCaseAndAtivoTrue(Pageable pageable, String nome);

    Produto findByCodigoBarrasEAN13(String codigoBarras);

    @Query("""
            SELECT i.produto, SUM(i.quantidade) as totalVendido
            FROM Item i
            JOIN i.venda v
            WHERE EXTRACT(MONTH FROM v.dataHoraConclusao) = :mes
            AND EXTRACT(YEAR FROM v.dataHoraConclusao) = :ano
            GROUP BY i.produto
            ORDER BY totalVendido DESC
            LIMIT 5
        """)
    List<Produto> getMaisVendidosMes(int mes, int ano);
}
