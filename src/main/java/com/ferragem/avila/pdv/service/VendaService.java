package com.ferragem.avila.pdv.service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferragem.avila.pdv.dto.venda.GraficoVendasDto;
import com.ferragem.avila.pdv.dto.venda.ItemDto;
import com.ferragem.avila.pdv.dto.venda.VendaDto;
import com.ferragem.avila.pdv.dto.venda.VendasDiariasDto;
import com.ferragem.avila.pdv.dto.venda.VendedorDto;
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.ProdutoSemEstoqueException;
import com.ferragem.avila.pdv.exceptions.VendaNotFoundException;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.model.enums.VendaExclusaoResultado;
import com.ferragem.avila.pdv.repository.VendaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ItemService itemService;
    private final ProdutoService produtoService;

    public VendaService(VendaRepository vendaRepository, ItemService itemService, ProdutoService produtoService) {
        this.vendaRepository = vendaRepository;
        this.itemService = itemService;
        this.produtoService = produtoService;
    }

    @Cacheable(value = "vendas", key = "'page_' + #pageable.pageNumber", unless = "#result == null or #result.isEmpty()")
    public Page<Venda> getAll(Pageable pageable) {
        return vendaRepository.findByConcluidaTrueOrderByDataHoraConclusaoDesc(pageable);
    }

    public Venda getById(long id) {
        return vendaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Venda não existe."));
    }

    @Cacheable(value = "vendas_between_datas", key = "'page_' + #pageable.pageNumber", unless = "#result == null or #result.isEmpty()")
    public Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim) {
        return vendaRepository.findByDataHoraConclusaoBetween(pageable, dataInicio, dataFim);
    }

    public List<Item> getItensFromVendaAtiva() {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaNotFoundException());
        return venda.getItens();
    }

    public GraficoVendasDto getGraficoMensalVendas() {
        LocalDate dataAtual = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        YearMonth mesAno = YearMonth.from(dataAtual);
        return getGraficoData(dataAtual, dataAtual.withDayOfMonth(1), mesAno.atEndOfMonth());
    }

    public GraficoVendasDto getGraficoSemanalVendas() {
        LocalDate dataAtual = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        return getGraficoData(dataAtual, dataAtual.with(DayOfWeek.MONDAY), dataAtual.with(DayOfWeek.SUNDAY));
    }

    private GraficoVendasDto getGraficoData(LocalDate dataAtual, LocalDate dataInicial, LocalDate dataFinal) {
        GraficoVendasDto graficoVendas = new GraficoVendasDto(dataAtual);

        while (dataInicial.isBefore(dataFinal) || dataInicial.isEqual(dataFinal)) {
            int pagina = 0;
            int tamanhoPagina = 1000;
            Pageable pageable = PageRequest.of(pagina, tamanhoPagina);
            Page<Venda> paginaVendas;

            BigDecimal valorTotalVendas = BigDecimal.ZERO;
            BigDecimal lucroTotalVendas = BigDecimal.ZERO;

            do {
                paginaVendas = vendaRepository.findByDataHoraConclusaoBetween(pageable, dataInicial, dataInicial);

                for (Venda venda : paginaVendas.getContent()) {
                    valorTotalVendas = valorTotalVendas.add(venda.getPrecoTotal());
                    lucroTotalVendas = lucroTotalVendas.add(venda.calcularLucroTotal());
                }

                pageable = PageRequest.of(++pagina, tamanhoPagina);
            } while (paginaVendas.hasNext());

            graficoVendas.getVendasDiarias().add(new VendasDiariasDto(dataInicial, valorTotalVendas, lucroTotalVendas));
            dataInicial = dataInicial.plusDays(1);
        }

        return graficoVendas;
    }

    public Venda save(Venda v) {
        return vendaRepository.save(v);
    }

    public Optional<Venda> getVendaAtiva() {
        return vendaRepository.findByConcluidaFalse();
    }

	@CacheEvict(value = { "produtos_ativos" }, allEntries = true)
    public void cancel() {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaNotFoundException());
        List<Item> itens = venda.getItens();

        for (Item item : itens) {
			produtoService.restoreStock(item.getProduto(), item.getQuantidade());
        }

        itemService.deleteAll(itens);
        vendaRepository.deleteById(venda.getId());
    }

    @CacheEvict(value = { "vendas", "vendas_between_datas", "produtos_ativos", "produtos_inativos" }, allEntries = true)
    public VendaExclusaoResultado delete(Long id) {
        if (vendaRepository.existsById(id)) {
            Venda venda = getById(id);

            if (venda.isConcluida()) {
                for (Item item : venda.getItens()) {
                    Long produtoId = item.getProduto().getId();
                    Produto p = produtoService.getById(produtoId);
                    p.sumEstoque(item.getQuantidade());
                    produtoService.save(p);
                }

                vendaRepository.delete(venda);
                return VendaExclusaoResultado.DELETADA;
            } else {
                return VendaExclusaoResultado.NAO_CONCLUIDA;
            }
        }

        return VendaExclusaoResultado.NAO_EXISTE;
    }

    @Transactional
    public Venda addItem(ItemDto itemDto, VendedorDto vendedor) {
        long produtoId = itemDto.produtoId();
        float itemQtd = itemDto.quantidade();

        Venda venda = getVendaAtiva().orElseGet(() -> {
            Venda novaVenda = new Venda();
            novaVenda.setVendedorId(vendedor.id());
            novaVenda.setVendedorNome(vendedor.nome());
            return save(novaVenda);
        });

        Produto produto = produtoService.getById(produtoId);            

        if (venda.getItem(produtoId).isPresent()) {
            Item item = venda.getItem(produtoId).get();
            item.sumQuantidade(itemQtd);
            item.calcularPrecoTotal();
        } else {
            Item item = new Item(itemQtd, produto, venda);
            venda.addItem(item);
        }
                    
        produto.subtractEstoque(itemQtd);
        produtoService.save(produto);
        venda.calcularPrecoTotal();
        
        return vendaRepository.save(venda);
    }

    public Venda addItem(String codigoBarras, VendedorDto vendedor) {
        if (!codigoBarras.matches("^\\d{13}$")) {
            throw new CodigoBarrasInvalidoException();
        }

        return addItem(new ItemDto(1.0f, produtoService.getIdByCodigoBarras(codigoBarras)), vendedor);
    }

    public Venda addItem(List<ItemDto> itensDto, VendedorDto vendedor) {
        for (ItemDto itemDTO : itensDto) {
            addItem(itemDTO, vendedor);
        }

        return getVendaAtiva().get();
    }

    @Transactional
    public Venda editItem(long itemId, float quantidade) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaNotFoundException());
        Item item = itemService.getById(itemId);
        Produto produto = item.getProduto();

        float estoqueAtual = produto.getEstoque() + item.getQuantidade();
        float novoEstoque = estoqueAtual - quantidade;

        if (novoEstoque < 0)
            throw new ProdutoSemEstoqueException();

        produto.setEstoque(novoEstoque);
        item.setQuantidade(quantidade);
        venda.getItens().remove(item);
        venda.getItens().add(item);

        item.calcularPrecoTotal();
        venda.calcularPrecoTotal();

        produtoService.save(produto);
        return save(venda);
    }

    public Venda removeItem(long itemId) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaNotFoundException());
        Item item = itemService.getById(itemId);
        Produto produto = item.getProduto();

        venda.getItens().remove(item);
        produto.setEstoque(produto.getEstoque() + item.getQuantidade());

        produtoService.save(produto);
        itemService.delete(item);
        venda.calcularPrecoTotal();
        return save(venda);
    }

    @CacheEvict(value = { "vendas", "vendas_between_datas", "produtos_ativos" }, allEntries = true)
    public Venda concluirVenda(VendaDto dto) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaNotFoundException());

        if (dto.dataHoraConclusao().isBefore(venda.getDataHoraInicio()))
            throw new DateTimeException("A data de conclusão da venda não pode ser anterior a data de início.");

        venda.setDataHoraConclusao(dto.dataHoraConclusao());
        venda.setFormaPagamento(dto.formaPagamento());
        venda.setConcluida(true);
        return save(venda);
    }

}
