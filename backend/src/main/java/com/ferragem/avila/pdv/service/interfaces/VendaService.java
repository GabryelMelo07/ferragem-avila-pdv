package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
import com.ferragem.avila.pdv.model.Venda;

public interface VendaService {
    List<Venda> getAll();

    Venda getById(long id);

    Venda saveInRedis();
    
    void deleteFromRedis();

    Venda addItemToVenda(ItemDTO itemDto);

    Venda persistVenda(VendaDTO dto);
}
