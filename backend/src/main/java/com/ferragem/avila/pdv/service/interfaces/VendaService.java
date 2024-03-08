package com.ferragem.avila.pdv.service.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
import com.ferragem.avila.pdv.model.Venda;

public interface VendaService {
    List<Venda> getAll();

    Venda getById(long id);

    List<Venda> getBetweenDataConclusao(LocalDate dataInicio, LocalDate dataFim);

    Venda save();
    
    void delete();

    Venda addItem(ItemDTO itemDto);

    Venda addItem(String codigoBarras);

    Venda addItem(List<ItemDTO> itensDto);

    Venda persist(VendaDTO dto);
}
