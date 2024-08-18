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

import com.ferragem.avila.pdv.dto.ItemDto;
import com.ferragem.avila.pdv.dto.DataBetweenDto;
import com.ferragem.avila.pdv.dto.VendaDto;
import com.ferragem.avila.pdv.dto.VendedorDto;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.model.enums.VendaExclusaoResultado;
import com.ferragem.avila.pdv.service.interfaces.VendaService;

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

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Page<Venda>> getAllConcluidas(Pageable pageable) {
        return ResponseEntity.ok(vendaService.getAll(pageable));
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Venda> getById(@PathVariable long id) {
        return ResponseEntity.ok(vendaService.getById(id));
    }

    @PostMapping("/relatorio/data")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Page<Venda>> getBetweenDates(Pageable pageable, @RequestBody @Valid DataBetweenDto datas) {
        return ResponseEntity
                .ok(vendaService.getBetweenDataConclusao(pageable, datas.dataHoraInicio(), datas.dataHoraFim()));
    }

    @GetMapping("/ativa/itens")
    public ResponseEntity<List<Item>> getItens() {
        return ResponseEntity.ok(vendaService.getItensFromVendaAtiva());
    }

    @GetMapping("/ativa")
    public ResponseEntity<Optional<Venda>> getVendaAtiva() {
        return ResponseEntity.ok(vendaService.getVendaAtiva());
    }

    @PostMapping("/add-item")
    public ResponseEntity<Venda> addItem(@RequestBody @Valid ItemDto item, JwtAuthenticationToken token) {
        VendedorDto vendedor = new VendedorDto(
            UUID.fromString(token.getName()),
            token.getTokenAttributes().get("nome").toString()
        );

        return ResponseEntity.ok(vendaService.addItem(item, vendedor));
    }

    @PostMapping("/add-item/codigo-barras")
    public ResponseEntity<Venda> addItem(@RequestParam String codigoBarras, JwtAuthenticationToken token) {
        VendedorDto vendedor = new VendedorDto(
            UUID.fromString(token.getName()),
            token.getTokenAttributes().get("nome").toString()
        );
        
        return ResponseEntity.ok(vendaService.addItem(codigoBarras, vendedor));
    }

    @PostMapping("/add-itens/lista")
    public ResponseEntity<Venda> addItem(@RequestBody @Valid List<ItemDto> itens, JwtAuthenticationToken token) {
        VendedorDto vendedor = new VendedorDto(
            UUID.fromString(token.getName()),
            token.getTokenAttributes().get("nome").toString()
        );
        
        return ResponseEntity.ok(vendaService.addItem(itens, vendedor));
    }

    @PostMapping("/edit-item/{itemId}")
    public ResponseEntity<Venda> editItem(@RequestParam long itemId, @RequestParam @PositiveOrZero float quantidade) {
        return ResponseEntity.ok(vendaService.editItem(itemId, quantidade));
    }

    @DeleteMapping("/remover-item/{itemId}")
    public ResponseEntity<Venda> removeItem(@RequestParam long itemId) {
        return ResponseEntity.ok(vendaService.removeItem(itemId));
    }

    @PostMapping("/concluir")
    public ResponseEntity<Venda> concluir(@RequestBody @Valid VendaDto dto) {
        vendaService.concluirVenda(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelar() {
        vendaService.cancel();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> delete(Long id) {
        VendaExclusaoResultado deletada = vendaService.delete(id);

        switch (deletada) {
            case DELETADA:
                return ResponseEntity.ok("Venda deletada com sucesso!");
            case NAO_CONCLUIDA:
                return ResponseEntity.badRequest().body("Venda não concluida, cancele a venda ao invés de deletar");
            case NAO_EXISTE:
                return ResponseEntity.badRequest().body("Venda não existe.");
            default:
                return ResponseEntity.internalServerError().body("Erro ao tentar deletar a venda, entre em contato com o suporte.");
        }
    }

}
