package com.ferragem.avila.pdv.service;

import java.math.BigDecimal;
import java.time.DateTimeException;
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

import com.ferragem.avila.pdv.dto.GraficoVendasDto;
import com.ferragem.avila.pdv.dto.ItemDto;
import com.ferragem.avila.pdv.dto.VendaDto;
import com.ferragem.avila.pdv.dto.VendedorDto;
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.ProdutoSemEstoqueException;
import com.ferragem.avila.pdv.exceptions.VendaInativaException;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.model.enums.VendaExclusaoResultado;
import com.ferragem.avila.pdv.repository.VendaRepository;
import com.ferragem.avila.pdv.service.interfaces.ItemService;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;
import com.ferragem.avila.pdv.service.interfaces.VendaService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VendaServiceImpl implements VendaService {

    private final VendaRepository vendaRepository;
    private final ItemService itemService;
    private final ProdutoService produtoService;

    public VendaServiceImpl(VendaRepository vendaRepository, ItemService itemService, ProdutoService produtoService) {
        this.vendaRepository = vendaRepository;
        this.itemService = itemService;
        this.produtoService = produtoService;
    }

    @Override
    @Cacheable(value = "vendas", key = "'page_' + #pageable.pageNumber", unless = "#result == null or #result.isEmpty()")
    public Page<Venda> getAll(Pageable pageable) {
        return vendaRepository.findAll(pageable);
    }

    @Override
    public Venda getById(long id) {
        return vendaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Venda não existe."));
    }

    @Override
    @Cacheable(value = "vendas_between_datas", key = "'page_' + #pageable.pageNumber", unless = "#result == null or #result.isEmpty()")
    public Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim) {
        return vendaRepository.findByDataHoraConclusaoBetween(pageable, dataInicio, dataFim);
    }

    @Override
    public List<Item> getItensFromVendaAtiva() {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
        return venda.getItens();
    }
    
    @Override
    public GraficoVendasDto getGraficoMensalVendas() {
        LocalDate dataAtual = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        YearMonth mesAno = YearMonth.from(dataAtual);

        int pagina = 0;
        int tamanhoPagina = 1000;

        Pageable pageable = PageRequest.of(pagina, tamanhoPagina);
        Page<Venda> paginaVendas;

        BigDecimal valorTotalVendas = BigDecimal.ZERO;
        BigDecimal lucroTotalVendas = BigDecimal.ZERO;

        do {
            paginaVendas = vendaRepository.findByDataHoraConclusaoBetween(pageable, dataAtual.withDayOfMonth(1), mesAno.atEndOfMonth());

            for (Venda venda : paginaVendas.getContent()) {
                valorTotalVendas = valorTotalVendas.add(venda.getPrecoTotal());
                lucroTotalVendas = lucroTotalVendas.add(venda.calcularLucroTotal());
            }

            pageable = PageRequest.of(++pagina, tamanhoPagina);
            
        } while (paginaVendas.hasNext());
        
        return new GraficoVendasDto(dataAtual, valorTotalVendas, lucroTotalVendas);
    }

    @Override
    public Venda save(Venda v) {
        return vendaRepository.save(v);
    }

    @Override
    public Optional<Venda> getVendaAtiva() {
        return vendaRepository.findByConcluidaFalse();
    }

    @Override
    public void cancel() {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
        List<Item> itens = venda.getItens();

        for (Item item : itens) {
            item.getProduto().sumEstoque(item.getQuantidade());
        }

        itemService.deleteAll(itens);
        vendaRepository.deleteById(venda.getId());
    }

    @Override
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

    @Override
    public Venda addItem(ItemDto itemDto, VendedorDto vendedor) {
        long produtoId = itemDto.produtoId();
        float itemQtd = itemDto.quantidade();

        Optional<Venda> opVenda = getVendaAtiva();
        Venda venda = new Venda();

        if (opVenda.isEmpty()) {
            venda.setVendedorId(vendedor.id());
            venda.setVendedorNome(vendedor.nome());
            venda = save(venda);
        } else {
            venda = opVenda.get();
        }

        Produto produto = produtoService.getById(produtoId);

        if (produto.getEstoque() - itemQtd < 0)
            throw new ProdutoSemEstoqueException();

        Optional<Item> opItem = itemService.getItemByProdutoAndVendaId(produtoId, venda.getId());
        Item item;

        if (opItem.isPresent()) {
            item = opItem.get();
            item.sumQuantidade(itemQtd);
            item.calcularPrecoTotal();
        } else {
            item = new Item(itemQtd, produto, venda);
        }

        produto.subtractEstoque(itemQtd);
        produtoService.save(produto);

        itemService.save(item);

        venda.getItens().add(item);
        venda.calcularPrecoTotal();
        return save(venda);
    }

    @Override
    public Venda addItem(String codigoBarras, VendedorDto vendedor) {
        if (!codigoBarras.matches("^\\d{13}$"))
            throw new CodigoBarrasInvalidoException();

        return addItem(new ItemDto(1.0f, produtoService.getByCodigoBarras(codigoBarras).getId()), vendedor);
    }

    @Override
    public Venda addItem(List<ItemDto> itensDto, VendedorDto vendedor) {
        for (ItemDto itemDTO : itensDto) {
            addItem(itemDTO, vendedor);
        }

        return getVendaAtiva().get();
    }

    @Override
    @Transactional
    public Venda editItem(long itemId, float quantidade) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
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
        itemService.save(item);
        return save(venda);
    }

    @Override
    public Venda removeItem(long itemId) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
        Item item = itemService.getById(itemId);
        Produto produto = item.getProduto();

        venda.getItens().remove(item);
        produto.setEstoque(produto.getEstoque() + item.getQuantidade());

        produtoService.save(produto);
        itemService.delete(item);
        venda.calcularPrecoTotal();
        return save(venda);
    }

    @Override
    @CacheEvict(value = { "vendas", "vendas_between_datas", "produtos_ativos" }, allEntries = true)
    public void concluirVenda(VendaDto dto) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());

        if (dto.dataHoraConclusao().isBefore(venda.getDataHoraInicio()))
            throw new DateTimeException("A data de conclusão da venda não pode ser anterior a data de início.");
        
        venda.setDataHoraConclusao(dto.dataHoraConclusao());
        venda.setFormaPagamento(dto.formaPagamento());
        venda.setConcluida(true);
        save(venda);
    }

}
