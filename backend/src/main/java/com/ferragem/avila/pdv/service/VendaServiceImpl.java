package com.ferragem.avila.pdv.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.repository.VendaRepository;
import com.ferragem.avila.pdv.service.interfaces.ItemService;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;
import com.ferragem.avila.pdv.service.interfaces.VendaService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class VendaServiceImpl implements VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ProdutoService produtoService;
    
    @Autowired
    private CacheService cacheService;
    
    @Override
    public Page<Venda> getAll(Pageable pageable) {
        return vendaRepository.findAll(pageable);
    }

    @Override
    public Venda getById(long id) {
        return vendaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Venda não existe."));
    }

    @Override
    public Page<Venda> getBetweenDataConclusao(Pageable pageable, LocalDate dataInicio, LocalDate dataFim) {
        return vendaRepository.findByDataHoraConclusaoBetween(pageable, dataInicio, dataFim);
    }

    @Override
    public List<Produto> getProdutosFromVendaAtiva() {
        if (!cacheService.existsByKey("venda_ativa"))
            throw new RuntimeException("Não existe venda ativa.");

        Venda venda = getVendaFromRedis();
        List<Produto> produtos = new ArrayList<>();

        for (Item item : venda.getItens()) {
            produtos.add(item.getProduto());
        }
        
        return produtos;
    }
    
    @Override
    public Venda save() {          
        if (cacheService.existsByKey("venda_ativa"))
            throw new RuntimeException("Já existe uma venda ativa.");
            
        Venda v = new Venda(vendaRepository.findLastId());
        cacheService.save("venda_ativa", v);
        return v;
    }

    @Override
    public boolean existsVendaAtiva() {
        if (!cacheService.existsByKey("venda_ativa"))
            return false;

        return true;
    }

    @Override
    public void delete() {
        if (!cacheService.existsByKey("venda_ativa"))
            throw new RuntimeException("Não existe venda ativa.");

        cacheService.delete("venda_ativa");
    }

    private Venda getVendaFromRedis() {
        if (!cacheService.existsByKey("venda_ativa"))
            throw new RuntimeException("Não existe venda ativa.");
        
        Venda venda = (Venda) cacheService.find("venda_ativa", Venda.class);
        return venda;
    }

    @Override
    public Venda addItem(ItemDTO itemDto) {
        Venda venda = getVendaFromRedis();
        Produto produto = produtoService.getById(itemDto.produtoId());
        List<Item> itens = venda.getItens();

        if (itens != null) {
            for (Item item : itens) {
                if (item.getProduto().getId() == produto.getId())
                    throw new RuntimeException("Produto já está incluso no item: " + item.getId() + ", altere o produto para aumentar a quantidade.");
            }
        }

        if (produto.getEstoque() - itemDto.quantidade() < 0)
            throw new RuntimeException("Estoque do produto insuficiente");

        Item item = new Item(itemDto.quantidade(), produto);
        venda.getItens().add(item);
        venda.calcularPrecoTotal();
        cacheService.save("venda_ativa", venda);

        return venda;
    }

    @Override
    public Venda addItem(String codigoBarras) {

        if (!codigoBarras.matches("[0-9]+"))
            throw new RuntimeException("Código de barras inválido");

        // try {
        //     Long.valueOf(codigoBarras);
        // } catch (Exception e) {
        //     throw new RuntimeException("Código de barras inválido." + e.getMessage());
        // }
        
        Venda venda = getVendaFromRedis();
        Produto produto = produtoService.getByCodigoBarras(codigoBarras);

        boolean hasProduto = false;
        float somaQuantidades = 0;

        for (Item item : venda.getItens()) {
            if (item.getProduto().getId() == produto.getId()) {
                hasProduto = true;
                somaQuantidades = item.getQuantidade() + 1;

                if (produto.getEstoque() - somaQuantidades > 0) {
                    item.setQuantidade(somaQuantidades);
                    item.calcularPrecoTotal(somaQuantidades);
                } else {
                    throw new RuntimeException("Estoque do produto insuficiente.");
                }

                break;
            }
        }

        if (!hasProduto) {
            if (produto.getEstoque() >= 1) {
                Item novoItem = new Item(1.0F, produto);
                venda.getItens().add(novoItem);
            } else {
                throw new RuntimeException("Estoque do produto insuficiente.");
            }
        }

        venda.calcularPrecoTotal();
        cacheService.save("venda_ativa", venda);
        return venda;
    }

    @Override
    public Venda addItem(List<ItemDTO> itensDto) {
        Venda venda = getVendaFromRedis();
        List<Item> itens = venda.getItens();

        for (ItemDTO itemDto : itensDto) {
            Produto produto = produtoService.getById(itemDto.produtoId());

            if (produto.isAtivo() == false)
                throw new RuntimeException("Produto inativo, para restaurar vá até a página de produtos.");
            
            Item item = new Item(itemDto.quantidade(), produto);
            
            if (itens.contains(item))
                throw new RuntimeException("Item já incluso na venda.");
            
            if (produto.getEstoque() - item.getQuantidade() < 0)
                throw new RuntimeException("Estoque insuficiente.");
            
            itens.add(item);
        }

        venda.getItens().addAll(itens);
        venda.calcularPrecoTotal();
        cacheService.save("venda_ativa", venda);
        return venda;
    }

    @Override
    @Transactional
    public Venda persist(VendaDTO dto) {
        Venda venda = getVendaFromRedis();        
        List<Item> itens = venda.getItens();

        venda.setConcluida(true);
        venda.setFormaPagamento(dto.formaPagamento());
        venda.setDataHoraConclusao(LocalDateTime.now());
        vendaRepository.save(venda);
        
        for (Item item : itens) {
            item.setVenda(venda);

            Produto p = item.getProduto();
            p.setEstoque(p.getEstoque() - item.getQuantidade());
            produtoService.save(p);
        }
        
        itemService.saveAll(itens);
        delete();
        return venda;
    }
    
}
