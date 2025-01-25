package com.ferragem.avila.pdv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferragem.avila.pdv.dto.ProdutoDto;
import com.ferragem.avila.pdv.dto.UpdateProdutoDto;
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.ProdutoNaoEncontradoException;
import com.ferragem.avila.pdv.exceptions.XlsxSizeLimitException;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.repository.ProdutoRepository;
import com.ferragem.avila.pdv.utils.CsvToProduto;
import com.ferragem.avila.pdv.utils.ProdutoComErro;
import com.ferragem.avila.pdv.utils.ProdutosFromCsv;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final FileStorageService fileStorageService;
    private final RelatorioService relatorioService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${xlsx.file.limit}")
    private Integer xlsxFileLimit;

    @Value("${import-csv.redis.key}")
    private String importCsvRedisKey;

    // Tópicos do sistema de Pub/Sub do Redis, para publicar mensagens que serão
    // consumidas no frontend.
    private final String RELATORIO_GERAL_PRODUTOS_CHANNEL = "pdv:relatorio-produtos";
    private final String RESULTADO_UPLOAD_CSV_CHANNEL = "pdv:resultado-upload-csv";

    public ProdutoService(ProdutoRepository produtoRepository, FileStorageService fileStorageService, RelatorioService relatorioService, RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.produtoRepository = produtoRepository;
        this.fileStorageService = fileStorageService;
        this.relatorioService = relatorioService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Este método é responsável por carregar todos os registros da tabela produtos
     * do banco de dados, montar o relatório .xlsx e avisar o front-end do término do
     * processo via sistema de Pub/Sub do próprio Redis.
     * 
     * Ele roda de forma assíncrona em uma thread separada.
     * 
     * @throws JsonProcessingException
     */
    @Async
    public void gerarRelatorioProdutosGeral(String relatorioKey) {
        List<Produto> produtos = produtoRepository.findAllAtivosOrderedById();

        if (produtos.size() > xlsxFileLimit) {
            throw new XlsxSizeLimitException(xlsxFileLimit);
        }

        if (produtos != null && !produtos.isEmpty()) {
            List<String> cabecalho = List.of("id", "descricao", "unidadeMedida", "estoque", "precoFornecedor", "preco", "codigoBarrasEAN13", "ativo", "imagem");
            byte[] relatorio = relatorioService.gerarRelatorioProdutos(cabecalho, "Produtos", produtos);
            String nomeRelatorio = fileStorageService.uploadReport(relatorio, "relatorio_produtos");

            redisTemplate.opsForValue().set(relatorioKey, nomeRelatorio, 3, TimeUnit.HOURS);
            redisTemplate.convertAndSend(RELATORIO_GERAL_PRODUTOS_CHANNEL, "Relatório de produtos gerado com sucesso!");
        }
    }

    public byte[] getRelatorioGerado(String nomeRelatorio) {
        return fileStorageService.downloadReport(nomeRelatorio);
    }

    @Cacheable(value = "produtos_ativos", key = "'pagina_' + #pageable.pageNumber + '_' + #pageable.sort.toString()", unless = "#result == null or #result.isEmpty()")
    public Page<Produto> getAll(Pageable pageable) {
        return produtoRepository.findByAtivoTrue(pageable);
    }

    @Cacheable(value = "produtos_inativos", key = "'pagina_' + #pageable.pageNumber", unless = "#result == null or #result.isEmpty()")
    public Page<Produto> getAllInativos(Pageable pageable) {
        return produtoRepository.findByAtivoFalse(pageable);
    }

    public Page<Produto> findByParams(Pageable pageable, String parametro) {
        return produtoRepository.findByParametros(pageable, parametro);
    }

    @Transactional
    public Produto getById(long id) {
        return produtoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não existe."));
    }

    public Long getIdByCodigoBarras(String codigoBarras) {
        Long produtoId = produtoRepository.getIdByCodigoBarras(codigoBarras);

        if (produtoId == null) {
            throw new ProdutoNaoEncontradoException();
        }
        
        return produtoId;
    }

    public List<Produto> getMaisVendidosMes(LocalDate data) {
        return produtoRepository.getMaisVendidosMes(data.getMonthValue(), data.getYear());
    }

    public Page<Produto> getProdutosBaixoEstoque(Pageable pageable) {
        return produtoRepository.findProdutosComEstoqueBaixo(pageable);
    }

    public Produto save(Produto produto) {
        return produtoRepository.save(produto);
    }
    
    private Produto save(Produto produto, MultipartFile imagem) {
        String imagemUrl = produto.getImagem();
        
        if (imagemUrl == null) {
            imagemUrl = fileStorageService.uploadImage(imagem);
            produto.setImagem(imagemUrl);
            return save(produto);
        }

        fileStorageService.deleteImage(imagemUrl);
        imagemUrl = fileStorageService.uploadImage(imagem);
        produto.setImagem(imagemUrl);

        return save(produto);
    }

    @CacheEvict(value = "produtos_ativos", allEntries = true)
    public Produto save(ProdutoDto dto) {
        if (!dto.codigoBarrasEAN13().matches("^\\d{13}$")) {
            throw new CodigoBarrasInvalidoException();
        }

        if (dto.imagem() != null) {
            return save(new Produto(dto), dto.imagem());
        }
            
        Produto p = new Produto(dto);
        return save(p);
    }

    @CacheEvict(value = "produtos_ativos", allEntries = true)
    public Produto update(long id, UpdateProdutoDto dto) {
        if (!dto.codigoBarrasEAN13().matches("^\\d{13}$")) {
            throw new CodigoBarrasInvalidoException();
        }

        Produto p = getById(id);
        p.setDescricao(dto.descricao());
        p.setUnidadeMedida(dto.unidadeMedida());
        p.setPrecoFornecedor(dto.precoFornecedor());
        p.setPreco(dto.preco());
        p.setCodigoBarrasEAN13(dto.codigoBarrasEAN13());

        if (dto.imagem() != null) {
            return save(p, dto.imagem());
        }
        
        return save(p);
    }

    @CacheEvict(value = "produtos_ativos", allEntries = true)
    public Produto updateEstoque(long id, Float novoEstoque) {
        Produto p = getById(id);
        p.setEstoque(novoEstoque);
        return save(p);
    }

    @CacheEvict(value = { "produtos_ativos", "produtos_inativos" }, allEntries = true)
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

    private boolean ifProductAlreadyExistsUpdateIt(Produto p) {
        Optional<Produto> productByCodBarras = produtoRepository.findByCodigoBarrasEAN13(p.getCodigoBarrasEAN13());
        Optional<Produto> productByDescricao = produtoRepository.findByDescricao(p.getDescricao());

        Produto produto = new Produto();

        if (productByCodBarras.isPresent()) {
            produto = productByCodBarras.get();
        } else if (productByDescricao.isPresent()) {
            produto = productByDescricao.get();
        } else {
            return false;
        }

        produto.sumEstoque(p.getEstoque());
        produtoRepository.save(produto);

        return true;
    }

    @Async
    @CacheEvict(value = "produtos_ativos", allEntries = true)
    public void importarProdutosCsv(MultipartFile file) throws IOException {
        List<CsvToProduto> produtosCsv = parseCsvFile(file);
        List<Produto> produtos = new ArrayList<>();
        ProdutosFromCsv resultado = new ProdutosFromCsv();

        for (CsvToProduto pCsv : produtosCsv) {
            Produto p = new Produto(
                    pCsv.getDescricao(),
                    pCsv.getUnidadeMedida(),
                    pCsv.getEstoque(),
                    pCsv.getPrecoFornecedor(),
                    pCsv.getPreco(),
                    pCsv.getCodigoBarrasEAN13());

            if (ifProductAlreadyExistsUpdateIt(p)) {
                resultado.somar();
                continue;
            }

            produtos.add(p);
        }

        for (Produto produto : produtos) {
            try {
                produtoRepository.save(produto);
                resultado.somar();
            } catch (Exception e) {
                resultado.getProdutosComErro().add(new ProdutoComErro(produto.getDescricao(), produto.getCodigoBarrasEAN13(), e.getMessage()));
            }
        }

        if (resultado.getProdutosComErro().isEmpty()) {
            redisTemplate.convertAndSend(RESULTADO_UPLOAD_CSV_CHANNEL,
                    String.format("Todos os %d produtos foram importados com sucesso!", resultado.getProdutosSalvos()));
        } else {
            String resultadoJson = objectMapper.writeValueAsString(resultado);
            redisTemplate.opsForValue().set(importCsvRedisKey, resultadoJson, 3, TimeUnit.HOURS);

            redisTemplate.convertAndSend(
                    RESULTADO_UPLOAD_CSV_CHANNEL,
                    String.format("""
                            O processo de importação dos produtos foi concluído.
                            Produtos salvos: %d
                            Produtos com erro: %d
                            """, resultado.getProdutosSalvos(), resultado.getProdutosComErro().size()));
        }
    }
        
}
