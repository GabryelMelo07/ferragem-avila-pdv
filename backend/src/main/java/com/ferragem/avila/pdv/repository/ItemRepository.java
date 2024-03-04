package com.ferragem.avila.pdv.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ferragem.avila.pdv.model.Item;
import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByVendaId(long id);
}
