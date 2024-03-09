package com.ferragem.avila.pdv.service.interfaces;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.Venda;

public interface VendaService {
    Page<Venda> getAll(Pageable pageable);

    Venda getById(long id);

    Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim);

    List<Produto> getProdutosFromVendaAtiva();

    Venda save();

    boolean existsVendaAtiva();
    
    void delete();

    Venda addItem(ItemDTO itemDto);

    Venda addItem(String codigoBarras);

    Venda addItem(List<ItemDTO> itensDto);

    Venda persist(VendaDTO dto);
}
