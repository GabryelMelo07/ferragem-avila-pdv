package com.ferragem.avila.pdv.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ferragem.avila.pdv.dto.ProdutoDTO;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.utils.ProdutosFromCsv;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequestMapping("/produto")
public class ProdutoController {
    
    @Autowired
    private ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/ativos")
    public ResponseEntity<Page<Produto>> getAllProdutosAtivos(Pageable pageable) {
        return ResponseEntity.ok().body(produtoService.getAll(pageable));
    }

    @GetMapping("/inativos")
    public ResponseEntity<Page<Produto>> getAllProdutosInativos(Pageable pageable) {
        return ResponseEntity.ok().body(produtoService.getAllInativos(pageable));
    }

    @GetMapping("/descricao/{descricao}")
    public ResponseEntity<Page<Produto>> getAllProdutosByDescricao(Pageable pageable, @PathVariable String descricao) {
        return ResponseEntity.ok().body(produtoService.getAllByDescricao(pageable, descricao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> getById(@PathVariable int id) {
        return ResponseEntity.ok().body(produtoService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Produto> save(@Valid @RequestBody ProdutoDTO produtoDto) {
        return ResponseEntity.ok().body(produtoService.save(produtoDto));
    }

    @PostMapping(path = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutosFromCsv> saveFromCsv(@RequestPart MultipartFile file) {
        try {
            return ResponseEntity.ok().body(produtoService.saveProductsFromCsv(file));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> update(@PathVariable int id,  @Valid @RequestBody ProdutoDTO produtoDto) {
        return ResponseEntity.ok().body(produtoService.update(id, produtoDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}
