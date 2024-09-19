package com.ferragem.avila.pdv.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferragem.avila.pdv.dto.ProdutoDto;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.utils.ProdutosFromCsv;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/produto")
@Slf4j
public class ProdutoController {
    
    private final ProdutoService produtoService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${import-csv.redis.key}")
    private String importCsvRedisKey;

    public ProdutoController(ProdutoService produtoService, RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.produtoService = produtoService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/ativos")
    public ResponseEntity<Page<Produto>> getAllProdutosAtivos(Pageable pageable, Direction direction) {
        if (direction == Direction.DESC) {
            Sort originalSort = pageable.getSort();
            Sort sort = Sort.by(originalSort.stream()
                    .map(order -> new Sort.Order(direction, order.getProperty()))
                    .toList());

            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }
        
        return ResponseEntity.ok().body(produtoService.getAll(pageable));
    }

    @GetMapping("/inativos")
    public ResponseEntity<Page<Produto>> getAllProdutosInativos(Pageable pageable) {
        return ResponseEntity.ok().body(produtoService.getAllInativos(pageable));
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Produto>> buscarProdutosPorParametro(Pageable pageable, @RequestParam String parametro) {
        return ResponseEntity.ok().body(produtoService.findByParams(pageable, parametro));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> getById(@PathVariable int id) {
        return ResponseEntity.ok().body(produtoService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Produto> save(@Valid @RequestBody ProdutoDto produtoDto) {
        return ResponseEntity.ok().body(produtoService.save(produtoDto));
    }

    @PostMapping(path = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> saveFromCsv(@RequestPart MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            produtoService.importarProdutosCsv(file);
            return ResponseEntity.ok("Importando produtos do arquivo CSV: " + fileName);
        } catch (IOException e) {
            log.error("Erro ao processar o arquivo CSV 'IOException': ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo CSV: " + fileName);
        }
    }

    @GetMapping("/importar-csv/resultado")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<ProdutosFromCsv> getSaveFromCsvResult() {
        if (redisTemplate.hasKey(importCsvRedisKey)) {
            String redisValue = (String) redisTemplate.opsForValue().get(importCsvRedisKey);
            ProdutosFromCsv resultado = new ProdutosFromCsv();

            try {
                resultado = objectMapper.readValue(redisValue, ProdutosFromCsv.class);
            } catch (JsonMappingException e) {
                e.printStackTrace();
                log.error("Erro ao mapear o objeto do Redis (String) para a classe 'ProdutosFromCsv': ", e);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("Erro ao mapear o objeto do Redis (String) para a classe 'ProdutosFromCsv': ", e);
            }

            return ResponseEntity.ok(resultado);
        }
        
        return ResponseEntity.internalServerError().build();
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Produto> update(@PathVariable int id,  @Valid @RequestBody ProdutoDto produtoDto) {
        return ResponseEntity.ok().body(produtoService.update(id, produtoDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}
