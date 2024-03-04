package com.ferragem.avila.pdv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/produto")
public class ProdutoController {
    
    @Autowired
    private ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Produto>> getAllProdutosAtivos() {
        return ResponseEntity.ok().body(produtoService.getAll());
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<Produto>> getAllProdutosInativos() {
        return ResponseEntity.ok().body(produtoService.getAllInativos());
    }

    @GetMapping("/descricao/{descricao}")
    public ResponseEntity<List<Produto>> getAllProdutosByDescricao(@RequestParam String descricao) {
        return ResponseEntity.ok().body(produtoService.getAllByDescricao(descricao));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<Produto> getById(@PathVariable int id) {
        return ResponseEntity.ok().body(produtoService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Produto> save(@RequestBody ProdutoDTO produtoDto) {
        return ResponseEntity.ok().body(produtoService.save(produtoDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> update(@PathVariable int id, @RequestBody ProdutoDTO produtoDto) {
        return ResponseEntity.ok().body(produtoService.update(id, produtoDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}
