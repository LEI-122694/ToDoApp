package com.example.qrcode;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public final class QrCodeGenerator {

    private QrCodeGenerator() { /* util */ }

    /**
     * Gera um BufferedImage contendo o QR Code do texto fornecido.
     *
     * @param text  texto a codificar
     * @param width largura do QR em pixels
     * @param height altura do QR em pixels
     * @return BufferedImage com o QR Code
     * @throws WriterException se ocorrer erro na geração
     */
    public static BufferedImage generate(String text, int width, int height) throws WriterException {
        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new MultiFormatWriter()
                .encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
