package com.ferragem.avila.pdv.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ferragem.avila.pdv.dto.venda.GraficoVendasDto;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.service.ProdutoService;
import com.ferragem.avila.pdv.service.VendaService;
import com.ferragem.avila.pdv.utils.api_responses_examples.DashboardResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
@Slf4j
public class DashboardController {

    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final RedisTemplate<String, Object> redisTemplate;

    // Chave para inserir e buscar o resultado do processamento assíncrono do upload
    // de produtos via CSV.
    private final String RELATORIO_PRODUTOS_KEY = "produtos_ativos::relatorio";

    /**
     * Este endpoint irá verificar se existe a chave "produtos_ativos::relatorio" no
     * Redis.
     * Caso exista, ele retorna o valor dela com o relatório gerado.
     * Caso não exista, irá chamar um método ASSÍNCRONO, que gera o relatório a
     * partir do banco de dados,
     * e entregará a resposta da API de forma antecipada, antes do fim do
     * carregamento do relatório.
     * 
     * Para acessar o resultado do relatório, este método deverá ser chamado
     * novamente, pois o resultado
     * estará cacheado, e não será processado novamente.
     * 
     * Caso seja adicionado ou removido um produto do banco de dados, o cache é
     * limpo e será carregado novamente.
     * O cache do relatório dura 3h.
     * 
     * Esta estratégia foi definida pois as informações deste método podem levar
     * horas para serem carregadas,
     * dependendo da quantidade de registros da tabela "produto" no banco de dados.
     * 
     * @return Retorna código 200 quando chamado pela primeira vez, na segunda vez
     *         retorna o download do arquivo gerado."
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Operation(summary = "Gerar relatório geral de produtos", description = """
            Este recurso só pode ser usado por usuários administradores e gera um relatório no formato XLSX de todos os produtos cadastrados no banco de dados.

            Na primeira vez em que o recurso for consumido, irá gerar o relatório de forma assíncrona, inserir no cache do Redis o nome do arquivo no Object Storage
            e, após isso, enviar uma mensagem para o sistema de pub/sub do Redis informando do término da geração do relatório.

            Na segunda vez em que o recurso for consumido (após a geração do relatório), ele irá buscar o relatório no Object Storage e disponibilizar o download.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Processing", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Gerando relatório"))),
            @ApiResponse(responseCode = "200", description = "Baixar relatório", content = @Content(mediaType = "application/octet-stream")),
    })
    @GetMapping("/produtos/relatorio-produtos")
    public ResponseEntity<?> getRelatorioProdutos() {
        if (redisTemplate.hasKey(RELATORIO_PRODUTOS_KEY)) {
            String redisValue = (String) redisTemplate.opsForValue().get(RELATORIO_PRODUTOS_KEY);
            byte[] relatorio = produtoService.getRelatorioGerado(redisValue);

			if (relatorio == null) {
				redisTemplate.delete(RELATORIO_PRODUTOS_KEY);
				return ResponseEntity.status(HttpStatus.GONE)
						.body("Relatório expirado ou não encontrado, gere novamente.");
			}

			redisTemplate.delete(RELATORIO_PRODUTOS_KEY);
			produtoService.deletarRelatorioConsumido(redisValue);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + redisValue + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(relatorio);
        }

        produtoService.gerarRelatorioProdutosGeral(RELATORIO_PRODUTOS_KEY);
        return ResponseEntity.status(202).contentType(MediaType.APPLICATION_JSON).body("Gerando relatório");
    }

    @Operation(summary = "Buscar produtos com baixo estoque", description = """
            Este recurso só pode ser usado por usuários administradores e realiza uma consulta no banco de dados trazendo páginas de produtos que tenham 10 ou menos em estoque
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = DashboardResponses.GET_PRODUTOS_BAIXO_ESTOQUE)))
    })
    @GetMapping("/produtos/baixo-estoque")
    public ResponseEntity<Page<Produto>> getProdutosBaixoEstoque(Pageable pageable) {
        return ResponseEntity.ok(produtoService.getProdutosBaixoEstoque(pageable));
    }

    @Operation(summary = "Buscar produtos mais vendidos por mês", description = """
            Este recurso só pode ser usado por usuários administradores e realiza uma consulta no banco de dados trazendo uma lista dos 5 produtos mais vendidos no mês passado como parâmetro
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = DashboardResponses.GET_MAIS_VENDIDOS_MES)))
    })
    @GetMapping("/produtos/mais-vendidos")
    public ResponseEntity<List<Produto>> getMaisVendidosMes(@RequestParam LocalDate data) {
        return ResponseEntity.ok(produtoService.getMaisVendidosMes(data));
    }

    @Operation(summary = "Buscar relatório de vendas mensais", description = """
            Este recurso só pode ser usado por usuários administradores e realiza uma consulta no banco de dados trazendo informações das vendas do mês atual para montagem do gráfico mensal
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = DashboardResponses.GET_GRAFICO_VENDAS)))
    })
    @GetMapping("/vendas/grafico-mensal")
    public ResponseEntity<GraficoVendasDto> getGraficoMensalVendas() {
        return ResponseEntity.ok(vendaService.getGraficoMensalVendas());
    }

    @Operation(summary = "Buscar relatório de vendas semanais", description = """
            Este recurso só pode ser usado por usuários administradores e realiza uma consulta no banco de dados trazendo informações das vendas da semana atual para montagem do gráfico semanal
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = DashboardResponses.GET_GRAFICO_VENDAS)))
    })
    @GetMapping("/vendas/grafico-semanal")
    public ResponseEntity<GraficoVendasDto> getGraficoSemanalVendas() {
        return ResponseEntity.ok(vendaService.getGraficoSemanalVendas());
    }

}
