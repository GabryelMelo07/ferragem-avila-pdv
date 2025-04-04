package com.ferragem.avila.pdv.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

	private final S3Client s3Client;

	@Value("${aws.endpoint}")
	private String s3Url;

	@Value("${aws.images.bucket.name}")
	private String imagesBucket;

	@Value("${aws.reports.bucket.name}")
	private String reportsBucket;

	@Value("${contabo.access.hash}")
	private String contaboAccessHash;

	public String uploadImage(MultipartFile file) {
		String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();

		try {
			PutObjectRequest putOb = PutObjectRequest.builder()
					.bucket(imagesBucket)
					.key(filename)
					.acl(ObjectCannedACL.PUBLIC_READ)
					.build();

			s3Client.putObject(putOb, RequestBody.fromByteBuffer(ByteBuffer.wrap(file.getBytes())));
			String imageUrl = String.format("%s/%s:%s/%s", s3Url, contaboAccessHash, imagesBucket, filename);
			return imageUrl;
		} catch (Exception e) {
			log.error("Não foi possivel realizar o upload da imagem: {}", e.getMessage());
			return null;
		}
	}

	public void deleteImage(String url) {
		String key = url.substring(url.lastIndexOf("/") + 1);

		DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(imagesBucket)
				.key(key)
				.build();

		s3Client.deleteObject(request);
	}

	public String uploadReport(byte[] file, String name) {
		String reportName = String.format("%s_%s.xlsx", name.trim(), LocalDateTime.now().toString());

		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(reportsBucket)
				.key(reportName)
				.acl(ObjectCannedACL.PUBLIC_READ)
				.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.build();

		try {
			s3Client.putObject(request, RequestBody.fromBytes(file));
			return reportName;
		} catch (S3Exception ex) {
			String errorMessage = "Não foi possivel realizar o upload do arquivo";
			log.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		} catch (Exception ex) {
			log.error("Erro no uploadReport: ", ex);
			throw ex;
		}
	}

	public byte[] downloadReport(String reportFileName) {
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(reportsBucket)
				.key(reportFileName)
				.build();

		try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request)) {
			return inputStream.readAllBytes();
		} catch (IOException ex) {
			String errorMessage = "Não foi possivel realizar o download do arquivo";
			log.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		} catch (Exception ex) {
			log.error("Erro no downloadReport: ", ex);
			throw ex;
		}
	}

}
