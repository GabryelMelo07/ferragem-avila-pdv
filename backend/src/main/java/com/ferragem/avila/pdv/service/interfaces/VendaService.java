package com.ferragem.avila.pdv.service.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ferragem.avila.pdv.dto.ItemDto;
import com.ferragem.avila.pdv.dto.VendaDto;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Venda;

public interface VendaService {
    Page<Venda> getAll(Pageable pageable);

    Venda getById(long id);

    Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim);

    List<Item> getItensFromVendaAtiva();

    Venda save();

    Venda save(Venda venda);

    Optional<Venda> getVendaAtiva();
    
    void delete();

    Venda addItem(ItemDto itemDto);

    Venda addItem(String codigoBarras);

    Venda addItem(List<ItemDto> itensDto);

    Venda editItem(long itemId, float quantidade);

    Venda removeItem(long itemId);

    void concluirVenda(VendaDto dto);
}
