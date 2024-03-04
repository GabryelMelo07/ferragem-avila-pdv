package com.ferragem.avila.pdv.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
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
        if (redisTemplate.opsForValue().get("venda_ativa") != null)
            throw new RuntimeException("Já existe uma venda ativa.");
            
        try {
            Venda v = new Venda();
            Long idUltimaVenda = vendaRepository.findLastId();
            v.setId(idUltimaVenda != null ? idUltimaVenda + 1 : 1);
            v.setDataHoraInicio(LocalDateTime.now());
            String vendaToJson = objectMapper.writeValueAsString(v);
            redisTemplate.opsForValue().set("venda_ativa", vendaToJson);
            return v;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteFromRedis() {
        redisTemplate.delete("venda_ativa");
    }

    private Venda getVendaFromRedis() {
        String vendaAtiva = (String) redisTemplate.opsForValue().get("venda_ativa");

        if (vendaAtiva == null) {
            throw new RuntimeException("Não existe venda aberta.");
        }

        try {
            Venda venda = objectMapper.readValue(vendaAtiva, Venda.class);
            return venda;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao desserializar o objeto de Venda do Redis.");
        }
    }

    private void saveVendaInRedis(Venda venda) {
        try {
            String vendaJson = objectMapper.writeValueAsString(venda);
            redisTemplate.opsForValue().set("venda_ativa", vendaJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
        item.setPreco(produto.getPreco().multiply(new BigDecimal(itemDto.quantidade().floatValue())));
        item.setProduto(produto);
        item.setVenda(venda);

        try {
            venda.getItens().add(item);
            saveVendaInRedis(venda);
            
            produto.setEstoque(novoEstoque);
            produtoService.save(produto); // Bug: Código de barras do produto está ficando nulo.
        } catch (Exception e) {
            venda.getItens().remove(item);
            saveVendaInRedis(venda);
            produto.setEstoque(estoqueAntigo);
            produtoService.save(produto);
        }

        return venda;
    }

    @Override
    @Transactional
    public Venda persistVenda(VendaDTO dto) {
        String vendaAtiva = (String) redisTemplate.opsForValue().get("venda_ativa");

        if (vendaAtiva == null)
            throw new RuntimeException("Não existe venda ativa.");

        Venda venda = null;
        List<Item> itens = null;
            
        try {
            venda = objectMapper.readValue(vendaAtiva, Venda.class);
            itens = venda.getItens();

            venda.setId(null);
            venda.setItens(new ArrayList<>());
            venda.setConcluida(true);
            venda.setDataHoraConclusao(LocalDateTime.now());
            venda.setFormaPagamento(dto.formaPagamento());

            venda = vendaRepository.save(venda);

            for (Item item : itens) {
                item.setVenda(venda);
            }

            itemService.saveAll(itens);
            venda.setItens(itens);
            deleteFromRedis();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        return save(venda);
    }
    
}
