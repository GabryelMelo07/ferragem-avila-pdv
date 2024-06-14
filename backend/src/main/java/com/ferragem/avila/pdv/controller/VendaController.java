package com.ferragem.avila.pdv.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.DataBetweenDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
import com.ferragem.avila.pdv.model.Produto;
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
    public ResponseEntity<Page<Venda>> getAllVendasConcluidas(Pageable pageable) {
        return ResponseEntity.ok().body(vendaService.getAll(pageable));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Venda> getVendaById(@PathVariable long id) {
        return ResponseEntity.ok().body(vendaService.getById(id));
    }

    @PostMapping("/relatorio/data")
    public ResponseEntity<List<Venda>> getVendasBetweenDates(Pageable pageable, @RequestBody DataBetweenDTO datas) {
        return ResponseEntity.ok().body(vendaService.getBetweenDataConclusao(pageable, datas.dataHoraInicio(), datas.dataHoraFim()).getContent());
    }

    @GetMapping("/ativa/produtos")
    public ResponseEntity<List<Produto>> getProdutosFromVendaAtiva() {
        return ResponseEntity.ok().body(vendaService.getProdutosFromVendaAtiva());
    }
    
    @GetMapping("/ativa")
    public ResponseEntity<Optional<Venda>> getVendaAtiva() {
        return ResponseEntity.ok().body(vendaService.getVendaAtiva());
    }
    
    @PostMapping("/add-item")
    public ResponseEntity<Venda> addItemToVenda(@RequestBody ItemDTO item) {
        return ResponseEntity.ok().body(vendaService.addItem(item));
    }

    @PostMapping("/add-item/codigo-barras")
    public ResponseEntity<Venda> addItemToVenda(@RequestParam String codigoBarras) {
        return ResponseEntity.ok().body(vendaService.addItem(codigoBarras));
    }

    @PostMapping("/add-itens/lista")
    public ResponseEntity<Venda> addItemToVenda(@RequestBody List<ItemDTO> itens) {
        return ResponseEntity.ok().body(vendaService.addItem(itens));
    }
    
    @PostMapping("/concluir")
    public ResponseEntity<Venda> concluirVenda(@RequestBody VendaDTO dto) {
        vendaService.concluirVenda(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelarVenda() {
        vendaService.delete();
        return ResponseEntity.noContent().build();
    }
    
}
