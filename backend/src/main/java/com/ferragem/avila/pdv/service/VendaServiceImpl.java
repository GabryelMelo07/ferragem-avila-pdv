package com.ferragem.avila.pdv.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Venda> getAll() {
        return vendaRepository.findAll();
    }

    @Override
    public Venda getById(long id) {
        return vendaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Venda não existe."));
    }

    @Override
    public List<Venda> getBetweenDataConclusao(LocalDate dataInicio, LocalDate dataFim) {
        return vendaRepository.findByDataHoraConclusaoBetween(dataInicio, dataFim);
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
    public void delete() {
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
