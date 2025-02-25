package com.ferragem.avila.pdv.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ferragem.avila.pdv.dto.produto.ProdutoDto;
import com.ferragem.avila.pdv.dto.produto.UpdateProdutoDto;
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.ProdutoNaoEncontradoException;
import com.ferragem.avila.pdv.exceptions.XlsxSizeLimitException;
import com.ferragem.avila.pdv.model.Produto;
import com.ferragem.avila.pdv.model.enums.UnidadeMedida;
import com.ferragem.avila.pdv.repository.ProdutoRepository;
import com.ferragem.avila.pdv.service.apis.NfeApiService;
import com.ferragem.avila.pdv.utils.product_conversion.RedisProductUtils;
import com.ferragem.avila.pdv.utils.product_conversion.csv.CsvToProduto;
import com.ferragem.avila.pdv.utils.product_conversion.csv.ProdutoComErro;
import com.ferragem.avila.pdv.utils.product_conversion.csv.ProdutosImportados;
import com.ferragem.avila.pdv.utils.product_conversion.xml.Det;
import com.ferragem.avila.pdv.utils.product_conversion.xml.ProductFromXML;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProdutoService {
	private final ProdutoRepository produtoRepository;
	private final FileStorageService fileStorageService;
	private final RelatorioService relatorioService;
	private final RedisProductUtils redisProductUtils;
	private final NfeApiService nfeApiService;
	private final Integer xlsxFileLimit;
	private final String importProductsByCsvRedisKey;
	private final String importProductsByXmlRedisKey;

	public ProdutoService(ProdutoRepository produtoRepository, FileStorageService fileStorageService,
			RelatorioService relatorioService, RedisProductUtils redisProductUtils, NfeApiService nfeApiService,
			@Value("${xlsx.file.limit}") Integer xlsxFileLimit,
			@Value("${import-csv.redis.key}") String importProductsByCsvRedisKey,
			@Value("${import-xml.redis.key}") String importProductsByXmlRedisKey) {
		this.produtoRepository = produtoRepository;
		this.fileStorageService = fileStorageService;
		this.relatorioService = relatorioService;
		this.redisProductUtils = redisProductUtils;
		this.nfeApiService = nfeApiService;
		this.xlsxFileLimit = xlsxFileLimit;
		this.importProductsByCsvRedisKey = importProductsByCsvRedisKey;
		this.importProductsByXmlRedisKey = importProductsByXmlRedisKey;
	}

	/**
	 * Este método é responsável por carregar todos os registros da tabela produtos
	 * do banco de dados, montar o relatório .xlsx e avisar o front-end do término
	 * do
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
			List<String> cabecalho = List.of("id", "descricao", "unidadeMedida", "estoque", "precoFornecedor", "preco",
					"codigoBarrasEAN13", "ativo", "imagem");
			byte[] relatorio = relatorioService.gerarRelatorioProdutos(cabecalho, "Produtos", produtos);
			String nomeRelatorio = fileStorageService.uploadReport(relatorio, "relatorio_produtos");

			redisProductUtils.storeValueAndSendMessage(relatorioKey, nomeRelatorio, 3, TimeUnit.HOURS,
					"Relatório de produtos gerado com sucesso!");
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

	public void restoreStock(Produto produto, float estoque) {
		produto.sumEstoque(estoque);
		produtoRepository.save(produto);
	}

	@Async
	@CacheEvict(value = "produtos_ativos", allEntries = true)
	public void importarProdutosViaCsv(String filePath) throws IOException {
		List<CsvToProduto> produtosCsv = parseCsvFile(filePath);
		List<Produto> produtos = new ArrayList<>();

		for (CsvToProduto pCsv : produtosCsv) {
			Produto p = new Produto(
					pCsv.getDescricao(),
					pCsv.getUnidadeMedida(),
					pCsv.getEstoque(),
					pCsv.getPrecoFornecedor(),
					pCsv.getPreco(),
					pCsv.getCodigoBarrasEAN13());
			produtos.add(p);
		}

		importProducts(produtos, "CSV");
		Files.deleteIfExists(Path.of(filePath));
	}

	@Async
	@CacheEvict(value = "produtos_ativos", allEntries = true)
	public void importarProdutosViaXml(String chaveAcessoNfe, double porcentagemAumentoPreco) {
		List<Det> produtosXml = nfeApiService.getNfeXml(chaveAcessoNfe);
		List<Produto> produtos = new ArrayList<>();

		Map<String, UnidadeMedida> unidadeMap = Map.of(
				"UN", UnidadeMedida.UNIDADE,
				"KG", UnidadeMedida.GRAMA,
				"MT", UnidadeMedida.METRO);

		for (Det det : produtosXml) {
			ProductFromXML productFromXML = det.getProd();
			BigDecimal precoFornecedor = productFromXML.getVUnCom();

			Produto p = new Produto();
			p.setDescricao(productFromXML.getXProd());
			p.setUnidadeMedida(unidadeMap.getOrDefault(productFromXML.getUCom(), UnidadeMedida.UNIDADE));
			p.setEstoque(productFromXML.getQCom());
			p.setPrecoFornecedor(precoFornecedor);
			p.setCodigoBarrasEAN13(productFromXML.getCEAN());

			BigDecimal aumento = BigDecimal.valueOf(1 + (porcentagemAumentoPreco / 100));
			BigDecimal preco = precoFornecedor.multiply(aumento);
			p.setPreco(preco);

			produtos.add(p);
		}

		importProducts(produtos, "XML");
	}

	private List<CsvToProduto> parseCsvFile(String filePath) throws IOException {
		try (Reader reader = new BufferedReader(new FileReader(filePath))) {
			CsvToBean<CsvToProduto> csvToBean = new CsvToBeanBuilder<CsvToProduto>(reader)
					.withType(CsvToProduto.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();

			return csvToBean.parse();
		}
	}

	private boolean ifProductAlreadyExistsUpdateIt(Produto p) {
		String codigoBarrasEan13 = p.getCodigoBarrasEAN13();

		Optional<Produto> productByCodBarras = Optional.empty();
		Optional<Produto> productByDescricao = produtoRepository.findByDescricao(p.getDescricao());

		if (codigoBarrasEan13 != null) {
			productByCodBarras = produtoRepository.findByCodigoBarrasEAN13(codigoBarrasEan13);
		}

		Produto product = new Produto();

		if (productByCodBarras.isPresent()) {
			product = productByCodBarras.get();
		} else if (productByDescricao.isPresent()) {
			product = productByDescricao.get();
		} else {
			return false;
		}

		product.sumEstoque(p.getEstoque());
		product.setPreco(p.getPreco());
		product.setPrecoFornecedor(p.getPrecoFornecedor());
		produtoRepository.save(product);

		return true;
	}

	private String extrairMensagemErro(DataIntegrityViolationException exception) {
		String message = exception.getRootCause().getMessage();

		if (message.contains("codigo_barrasean13")) {
			return "Já existe um produto cadastrado com este Código de Barras.";
		} else if (message.contains("descricao")) {
			return "Já existe um produto cadastrado com esta Descrição.";
		}

		return "Erro de integridade ao salvar produto.";
	}

	private void importProducts(List<Produto> products, String origin) {
		ProdutosImportados result = new ProdutosImportados();
		List<Produto> newProducts = new ArrayList<>();

		for (Produto p : products) {
			if (ifProductAlreadyExistsUpdateIt(p)) {
				result.somar();
				continue;
			}
			newProducts.add(p);
		}

		for (Produto product : newProducts) {
			try {
				produtoRepository.save(product);
				result.somar();
			} catch (DataIntegrityViolationException e) {
				String errorMessage = extrairMensagemErro(e);
				result.getProdutosComErro()
						.add(new ProdutoComErro(product.getDescricao(), product.getCodigoBarrasEAN13(), errorMessage));
			}
		}

		int savedProducts = result.getProdutosSalvos();

		redisProductUtils.sendMessageOrStoreError(
				origin.equals("CSV") ? importProductsByCsvRedisKey : importProductsByXmlRedisKey,
				String.format("Todos os %d produtos foram importados com sucesso!", savedProducts),
				String.format("""
						O processo de importação dos produtos foi concluído.
						Produtos salvos: %d
						Produtos com erro: %d
						""", savedProducts, result.getProdutosComErro().size()),
				result);
	}

}
