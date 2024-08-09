package com.ferragem.avila.pdv.service.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ferragem.avila.pdv.dto.ItemDto;
import com.ferragem.avila.pdv.dto.VendaDto;
import com.ferragem.avila.pdv.dto.VendedorDto;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.model.enums.VendaExclusaoResultado;

public interface VendaService {
    Page<Venda> getAll(Pageable pageable);

    Venda getById(long id);

    Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim);

    List<Item> getItensFromVendaAtiva();

    Venda save(Venda venda);

    Optional<Venda> getVendaAtiva();
    
    void cancel();

    VendaExclusaoResultado delete(Long id);

    Venda addItem(ItemDto itemDto, VendedorDto vendedor);

    Venda addItem(String codigoBarras, VendedorDto vendedor);

    Venda addItem(List<ItemDto> itensDto, VendedorDto vendedor);

    Venda editItem(long itemId, float quantidade);

    Venda removeItem(long itemId);

    void concluirVenda(VendaDto dto);
}
