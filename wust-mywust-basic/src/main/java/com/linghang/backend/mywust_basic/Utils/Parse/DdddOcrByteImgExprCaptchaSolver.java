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

public class DdddOcrByteImgExprCaptchaSolver implements CaptchaSolver<byte[]> {
    private Tesseract tesseract;
    /**
     * 构造方法，初始化 Tesseract OCR 引擎
     * @param tessDataPath tessdata 文件夹路径（包含 eng.traineddata）
     */
    /**
     * 默认构造方法，使用默认的 tessdata 路径
     */
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    // 注入tessdata在resources下的相对路径（默认tessdata）
    @Value("${parse.dependencypath:tessdata}")
    String path;

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
            // 4. 设置Tesseract의 data路径为临时目录
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
     * 核心方法：解决验证码（处理byte[]类型图片）
     * @param unsolvedImageCaptcha 未解决的图片验证码（byte[]类型图片数据）
     * @return 已解决的图片验证码（带上计算结果）
     * @throws ApiException 自定义异常，OCR 或计算失败时抛出
     */
    @Override
    public SolvedImageCaptcha<byte[]> solve(UnsolvedImageCaptcha<byte[]> unsolvedImageCaptcha) throws ApiException {
        try {
            // 创建结果对象，并设置原始图片
            SolvedImageCaptcha<byte[]> solvedImageCaptcha = new SolvedImageCaptcha<>(unsolvedImageCaptcha);
            // 执行 OCR 和表达式求值（直接传入byte[]图片数据）
            String result = this.ocrAndEvaluate(unsolvedImageCaptcha.getImage());
            // 设置计算结果
            solvedImageCaptcha.setResult(result);
            return solvedImageCaptcha;
        } catch (IOException | TesseractException e) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG); // 捕获异常，统一抛出为 API 异常
        }
    }

    /**
     * 识别图片中的验证码表达式，并计算其结果
     * @param imageBytes 图片字节数组（直接处理原始图片数据）
     * @return 表达式计算结果（字符串形式）
     */
    private String ocrAndEvaluate(byte[] imageBytes) throws IOException, TesseractException, ApiException {
        // 直接将字节数组转为 BufferedImage 对象（无需Base64解码）
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG, "无法解析图片数据");
        }

        // 使用 Tesseract 进行 OCR 识别，去除识别结果中的空白字符
        String rawExpression = tesseract.doOCR(image).replaceAll("\\s+", "");
        System.out.println("识别结果：" + rawExpression); // 调试输出

        // 计算表达式值
        return evaluateExpression(rawExpression);
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
    /**
     * 对识别到的表达式进行清洗并求值（逻辑保持不变）
     * @param expression OCR 识别出来的字符串
     * @return 表达式的求值结果
     */
    private String evaluateExpression(String expression) throws ApiException {
        try {
            // 清洗字符串：替换常见 OCR 错误字符
            expression = expression.replace('×', '*')
                    .replace('÷', '/')
                    .replace('＝', '=')
                    .replaceAll("[^0-9\\+\\-\\*/=]", "");

            // 去掉等号后面的内容（如 1+1=）
            if (expression.contains("=")) {
                expression = expression.substring(0, expression.indexOf("="));
            }

            // 匹配并提取表达式：格式必须为 “数字 运算符 数字”
            // 如果不匹配表达式格式，则检查是否为纯数字（部分系统如研究生系统使用纯数字验证码）
            if (!expression.matches("\\d+[\\+\\-\\*/]\\d+")) {
                if (expression.matches("\\d+")) {
                    return expression;
                }
                throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            // 解析运算符和操作数
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

            // 拆分左右数字
            String[] parts = expression.split("\\" + op);
            left = Integer.parseInt(parts[0]);
            right = Integer.parseInt(parts[1]);

            // 计算结果
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
}
