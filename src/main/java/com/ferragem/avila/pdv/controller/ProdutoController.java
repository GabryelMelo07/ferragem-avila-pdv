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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferragem.avila.pdv.dto.ProdutoDto;
import com.ferragem.avila.pdv.dto.UpdateProdutoDto;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.service.ProdutoService;
import com.ferragem.avila.pdv.utils.api_responses_examples.ProdutoResponses;
import com.ferragem.avila.pdv.utils.csv_product_conversion.ProdutosFromCsv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
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

    @Operation(summary = "Buscar todos os produtos ativos", description = """
            Este recurso realiza uma consulta no banco de dados trazendo páginas de produtos que têm a flag "ativo" como VERDADEIRO
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_ALL_PRODUTOS_ATIVOS)))
    })
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

    @Operation(summary = "Buscar todos os produtos inativos", description = """
            Este recurso realiza uma consulta no banco de dados trazendo páginas de produtos que têm a flag "ativo" como FALSO
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_ALL_PRODUTOS_INATIVOS)))
    })
    @GetMapping("/inativos")
    public ResponseEntity<Page<Produto>> getAllProdutosInativos(Pageable pageable) {
        return ResponseEntity.ok().body(produtoService.getAllInativos(pageable));
    }

    @Operation(summary = "Buscar produtos por parâmetro", description = """
            Este recurso realiza uma consulta no banco de dados, retornando páginas de produtos que têm a flag "ativo" como VERDADEIRO
            Ele filtra por código de barras, caso exista algum produto com aquele código
            Caso contrário, o filtro é feito por descrição, trazendo todos os produtos que contêm o parâmetro como parte da descrição (utilizando a cláusula LIKE)
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_ALL_PRODUTOS_ATIVOS)))
    })
    @GetMapping("/buscar")
    public ResponseEntity<Page<Produto>> buscarProdutosPorParametro(Pageable pageable, @RequestParam String parametro) {
        return ResponseEntity.ok().body(produtoService.findByParams(pageable, parametro));
    }

    @Operation(summary = "Buscar produto por ID", description = """
            Este recurso realiza uma consulta no banco de dados, retornando o produto caso ele tenha a flag "ativo" como VERDADEIRO e filtrando pelo ID passado como parâmetro
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_BY_ID)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Produto> getById(@PathVariable int id) {
        return ResponseEntity.ok().body(produtoService.getById(id));
    }

    @Operation(summary = "Cadastrar novo produto", description = """
            Este recurso só pode ser usado por usuários administradores e cadastra um novo produto no banco de dados
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_BY_ID)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Produto> save(@Valid @ModelAttribute ProdutoDto produtoDto) {
        return ResponseEntity.status(201).body(produtoService.save(produtoDto));
    }

    @Operation(summary = "Cadastrar produtos via arquivo CSV", description = """
            Este recurso só pode ser usado por usuários administradores e cadastra uma lista de produtos via arquivo CSV
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(path = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> saveFromCsv(@RequestPart MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            produtoService.importarProdutosCsv(file);
            return ResponseEntity.ok("Importando produtos do arquivo CSV: " + fileName);
        } catch (IOException e) {
            log.error("Erro ao processar o arquivo CSV 'IOException': ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar o arquivo CSV: " + fileName);
        }
    }

    @Operation(summary = "Buscar o resultado da importação de arquivos via CSV", description = """
            Este recurso só pode ser usado por usuários administradores e busca o resultado da importação de arquivos via CSV do cache Redis,
            informando se obteve sucesso ou erro ao cadastrar cada produto
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json"))
    })
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

        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Atualizar produto", description = """
            Este recurso só pode ser usado por usuários administradores e atualiza o registro de um produto no banco de dados a partir do id dele
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_BY_ID)))
    })
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Produto> update(@PathVariable int id, @Valid @ModelAttribute UpdateProdutoDto produtoDto) {
        return ResponseEntity.ok().body(produtoService.update(id, produtoDto));
    }

    @Operation(summary = "Atualizar estoque de produto", description = """
            Este recurso só pode ser usado por usuários administradores e atualiza a quantidade em estoque de um produto no banco de dados a partir do id dele
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ProdutoResponses.GET_BY_ID)))
    })
    @PatchMapping("/{id}/editar-estoque")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Produto> updateInventory(@PathVariable int id,
            @PositiveOrZero @RequestParam Float novoEstoque) {
        return ResponseEntity.ok().body(produtoService.updateEstoque(id, novoEstoque));
    }

    @Operation(summary = "Deletar produto", description = """
            Este recurso só pode ser usado por usuários administradores e deleta o registro de um produto no banco de dados a partir do id dele
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
