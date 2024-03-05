package com.ferragem.avila.pdv.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.dto.ItemDTO;
import com.ferragem.avila.pdv.dto.VendaDTO;
import com.ferragem.avila.pdv.model.Item;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.Venda;
import com.ferragem.avila.pdv.model.enums.UnidadeMedida;
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
        return vendaRepository.findByConcluidaTrue();
    }

    @Override
    public Venda getById(long id) {
        return vendaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Venda não existe."));
    }

    private Venda save(Venda venda) {
        return vendaRepository.save(venda);
    }
    
    @Override
    public Venda saveInRedis() {          
        if (cacheService.existsByKey("venda_ativa"))
            throw new RuntimeException("Já existe uma venda ativa.");
            
        Venda v = new Venda();
        Long idUltimaVenda = vendaRepository.findLastId();
        v.setId(idUltimaVenda != null ? idUltimaVenda + 1 : 1);
        v.setDataHoraInicio(LocalDateTime.now());

        cacheService.save("venda_ativa", v);
        return v;
    }

    @Override
    public void deleteFromRedis() {
        cacheService.delete("venda_ativa");
    }

    private Venda getVendaFromRedis() {
        if (!cacheService.existsByKey("venda_ativa"))
            throw new RuntimeException("Não existe venda ativa.");
        
        Venda venda = (Venda) cacheService.find("venda_ativa", Venda.class);
        return venda;
    }

    @Override
    public Venda addItemToVenda(ItemDTO itemDto) {
        Venda venda = getVendaFromRedis();
        Produto produto = produtoService.getById(itemDto.produtoId());
        Float estoqueAntigo = produto.getEstoque();
        Float novoEstoque = produto.getEstoque() - itemDto.quantidade();

        if (novoEstoque < 0)
            throw new IllegalStateException("O produto não tem estoque suficiente.");

        if (venda.getItens() == null)
            venda.setItens(new ArrayList<Item>());

        Item item = new Item();
        item.setQuantidade(itemDto.quantidade());

        if (produto.getUnidadeMedida() == UnidadeMedida.GRAMA) {
            BigDecimal precoPorKg = produto.getPreco().divide(new BigDecimal(1000));
            item.setPreco(precoPorKg.multiply(new BigDecimal(itemDto.quantidade().floatValue())));
        } else if (produto.getUnidadeMedida() == UnidadeMedida.METRO) {
            BigDecimal precoPorMetro = produto.getPreco().divide(new BigDecimal(100));
            item.setPreco(precoPorMetro.multiply(new BigDecimal(itemDto.quantidade().floatValue())));
        } else {
            item.setPreco(produto.getPreco().multiply(new BigDecimal(itemDto.quantidade().intValue())));
        }

        item.setProduto(produto);
        item.setVenda(venda);

        try {
            venda.getItens().add(item);
            cacheService.save("venda_ativa", venda);
            
            produto.setEstoque(novoEstoque);
            produtoService.save(produto);
        } catch (Exception e) {
            venda.getItens().remove(item);
            cacheService.save("venda_ativa", venda);
            produto.setEstoque(estoqueAntigo);
            produtoService.save(produto);
        }

        return venda;
    }

    @Override
    @Transactional
    public Venda persistVenda(VendaDTO dto) {
        Venda venda = getVendaFromRedis();
        List<Item> itens = venda.getItens();

        venda.setItens(new ArrayList<>());
        venda.setConcluida(true);
        venda.setFormaPagamento(dto.formaPagamento());
        venda.setDataHoraConclusao(LocalDateTime.now());
        vendaRepository.save(venda);
        
        itemService.saveAll(itens);
        venda.setItens(itens);
        deleteFromRedis();

        return save(venda);
    }
    
}
