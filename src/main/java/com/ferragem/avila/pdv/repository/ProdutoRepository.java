package com.ferragem.avila.pdv.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ferragem.avila.pdv.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByIdAndAtivoTrue(Long id);
    
    @Query("SELECT p FROM Produto p WHERE p.ativo = true ORDER BY p.id ASC")
    ArrayList<Produto> findAllAtivosOrderedById();
    
    Page<Produto> findByAtivoTrue(Pageable pageable);

    Page<Produto> findByAtivoFalse(Pageable pageable);

    Optional<Produto> findByCodigoBarrasEAN13(String codigoBarras);

    Optional<Produto> findByDescricao(String descricao);

    @Query("""
            SELECT DISTINCT p FROM Produto p
            WHERE
                p.ativo = true
                AND (p.codigoBarrasEAN13 LIKE CONCAT(:parametro, '%'))
                OR (LOWER(p.descricao) LIKE LOWER(CONCAT('%', :parametro, '%')))
        """)
    Page<Produto> findByParametros(Pageable pageable, String parametro);

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

    @Query("SELECT p FROM Produto p WHERE p.estoque <= 10")
    Page<Produto> findProdutosComEstoqueBaixo(Pageable pageable);
}
