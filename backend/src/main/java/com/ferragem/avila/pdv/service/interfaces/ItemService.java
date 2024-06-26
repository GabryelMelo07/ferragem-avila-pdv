package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.ferragem.avila.pdv.model.Item;

public interface ItemService {
    Item save(Item item);

    Item getById(long id);

    Optional<Item> getItemByProdutoAndVendaId(long produtoId, long vendaId);

    void saveAll(List<Item> item);

    void delete(Item item);

    void deleteAll(List<Item> itens);
}
