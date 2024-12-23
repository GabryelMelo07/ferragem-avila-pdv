package com.ferragem.avila.pdv.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;

@Service
public class FileStorageService {

    private final S3Template s3Template;

    @Value("${aws.images.bucket.name}")
    private String imagesBucket;

    @Value("${aws.reports.bucket.name}")
    private String reportsBucket;

    public FileStorageService(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    public String uploadImage(MultipartFile file) {
        try (var img = file.getInputStream()) {
            String imageName = String.format("%s_%s", UUID.randomUUID().toString(), file.getOriginalFilename().trim());
            S3Resource uploadedImg = s3Template.upload(imagesBucket, imageName, img);
            return uploadedImg.getURL().toString();
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possivel realizar o upload do documento", ex);
        }
    }

    public void deleteImage(String url) {
        String bucketName = "ferragem-avila-pdv-images";
        String key = url.substring(url.lastIndexOf("/") + 1);
        s3Template.deleteObject(bucketName, key);
    }

    public String uploadReport(byte[] file, String name) {
        try (var report = new ByteArrayInputStream(file)) {            
            String reportName = String.format("%s_%s.xlsx", name.trim(), LocalDateTime.now().toString());
            s3Template.upload(reportsBucket, reportName, report);
            return reportName;
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possivel realizar o upload do documento", ex);
        }
    }

    public byte[] downloadReport(String reportFileName) {
        try {
            S3Resource downloadedReport = s3Template.download(reportsBucket, reportFileName);
            return downloadedReport.getContentAsByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao baixar o relatório do S3", ex);
        }
    }

}
