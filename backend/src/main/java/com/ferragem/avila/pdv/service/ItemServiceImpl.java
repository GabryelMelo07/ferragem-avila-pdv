package com.ferragem.avila.pdv.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.repository.ItemRepository;
import com.ferragem.avila.pdv.service.interfaces.ItemService;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;
    
    @Override
    public Item save(Item item) {
        return itemRepository.save(item);
    }
    
    @Override
    public void saveAll(List<Item> item) {
        itemRepository.saveAll(item);
    }

}
