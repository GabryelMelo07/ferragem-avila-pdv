package com.ferragem.avila.pdv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<Venda>> getAllVendasConcluidas(Pageable pageable) {
        return ResponseEntity.ok().body(vendaService.getAll(pageable).getContent());
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
    
    @GetMapping("/status")
    public ResponseEntity<Boolean> existsVendaAtiva() {
        boolean response = vendaService.existsVendaAtiva();

        if (!response)
            return new ResponseEntity<Boolean>(response, HttpStatus.NOT_FOUND);

        return new ResponseEntity<Boolean>(response, HttpStatus.OK);
    }
        
    @PostMapping("/nova-venda")
    public ResponseEntity<Void> createVenda() {
        vendaService.save();
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/add-item")
    public ResponseEntity<Venda> addItemToVenda(@RequestBody ItemDTO item) {
        return ResponseEntity.ok().body(vendaService.addItem(item));
    }

    @PostMapping("/add-item/codigo-barras/")
    public ResponseEntity<Venda> addItemToVenda(@RequestParam String codigoBarras) {
        return ResponseEntity.ok().body(vendaService.addItem(codigoBarras));
    }

    @PostMapping("/add-itens/lista/produtos")
    public ResponseEntity<Venda> addItemToVenda(@RequestBody List<ItemDTO> itens) {
        return ResponseEntity.ok().body(vendaService.addItem(itens));
    }

    @PostMapping("/update/item/quantidade")
    public ResponseEntity<Venda> updateItemQuantity(@RequestParam float quantidade, long produtoId) {
        return ResponseEntity.ok().body(vendaService.updateItemQuantity(quantidade, produtoId));
    }
    
    @PostMapping("/concluir")
    public ResponseEntity<Venda> persistVenda(@RequestBody VendaDTO dto) {
        return ResponseEntity.ok().body(vendaService.persist(dto));
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> deleteVendaFromRedis() {
        vendaService.delete();
        return ResponseEntity.noContent().build();
    }
    
}
