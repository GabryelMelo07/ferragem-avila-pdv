package com.ferragem.avila.pdv.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.dto.ItemDto;
import com.ferragem.avila.pdv.dto.VendaDto;
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.ProdutoSemEstoqueException;
import com.ferragem.avila.pdv.exceptions.VendaInativaException;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.repository.VendaRepository;
import com.ferragem.avila.pdv.service.interfaces.ItemService;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;
import com.ferragem.avila.pdv.service.interfaces.VendaService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VendaServiceImpl implements VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ProdutoService produtoService;

    @Cacheable(value = "vendas", key = "'page_' + #pageable.pageNumber")
    @Override
    public Page<Venda> getAll(Pageable pageable) {
        return vendaRepository.findAll(pageable);
    }

    @Cacheable(value = "venda_by_id", key = "#id")
    @Override
    public Venda getById(long id) {
        return vendaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Venda não existe."));
    }

    @Cacheable(value = "vendas_between_datas", key = "'page_' + #pageable.pageNumber")
    @Override
    public Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim) {
        return vendaRepository.findByDataHoraConclusaoBetween(pageable, dataInicio, dataFim);
    }

    @Override
    public List<Produto> getProdutosFromVendaAtiva() {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
        List<Produto> produtos = new ArrayList<>();

        for (Item item : venda.getItens()) {
            produtos.add(item.getProduto());
        }

        return produtos;
    }

    @Override
    public Venda save() {
        return vendaRepository.save(new Venda());
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
    public void delete() {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
        List<Item> itens = venda.getItens();

        for (Item item : itens) {
            item.getProduto().sumEstoque(item.getQuantidade());
        }
            
        itemService.deleteAll(itens);
        vendaRepository.deleteById(venda.getId());
    }

    @Override
    public Venda addItem(ItemDto itemDto) {
        long produtoId = itemDto.produtoId();
        float itemQtd = itemDto.quantidade();

        Optional<Venda> opVenda = getVendaAtiva();
        Venda venda;

        if (opVenda.isEmpty())
            venda = save();
        else
            venda = opVenda.get();

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
    public Venda addItem(String codigoBarras) {
        if (!codigoBarras.matches("^\\d{13}$"))
            throw new CodigoBarrasInvalidoException();

        return addItem(new ItemDto(1.0f, produtoService.getByCodigoBarras(codigoBarras).getId()));
    }

    @Override
    public Venda addItem(List<ItemDto> itensDto) {
        for (ItemDto itemDTO : itensDto) {
            addItem(itemDTO);
        }

        return getVendaAtiva().get();
    }

    @CacheEvict(value = "vendas", allEntries = true)
    @Override
    public void concluirVenda(VendaDto dto) {
        Venda venda = getVendaAtiva().orElseThrow(() -> new VendaInativaException());
        venda.setDataHoraConclusao(dto.dataHoraConclusao());
        venda.setFormaPagamento(dto.formaPagamento());
        venda.setConcluida(true);
        save(venda);
    }

}
