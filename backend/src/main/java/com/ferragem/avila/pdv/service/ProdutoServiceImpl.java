package com.ferragem.avila.pdv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferragem.avila.pdv.dto.ProdutoDto;
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.ProdutoNaoEncontradoException;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.utils.CsvToProduto;
import com.ferragem.avila.pdv.model.utils.ProdutoComErro;
import com.ferragem.avila.pdv.model.utils.ProdutosFromCsv;
import com.ferragem.avila.pdv.repository.ProdutoRepository;
import com.ferragem.avila.pdv.service.interfaces.ProdutoService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${xlsx.file.limit}")
    private Integer xlsxFileLimit;
    
    // Tópicos do sistema de Pub/Sub do Redis, para enviar as mensagens.
    private final String RELATORIO_GERAL_PRODUTOS_CHANNEL = "pdv:relatorio-produtos";
    // private final String RESULTADO_UPLOAD_CSV_CHANNEL = "pdv:resultado-upload-csv";
    
    /**
     * Este método é responsável por carregar todos os registros da tabela produtos
     * do banco de dados, inseri-los no Redis e avisar o front-end do término do processo
     * via sistema de Pub/Sub do próprio Redis.
     * 
     * Ele roda de forma assíncrona em uma thread separada.
     * 
     * @throws JsonProcessingException
     */
    @Async
    @Override
    public void gerarRelatorioGeral(String relatorioKey) throws JsonProcessingException {
        List<Produto> produtos = produtoRepository.findByAtivoTrue();

        if (produtos.size() > xlsxFileLimit)
            throw new RuntimeException("Arquivos de formato .xlsx suportam somente " + xlsxFileLimit + " linhas."); //Personalizar esta exception.

        if (produtos != null && !produtos.isEmpty()) {
            String produtosToJson = objectMapper.writeValueAsString(produtos);
            redisTemplate.opsForValue().set(relatorioKey, produtosToJson);
            redisTemplate.convertAndSend(RELATORIO_GERAL_PRODUTOS_CHANNEL, "Relatório de produtos Carregado com sucesso!");
        }
    }
    
    @Cacheable(value = "produtos_ativos", key = "'pagina_' + #pageable.pageNumber")
    @Override
    public Page<Produto> getAll(Pageable pageable) {
        return produtoRepository.findByAtivoTrue(pageable);
    }
    
    @Cacheable(value = "produtos_inativos", key = "'pagina_' + #pageable.pageNumber")
    @Override
    public Page<Produto> getAllInativos(Pageable pageable) {
        return produtoRepository.findByAtivoFalse(pageable);
    }

    @Cacheable(value = "produto_by_descricao", key = "'pagina_' + #pageable.pageNumber")
    @Override
    public Page<Produto> getAllByDescricao(Pageable pageable, String descricao) {
        return produtoRepository.findByDescricaoContainingIgnoreCaseAndAtivoTrue(pageable, descricao);
    }
    
    @Override
    public Produto getById(long id) {
        return produtoRepository.findByIdAndAtivoTrue(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não existe."));
    }
    
    @Override
    public Produto getByCodigoBarras(String codigoBarras) {
        return produtoRepository.findByCodigoBarrasEAN13(codigoBarras);
    }

    @Override
    public List<Produto> getMaisVendidosMes(LocalDate data) {
        return produtoRepository.getMaisVendidosMes(data.getMonthValue(), data.getYear());
    }
    
    @Override
    public Page<Produto> getProdutosBaixoEstoque(Pageable pageable) {
        return produtoRepository.findProdutosComEstoqueBaixo(pageable);
    }
    
    @CacheEvict(value = "produtos_ativos", allEntries = true)
    @Override
    public Produto save(Produto produto) {
        return produtoRepository.save(produto);
    }
    
    @Override
    public Produto save(ProdutoDto dto) {
        if (!dto.codigoBarrasEAN13().matches("^\\d{13}$"))
            throw new CodigoBarrasInvalidoException();
        
        Produto p = new Produto(dto);
        return save(p);
    }

    @CacheEvict(value = "produtos_ativos", allEntries = true)
    @Override
    public Produto update(long id, ProdutoDto dto) {
        if (!dto.codigoBarrasEAN13().matches("^\\d{13}$"))
            throw new CodigoBarrasInvalidoException();
        
        Produto p = getById(id);
        p.setDescricao(dto.descricao());
        p.setUnidadeMedida(dto.unidadeMedida());
        p.setEstoque(dto.estoque());
        p.setPrecoFornecedor(dto.precoFornecedor());;
        p.setPreco(dto.preco());
        p.setCodigoBarrasEAN13(dto.codigoBarrasEAN13());
        return save(p);
    }

    @CacheEvict(value = {"produtos_ativos", "produtos_inativos"}, allEntries = true)
    @Override
    public void delete(long id) {
        Produto p = getById(id);
        p.setAtivo(false);
        save(p);
    }

    private List<CsvToProduto> parseCsvFile(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<CsvToProduto> csvToBean = new CsvToBeanBuilder<CsvToProduto>(reader)
                    .withType(CsvToProduto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
                    
            return csvToBean.parse();
        }
    }

    @CacheEvict(value = "produtos_ativos", allEntries = true)
    @Override
    public ProdutosFromCsv saveProductsFromCsv(MultipartFile file) throws IOException {
        List<CsvToProduto> csvToProduto = parseCsvFile(file);
        ProdutosFromCsv produtosFromCsv = new ProdutosFromCsv();

        for (CsvToProduto produto : csvToProduto) {
            try {
                Produto p = new Produto(
                    produto.getDescricao(),
                    produto.getUnidadeMedida(),
                    produto.getEstoque(),
                    produto.getPrecoFornecedor(),
                    produto.getPreco(),
                    produto.getCodigoBarrasEAN13()
                );

                save(p);
                produtosFromCsv.somar();
            } catch (Exception e) {
                produtosFromCsv.getProdutosComErro().add(new ProdutoComErro(produto, e.getMessage()));
            }
        }

        return produtosFromCsv;
    }
    
}
