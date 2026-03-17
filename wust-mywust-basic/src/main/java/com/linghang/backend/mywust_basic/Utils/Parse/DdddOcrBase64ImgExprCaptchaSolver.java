package com.linghang.backend.mywust_basic.Utils.Parse;

import cn.wustlinghang.mywust.captcha.SolvedImageCaptcha;
import cn.wustlinghang.mywust.captcha.UnsolvedImageCaptcha;
import cn.wustlinghang.mywust.core.request.service.captcha.solver.CaptchaSolver;
import cn.wustlinghang.mywust.exception.ApiException;
import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class DdddOcrBase64ImgExprCaptchaSolver implements CaptchaSolver<String> {
    private Tesseract tesseract;

    // 注入tessdata在resources下的相对路径（默认tessdata）
    @Value("${parse.dependencypath:tessdata}")
    String path;

    // 用于扫描JAR内的资源（关键：解决JAR内目录遍历问题）
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /**
     * 初始化Tesseract：从JAR内读取tessdata资源，复制到临时目录
     */
    @PostConstruct
    public void initTesseract() {
        this.tesseract = new Tesseract();
        File tempTessDataDir = null;

        try {
            // 1. 创建Ubuntu临时目录（/tmp/tessdata，每次启动覆盖旧目录）
            tempTessDataDir = new File("/tmp/tessdata");
            // 先删除旧目录（避免残留文件干扰）
            deleteDir(tempTessDataDir);
            // 新建临时目录
            if (!tempTessDataDir.mkdirs()) {
                throw new IOException("创建临时目录失败：" + tempTessDataDir.getAbsolutePath());
            }

            // 2. 扫描JAR内的tessdata目录（匹配所有文件，如eng.traineddata）
            // 资源路径格式：classpath:/tessdata/*（*表示所有文件）
            String resourcePattern = "classpath:/" + path + "/*";
            Resource[] resources = resourceResolver.getResources(resourcePattern);

            if (resources.length == 0) {
                throw new FileNotFoundException("JAR内未找到tessdata资源，请确认tessdata放在src/main/resources下");
            }

            // 3. 遍历每个资源，用流复制到临时目录
            for (Resource resource : resources) {
                // 获取资源文件名（如eng.traineddata）
                String fileName = resource.getFilename();
                if (fileName == null) {
                    continue; // 跳过无文件名的资源
                }

                // 临时目录中的目标文件
                File targetFile = new File(tempTessDataDir, fileName);

                // 用流复制（关键：避免getFile()，直接读JAR内资源流）
                try (InputStream in = resource.getInputStream();
                     OutputStream out = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }

                System.out.println("已复制tessdata资源：" + fileName + " → " + targetFile.getAbsolutePath());
            }

            // 4. 设置Tesseract的data路径为临时目录
            this.tesseract.setDatapath(tempTessDataDir.getAbsolutePath());
            this.tesseract.setLanguage("eng"); // 英文语言包（需确保eng.traineddata存在）
            System.out.println("Tesseract初始化成功，data路径：" + tempTessDataDir.getAbsolutePath());
        } catch (Exception e) {
            // 初始化失败时，删除临时目录，避免残留
            if (tempTessDataDir != null) {
                deleteDir(tempTessDataDir);
            }
            throw new RuntimeException("Tesseract初始化失败", e); // 抛出运行时异常，中断Bean创建
        }
    }

    /**
     * 递归删除目录（用于清理临时目录）
     */
    private void deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDir(file); // 递归删除子文件/子目录
                }
            }
            dir.delete(); // 删除空目录
        } else if (dir.exists() && dir.isFile()) {
            dir.delete(); // 删除单个文件
        }
    }

    // 以下solve、ocrAndEvaluate、evaluateExpression方法不变（复用之前的逻辑）
    @Override
    public SolvedImageCaptcha<String> solve(UnsolvedImageCaptcha<String> unsolvedImageCaptcha) throws ApiException {
        try {
            SolvedImageCaptcha<String> solvedImageCaptcha = new SolvedImageCaptcha<>(unsolvedImageCaptcha);
            String result = this.ocrAndEvaluate(unsolvedImageCaptcha.getImage());
            solvedImageCaptcha.setResult(result);
            return solvedImageCaptcha;
        } catch (IOException | TesseractException e) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
        }
    }

    private String ocrAndEvaluate(String base64URIData) throws IOException, TesseractException, ApiException {
        String base64Image = base64URIData.replaceFirst("^data:image/[^;]+;base64,", "");
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

        String rawExpression = tesseract.doOCR(image).replaceAll("\\s+", "");
        System.out.println("OCR识别结果：" + rawExpression);
        return evaluateExpression(rawExpression);
    }

    private String evaluateExpression(String expression) throws ApiException {
        try {
            expression = expression.replace('×', '*')
                    .replace('÷', '/')
                    .replace('＝', '=')
                    .replaceAll("[^0-9\\+\\-\\*/=]", "");

            if (expression.contains("=")) {
                expression = expression.substring(0, expression.indexOf("="));
            }

            if (!expression.matches("\\d+[\\+\\-\\*/]\\d+")) {
                if (expression.matches("\\d+")) {
                    return expression;
                }
                throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            int left, right;
            char op;
            if (expression.contains("+")) {
                op = '+';
            } else if (expression.contains("-")) {
                op = '-';
            } else if (expression.contains("*")) {
                op = '*';
            } else if (expression.contains("/")) {
                op = '/';
            } else {
                throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            String[] parts = expression.split("\\" + op);
            left = Integer.parseInt(parts[0]);
            right = Integer.parseInt(parts[1]);

            int result;
            switch (op) {
                case '+': result = left + right; break;
                case '-': result = left - right; break;
                case '*': result = left * right; break;
                case '/':
                    if (right == 0) throw new ArithmeticException("除数为0");
                    result = left / right;
                    break;
                default: throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            return String.valueOf(result);
        } catch (Exception e) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
        }
    }

    // 无参构造（Spring创建Bean用）
    public DdddOcrBase64ImgExprCaptchaSolver() {}
}
