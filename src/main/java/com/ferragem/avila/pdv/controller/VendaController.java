package com.ferragem.avila.pdv.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ferragem.avila.pdv.dto.venda.DataBetweenDto;
import com.ferragem.avila.pdv.dto.venda.ItemDto;
import com.ferragem.avila.pdv.dto.venda.VendaDto;
import com.ferragem.avila.pdv.dto.venda.VendedorDto;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.model.enums.VendaExclusaoResultado;
import com.ferragem.avila.pdv.service.VendaService;
import com.ferragem.avila.pdv.utils.api_responses_examples.VendaResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/venda")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @Operation(summary = "Buscar vendas concluidas", description = """
            Este recurso só pode ser usado por usuários administradores e busca todos os registros de vendas que foram concluídas de forma paginada
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET_ALL_CONCLUIDAS)))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Page<Venda>> getAllConcluidas(Pageable pageable) {
        return ResponseEntity.ok(vendaService.getAll(pageable));
    }

    @Operation(summary = "Buscar venda", description = """
            Este recurso só pode ser usado por usuários administradores e busca o registro de uma venda no banco de dados a partir do id dela
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET_BY_ID)))
    })
    @GetMapping("/id/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Venda> getById(@PathVariable long id) {
        return ResponseEntity.ok(vendaService.getById(id));
    }

    @Operation(summary = "Buscar vendas entre duas datas", description = """
            Este recurso só pode ser usado por usuários administradores e busca todas as vendas, de forma paginada, dentre duas datas
            Para esta busca é considerada a data de conclusão da venda
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET_BETWEEN_DATES)))
    })
    @PostMapping("/relatorio/data")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Page<Venda>> getBetweenDates(Pageable pageable, @RequestBody @Valid DataBetweenDto datas) {
        return ResponseEntity
                .ok(vendaService.getBetweenDataConclusao(pageable, datas.dataHoraInicio(), datas.dataHoraFim()));
    }

    @Operation(summary = "Buscar todos os itens de uma venda ativa", description = """
            Este recurso busca uma lista de todos os itens de uma venda ativa
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET_ITENS)))
    })
    @GetMapping("/ativa/itens")
    public ResponseEntity<List<Item>> getItens() {
        return ResponseEntity.ok(vendaService.getItensFromVendaAtiva());
    }

    @Operation(summary = "Busca a venda ativa atualmente", description = """
            Este recurso a venda ativa atualmente, caso ela exista
            Só pode ter uma venda ativa por vez no PDV, NUNCA existirá mais de uma em aberto
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET)))
    })
    @GetMapping("/ativa")
    public ResponseEntity<Optional<Venda>> getVendaAtiva() {
        return ResponseEntity.ok(vendaService.getVendaAtiva());
    }

    @Operation(summary = "Adicionar um item na venda ativa", description = """
            Este recurso adiciona um item na venda ativa atualmente, caso o item já esteja lá ele só aumenta a quantidade caso tenha a quantidade solicitada em estoque
            Caso não tenha em estoque a quantidade solicitada, irá gerar um erro BAD_REQUEST
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET)))
    })
    @PostMapping("/add-item")
    public ResponseEntity<Venda> addItem(@RequestBody @Valid ItemDto item, JwtAuthenticationToken token) {
        VendedorDto vendedor = new VendedorDto(
                UUID.fromString(token.getName()),
                token.getTokenAttributes().get("nome").toString());

        return ResponseEntity.ok(vendaService.addItem(item, vendedor));
    }

    @Operation(summary = "Adicionar um item na venda ativa por código de barras do produto", description = """
            Este recurso adiciona um item na venda ativa atualmente, pelo código de barras do produto, caso o item já esteja lá ele só aumenta a quantidade caso tenha a quantidade solicitada em estoque
            Caso não tenha em estoque a quantidade solicitada, irá gerar um erro BAD_REQUEST
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET)))
    })
    @PostMapping("/add-item/codigo-barras")
    public ResponseEntity<Venda> addItem(@RequestParam String codigoBarras, JwtAuthenticationToken token) {
        VendedorDto vendedor = new VendedorDto(
                UUID.fromString(token.getName()),
                token.getTokenAttributes().get("nome").toString());

        return ResponseEntity.ok(vendaService.addItem(codigoBarras, vendedor));
    }

    @Operation(summary = "Adicionar uma lista de itens na venda ativa", description = """
            Este recurso adiciona uma lista de itens na venda ativa atualmente, caso os itens já estejam lá ele só aumenta a quantidade caso tenha a quantidade solicitada em estoque
            Caso não tenha em estoque a quantidade solicitada, irá gerar um erro BAD_REQUEST
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET)))
    })
    @PostMapping("/add-itens/lista")
    public ResponseEntity<Venda> addItem(@RequestBody @Valid List<ItemDto> itens, JwtAuthenticationToken token) {
        VendedorDto vendedor = new VendedorDto(
                UUID.fromString(token.getName()),
                token.getTokenAttributes().get("nome").toString());

        return ResponseEntity.ok(vendaService.addItem(itens, vendedor));
    }

    @Operation(summary = "Editar a quantidade de um item na venda ativa", description = """
            Este recurso edita a quantidade de um item na venda ativa atualmente
            Caso não tenha em estoque a quantidade solicitada, irá gerar um erro BAD_REQUEST
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET)))
    })
    @PostMapping("/edit-item/{itemId}")
    public ResponseEntity<Venda> editItem(@PathVariable long itemId, @RequestParam @PositiveOrZero float quantidade) {
        return ResponseEntity.ok(vendaService.editItem(itemId, quantidade));
    }

    @Operation(summary = "Remover um item da venda ativa", description = """
            Este recurso remove um item da venda ativa
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = VendaResponses.GET)))
    })
    @DeleteMapping("/remover-item/{itemId}")
    public ResponseEntity<Venda> removeItem(@PathVariable long itemId) {
        return ResponseEntity.ok(vendaService.removeItem(itemId));
    }

    @Operation(summary = "Concluir a venda ativa", description = """
            Este recurso conclui a venda ativa atualmente
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/concluir")
    public ResponseEntity<Venda> concluir(@RequestBody @Valid VendaDto dto) {
        return ResponseEntity.ok().body(vendaService.concluirVenda(dto));
    }

    @Operation(summary = "Cancelar a venda ativa", description = """
            Este recurso cancela a venda ativa atualmente
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelar() {
        vendaService.cancel();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deletar um registro de venda", description = """
            Este recurso só pode ser usado por usuários administradores e deleta completamente o registro de uma venda concluída do banco de dados
            Caso a venda ainda não esteja concluída, irá gerar um erro BAD_REQUEST e informar para usar o recurso de cancelar a venda ao invés de deletar
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Venda deletada com sucesso!"))),
    })
    @DeleteMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> delete(Long id) {
        VendaExclusaoResultado deletada = vendaService.delete(id);

        switch (deletada) {
            case DELETADA:
                return ResponseEntity.ok("Venda deletada com sucesso!");
            case NAO_CONCLUIDA:
                return ResponseEntity.badRequest().body("Venda não concluída, cancele a venda ao invés de deletar");
            case NAO_EXISTE:
                return ResponseEntity.badRequest().body("Venda não existe.");
            default:
                return ResponseEntity.internalServerError()
                        .body("Erro ao tentar deletar a venda, entre em contato com o suporte.");
        }
    }

}
