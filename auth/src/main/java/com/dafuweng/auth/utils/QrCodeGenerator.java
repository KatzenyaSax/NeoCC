package com.dafuweng.auth.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 */
public class QrCodeGenerator {
    
    /**
     * 生成二维码 Base64 图片
     * 
     * @param content 二维码内容（URL 或文本）
     * @param width 宽度（像素）
     * @param height 高度（像素）
     * @return Base64 编码的 PNG 图片（data:image/png;base64,...）
     * @throws WriterException 二维码生成异常
     * @throws IOException IO 异常
     */
    public static String generateQrCodeBase64(String content, int width, int height) 
            throws WriterException, IOException {
        
        // 配置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");  // 字符编码
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H); // 容错级别
        hints.put(EncodeHintType.MARGIN, 2); // 边距
        
        // 生成二维码矩阵
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        
        // 将矩阵转换为图片
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        // 转换为 Base64
        byte[] imageBytes = outputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        // 返回 data URL 格式
        return "data:image/png;base64," + base64Image;
    }
    
    /**
     * 生成二维码 Base64 图片（默认尺寸 300x300）
     * 
     * @param content 二维码内容
     * @return Base64 编码的 PNG 图片
     * @throws WriterException 二维码生成异常
     * @throws IOException IO 异常
     */
    public static String generateQrCodeBase64(String content) 
            throws WriterException, IOException {
        return generateQrCodeBase64(content, 300, 300);
    }
}
