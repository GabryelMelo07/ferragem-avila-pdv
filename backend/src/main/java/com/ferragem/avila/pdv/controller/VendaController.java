package com.ferragem.avila.pdv.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ferragem.avila.pdv.dto.ItemDto;
import com.ferragem.avila.pdv.dto.DataBetweenDto;
import com.ferragem.avila.pdv.dto.VendaDto;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.service.interfaces.VendaService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/venda")
public class VendaController {

    @Autowired
    private VendaService vendaService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Page<Venda>> getAllConcluidas(Pageable pageable) {
        return ResponseEntity.ok(vendaService.getAll(pageable));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Venda> getById(@PathVariable long id) {
        return ResponseEntity.ok(vendaService.getById(id));
    }

    @PostMapping("/relatorio/data")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Page<Venda>> getBetweenDates(Pageable pageable, @RequestBody DataBetweenDto datas) {
        return ResponseEntity.ok(vendaService.getBetweenDataConclusao(pageable, datas.dataHoraInicio(), datas.dataHoraFim()));
    }

    @GetMapping("/ativa/itens")
    public ResponseEntity<List<Item>> getItens() {
        return ResponseEntity.ok(vendaService.getItensFromVendaAtiva());
    }
    
    @GetMapping("/ativa")
    public ResponseEntity<Optional<Venda>> getVendaAtiva() {
        return ResponseEntity.ok(vendaService.getVendaAtiva());
    }
    
    @PostMapping("/add-item")
    public ResponseEntity<Venda> addItem(@RequestBody ItemDto item) {
        return ResponseEntity.ok(vendaService.addItem(item));
    }

    @PostMapping("/add-item/codigo-barras")
    public ResponseEntity<Venda> addItem(@RequestParam String codigoBarras) {
        return ResponseEntity.ok(vendaService.addItem(codigoBarras));
    }

    @PostMapping("/add-itens/lista")
    public ResponseEntity<Venda> addItem(@RequestBody List<ItemDto> itens) {
        return ResponseEntity.ok(vendaService.addItem(itens));
    }

    @DeleteMapping("/remover-item/{itemId}")
    public ResponseEntity<Venda> removeItem(@RequestParam long itemId) {
        return ResponseEntity.ok(vendaService.removeItem(itemId));
    }
    
    @PostMapping("/concluir")
    public ResponseEntity<Venda> concluir(@RequestBody VendaDto dto) {
        vendaService.concluirVenda(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelar() {
        vendaService.delete();
        return ResponseEntity.noContent().build();
    }
    
}
