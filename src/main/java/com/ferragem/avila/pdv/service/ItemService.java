package com.ferragem.avila.pdv.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item getById(long id) {
        return itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item não encontrado."));
    }
    
    public Optional<Item> getItemByProdutoAndVendaId(long produtoId, long vendaId) {
        return itemRepository.getItemByProdutoAndVendaId(produtoId, vendaId);
    }
    
    public Item save(Item item) {
        return itemRepository.save(item);
    }
    
    public void saveAll(List<Item> item) {
        itemRepository.saveAll(item);
    }

    public void delete(Item item) {
        itemRepository.delete(item);
    }
        
    public void deleteAll(List<Item> itens) {
        itemRepository.deleteAll(itens);
    }

}
