package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;

import com.ferragem.avila.pdv.model.Item;

public interface ItemService {
    List<Item> getAllByVendaId(long id);
    
    Item save(Item item);

    void saveAll(List<Item> item);
}
