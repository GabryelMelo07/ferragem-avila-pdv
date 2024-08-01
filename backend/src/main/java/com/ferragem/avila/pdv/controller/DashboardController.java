package com.ferragem.avila.pdv.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
public class DashboardController {

    @Autowired
    private ProdutoService produtoService;

    // @Autowired
    // private VendaService vendaService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Chave para inserir e buscar o resultado do processamento assíncrono do upload de produtos via CSV.
    private final String RELATORIO_PRODUTOS_KEY = "produtos_ativos::relatorio";

    /**
     * Este endpoint irá verificar se existe a chave "produtos_ativos::relatorio" no Redis.
     * Caso exista, ele retorna o valor dela com o relatório já carregado do banco de dados.
     * Caso não exista, irá chamar o método ASSÍNCRONO "getAllWithoutPagination()", que carrega
     * o relatório do banco de dados, e entregará a resposta da API de forma antecipada, antes
     * do fim do carregamento do relatório.
     * 
     * Para acessar o resultado do relatório, este método deverá ser chamado novamente, pois o resultado
     * estará cacheado, e não será processado novamente.
     * 
     * Caso seja adicionado ou removido um produto do banco de dados, o cache é limpo e será carregado novamente. 
     * O cache dura 12h por padrão (configurado na classe "com.ferragem.avila.pdv.config.RedisConfig.java")
     * 
     * Estas estratégias foram definidas pois as informações deste método podem levar ATÉ horas para ser carregado,
     * dependendo da quantidade de registros da tabela "produto" no banco de dados.
     * 
     * @return Retorna uma listagem de todos produtos do banco de dados. "List<Produto> ou uma Lista vazia (enquanto carrega os dados)"
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @GetMapping("/produtos/relatorio-geral")
    public ResponseEntity<List<Produto>> getRelatorioProdutos() throws JsonMappingException, JsonProcessingException {
        if (redisTemplate.hasKey(RELATORIO_PRODUTOS_KEY)) {
            String redisValue = (String) redisTemplate.opsForValue().get(RELATORIO_PRODUTOS_KEY);
            List<Produto> relatorioGeral = objectMapper.readValue(redisValue, new TypeReference<List<Produto>>() {});
            return ResponseEntity.ok(relatorioGeral);
        }
        
        produtoService.gerarRelatorioGeral(RELATORIO_PRODUTOS_KEY);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/produtos/baixo-estoque")
    public ResponseEntity<Page<Produto>> getProdutosBaixoEstoque(Pageable pageable) {
        return ResponseEntity.ok(produtoService.getProdutosBaixoEstoque(pageable));
    }

    @GetMapping("/produtos/mais-vendidos")
    public ResponseEntity<List<Produto>> getMaisVendidosMes(@RequestParam LocalDate data) {
        return ResponseEntity.ok(produtoService.getMaisVendidosMes(data));
    }
    
}
