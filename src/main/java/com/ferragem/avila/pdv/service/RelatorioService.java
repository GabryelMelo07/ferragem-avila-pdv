package com.ferragem.avila.pdv.service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RelatorioService {

    public <T> byte[] gerarRelatorioProdutos(List<String> cabecalho, String nomePagina, List<T> data) {
        log.info("Gerando relatório geral de produtos.");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Produtos");
            Row header = sheet.createRow(0);

            for (int i = 0; i < cabecalho.size(); i++) {
                header.createCell(i).setCellValue(cabecalho.get(i));
            }

            int rowIndex = 1;

            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);
                Field[] fields = item.getClass().getDeclaredFields();

                for (int i = 0; i < fields.length; i++) {
                    if (i < cabecalho.size()) {
                        Field field = fields[i];
                        field.setAccessible(true);
                        Object value = field.get(item);

                        if (value != null) {
                            row.createCell(i).setCellValue(value.toString());
                        } else {
                            row.createCell(i).setCellValue("");
                        }
                    }
                }
            }

            for (int i = 0; i < cabecalho.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
