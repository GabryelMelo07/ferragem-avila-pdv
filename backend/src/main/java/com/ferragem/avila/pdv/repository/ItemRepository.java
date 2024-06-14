package com.ferragem.avila.pdv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ferragem.avila.pdv.model.Item;
import java.util.List;
import java.util.Optional;


public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByVendaId(long id);

    @Query("SELECT i FROM Item i WHERE i.produto.id = :produtoId AND i.venda.id = :vendaId")
    Optional<Item> getItemByProdutoAndVendaId(long produtoId, long vendaId);
}
