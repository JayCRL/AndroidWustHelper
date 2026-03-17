package com.example.wustchat.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileExtractionUtils {

    public static String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        byte[] bytes = file.getBytes();

        if (filename.endsWith(".txt") || filename.endsWith(".md") || filename.endsWith(".csv")) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (filename.endsWith(".pdf")) {
            return extractTextFromPdf(bytes);
        }
        if (filename.endsWith(".xlsx")) {
            return extractTextFromXlsx(bytes);
        }
        if (filename.endsWith(".docx")) {
            return extractTextFromDocx(bytes);
        }
        if (filename.endsWith(".doc")) {
            return extractTextFromDoc(bytes);
        }

        throw new IllegalArgumentException("不支持的文件类型: " + filename);
    }

    private static String extractTextFromPdf(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String extractTextFromXlsx(byte[] bytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            for (Sheet sheet : workbook) {
                sb.append("\n### Sheet: ").append(sheet.getSheetName()).append("\n");
                for (Row row : sheet) {
                    List<String> values = new ArrayList<>();
                    for (Cell cell : row) {
                        values.add(cell.toString());
                    }
                    sb.append(String.join(" | ", values)).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private static String extractTextFromDocx(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private static String extractTextFromDoc(byte[] bytes) throws IOException {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(bytes));
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}
