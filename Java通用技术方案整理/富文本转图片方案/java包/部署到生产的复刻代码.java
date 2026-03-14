import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * HTML 转 PDF 再转图片并裁剪的工具类
 * 依赖：flying-saucer, itext 2.1.7, pdfbox 2.0.27, jsoup
 */
public class HtmlToImageConverter {

    // ================= 配置区域 =================
    // 请修改为实际 Linux 服务器上的中文字体绝对路径
    // 建议至少注册一个宋体和一个黑体，防止缺字
    private static final String[] FONT_PATHS = {
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc", // 文泉驿正黑 (常见于 CentOS/Ubuntu)
            "/usr/share/fonts/chinese/simsun.ttc",          // 宋体 (常见路径)
            "/root/fonts/simhei.ttf"                        // 自定义路径示例
    };
    // ===========================================

    /**
     * 主入口方法
     *
     * @param htmlFilePath      输入 HTML 文件路径
     * @param outputFolderPath  输出图片的文件夹路径
     * @param dpi               图片清晰度 (推荐 144 或 300)
     */
    public static void convert(String htmlFilePath, String outputFolderPath, int dpi) {
        // 在新线程中执行，避免阻塞主业务（调用方控制线程，这里也可以内部封装，视需求而定）
        // 既然你说调用方会起新线程，这里直接同步执行逻辑即可
        try {
            System.out.println("[任务开始] 处理文件: " + htmlFilePath);

            // 1. 读取并格式化 HTML
            String formattedHtml = formatHtmlContent(htmlFilePath);
            System.out.println("[步骤 1] HTML 格式化完成");

            // 2. 生成临时 PDF 文件
            File tempPdfFile = File.createTempFile("render_", ".pdf");
            tempPdfFile.deleteOnExit(); // 程序结束后删除临时 PDF
            generatePdf(formattedHtml, tempPdfFile.getAbsolutePath());
            System.out.println("[步骤 2] PDF 生成完成: " + tempPdfFile.getName());

            // 3. PDF 转图片并裁剪
            convertPdfToCroppedImages(tempPdfFile.getAbsolutePath(), outputFolderPath, dpi);
            System.out.println("[步骤 3] 图片生成并裁剪完成，输出至: " + outputFolderPath);

        } catch (Exception e) {
            System.err.println("[任务失败] 发生异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("HTML 转图片处理失败", e);
        }
    }

    /**
     * 子方法：将 HTML 文件内容格式标准化
     * 使用 Jsoup 清理标签，补全 head/body，处理实体字符
     */
    private static String formatHtmlContent(String filePath) throws IOException {
        String rawContent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        
        // 使用 Jsoup 解析并整理 HTML
        Document doc = Jsoup.parse(rawContent);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml) // Flying Saucer 需要 XHTML 格式
                .escapeMode(Entities.EscapeMode.xhtml);     // 转义特殊字符

        // 确保有正确的编码声明，防止中文乱码
        if (doc.head().getElementsByTag("meta").isEmpty()) {
            doc.head().appendElement("meta")
                    .attr("http-equiv", "Content-Type")
                    .attr("content", "text/html; charset=UTF-8");
        }

        return doc.outerHtml();
    }

    /**
     * 核心方法：使用 ITextRenderer 和 FontResolver 生成 PDF
     */
    private static void generatePdf(String htmlContent, String pdfPath) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        ITextFontResolver fontResolver = renderer.getFontResolver();

        // 1. 注册 Linux 系统字体 (解决中文方块问题)
        for (String fontPath : FONT_PATHS) {
            File fontFile = new File(fontPath);
            if (fontFile.exists()) {
                // addFont 会读取字体文件并注册到渲染器
                fontResolver.addFont(fontPath, ITextFontResolver.IDENTITY_H, true);
                System.out.println("[字体加载] 成功加载: " + fontPath);
            } else {
                System.out.println("[字体警告] 未找到字体文件: " + fontPath + "，中文可能显示异常");
            }
        }

        // 2. 设置 HTML 内容
        // Flying Saucer 需要 XHTML 严格格式，Jsoup 已经处理过了
        renderer.setDocumentFromString(htmlContent);

        // 3. 布局计算
        renderer.layout();

        // 4. 创建 PDF
        try (FileOutputStream fos = new FileOutputStream(pdfPath)) {
            renderer.createPDF(fos);
        }
    }

    /**
     * 核心方法：PDF 转图片，并裁剪底部白色空白
     */
    private static void convertPdfToCroppedImages(String pdfPath, String outputFolder, int dpi) throws IOException {
        File outDir = new File(outputFolder);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int page = 0; page < pageCount; ++page) {
                // 1. 渲染图片 (RGB 模式)
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);

                // 2. 裁剪底部白色空白
                BufferedImage croppedImage = cropBottomWhiteSpace(image);

                // 3. 保存文件
                String fileName = "page_" + (page + 1) + ".png";
                File outputFile = new File(outDir, fileName);
                ImageIO.write(croppedImage, "PNG", outputFile);
                System.out.println("  -> 生成图片: " + fileName + " (尺寸:" + croppedImage.getWidth() + "x" + croppedImage.getHeight() + ")");
            }
        }
    }

    /**
     * 辅助方法：智能裁剪图片下方的纯白/近白区域
     * 算法：从底部向上扫描，找到第一个非白像素的行，截取该行之上部分
     */
    private static BufferedImage cropBottomWhiteSpace(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // 定义“白色”的阈值 (RGB 都大于 240 视为白色，抗锯齿边缘处理)
        int whiteThreshold = 240; 
        
        int bottomNonWhiteY = -1;

        // 从最底行向上遍历
        for (int y = height - 1; y >= 0; y--) {
            boolean rowIsWhite = true;
            // 优化：不必检查每一列，抽样检查或检查中间部分即可加速，这里为了精确检查整行
            // 如果图片很宽，可以只检查中间 80% 的区域，防止两侧边框干扰
            for (int x = 0; x < width; x++) {
                int pixel = originalImage.getRGB(x, y);
                Color c = new Color(pixel);
                if (c.getRed() < whiteThreshold || c.getGreen() < whiteThreshold || c.getBlue() < whiteThreshold) {
                    rowIsWhite = false;
                    break;
                }
            }

            if (!rowIsWhite) {
                bottomNonWhiteY = y;
                break;
            }
        }

        // 如果全是白的（极端情况），保留原图或只留一行，这里保留原图
        if (bottomNonWhiteY == -1 || bottomNonWhiteY == height - 1) {
            return originalImage;
        }

        // 裁剪：x, y, width, height
        // y 坐标是 top-left，所以高度是 bottomNonWhiteY + 1
        return originalImage.getSubimage(0, 0, width, bottomNonWhiteY + 1);
    }

    // ================= 测试 Main 方法 =================
    public static void main(String[] args) {
        // 模拟调用方：另起一个线程调用
        Thread workerThread = new Thread(() -> {
            // 参数示例
            String inputHtml = "/data/input/report.html"; 
            String outputDir = "/data/output/images";
            
            // 实际使用时请替换为真实路径
            // convert(inputHtml, outputDir, 144);
            
            System.out.println("线程已启动，等待真实路径配置后运行...");
        });
        
        workerThread.start();
    }
}