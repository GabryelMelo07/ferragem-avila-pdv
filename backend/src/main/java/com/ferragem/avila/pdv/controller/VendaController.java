package com.ferragem.avila.pdv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
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
    
    @GetMapping("/concluidas")
    public ResponseEntity<List<Venda>> getAllVendasConcluidas() {
        return ResponseEntity.ok().body(vendaService.getAll());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Venda> getVendaById(@PathVariable long id) {
        return ResponseEntity.ok().body(vendaService.getById(id));
    }
    
    @PostMapping("/nova-venda")
    public ResponseEntity<Void> createNewVenda() {
        vendaService.saveInRedis();
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/add-item")
    public ResponseEntity<Venda> addItemToVenda(@RequestBody ItemDTO item) {
        return ResponseEntity.ok().body(vendaService.addItemToVenda(item));
    }
    
    @PostMapping("/concluir")
    public ResponseEntity<Venda> persistVenda(@RequestBody VendaDTO dto) {
        return ResponseEntity.ok().body(vendaService.persistVenda(dto));
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> deleteVendaFromRedis() {
        vendaService.deleteFromRedis();
        return ResponseEntity.noContent().build();
    }
    
}
