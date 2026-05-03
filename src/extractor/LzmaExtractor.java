/*
 * The MIT License
 *
 * Copyright 2026 Slam.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package extractor;

/**
 *
 * @author Slam
 */
import java.io.*;
import java.nio.file.Files;
import model.DetectedEntry;
import utils.ByteUtils;

/**
 * Extractor para LZMA (con y sin header) Versión limpia, segura y mantenible.
 */
public class LzmaExtractor implements ExtractorStrategy {

    private static final long MAX_OUTPUT_SIZE = 64L * 1024 * 1024; // 64MB máximo
    private static final int MIN_VALID_OUTPUT = 2048;

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset < 0 || offset >= firmwareData.length) {
            return false;
        }

        byte[] result = null;

        // Intentar primero con header estándar
        if (looksLikeLzmaHeader(firmwareData, offset)) {
            result = decodeWithHeader(firmwareData, offset);
        }

        // Si falla o no tiene header → intentar raw
        if (result == null || result.length < MIN_VALID_OUTPUT) {
            result = decodeRawBrute(firmwareData, offset);
        }

        if (result == null || !isValidOutput(result)) {
            return false;
        }

        // Guardar resultado
        try {
            String suffix = looksLikeLzmaHeader(firmwareData, offset) ? "lzma" : "lzma_raw";
            String fileName = String.format("0x%08X_%s.bin", offset, suffix);
            File outFile = new File(outputDir, fileName);

            Files.write(outFile.toPath(), result);

            System.out.printf("✓ LZMA descomprimido: %s  (%,d bytes)%n", fileName, result.length);
            return true;

        } catch (IOException e) {
            System.err.println("Error guardando LZMA en " + offset);
            return false;
        }
    }

    // =========================================================
    // DETECCIÓN DE HEADER LZMA
    // =========================================================
    private boolean looksLikeLzmaHeader(byte[] data, int offset) {
        if (offset + 13 > data.length) {
            return false;
        }

        int props = data[offset] & 0xFF;
        int lc = props % 9;
        int rem = props / 9;
        int lp = rem % 5;
        int pb = rem / 5;

        if (lc > 8 || lp > 4 || pb > 4) {
            return false;
        }

        int dictSize = ByteUtils.readIntLittleEndian(data, offset + 1);
        if (dictSize < (1 << 12) || dictSize > 64 * 1024 * 1024) {
            return false;
        }

        return true;
    }

    // =========================================================
    // LZMA CON HEADER
    // =========================================================
    private byte[] decodeWithHeader(byte[] data, int offset) {
        try {
            // Aquí iría tu implementación real con RangeDecoder + LZMADecoder
            // Por ahora devolvemos null para que intente el modo raw
            return null;   // ← Reemplaza cuando tengas el decoder completo
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================
    // MODO RAW (Brute force parámetros comunes)
    // =========================================================
    private byte[] decodeRawBrute(byte[] data, int offset) {
        int[][] commonParams = {
            {3, 0, 2}, {3, 0, 3}, {3, 1, 2}
        };
        int[] dictSizes = {1 << 20, 4 << 20, 8 << 20, 16 << 20};

        for (int[] p : commonParams) {
            for (int dict : dictSizes) {
                byte[] result = tryDecodeRaw(data, offset, p[0], p[1], p[2], dict);
                if (result != null && isValidOutput(result)) {
                    return result;
                }
            }
        }
        return null;
    }

    private byte[] tryDecodeRaw(byte[] data, int offset, int lc, int lp, int pb, int dictSize) {
        try {
            // Placeholder - reemplaza con tu decoder real
            // RangeDecoder + LZMADecoder
            throw new UnsupportedOperationException("LZMA Decoder no implementado aún");
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    // =========================================================
    // VALIDACIÓN FUERTE
    // =========================================================
    private boolean isValidOutput(byte[] data) {
        if (data == null || data.length < MIN_VALID_OUTPUT) {
            return false;
        }
        if (data.length >= MAX_OUTPUT_SIZE) {
            return false;
        }

        int hits = 0;
        int checkLimit = Math.min(data.length - 4, 16384);

        for (int i = 0; i < checkLimit; i++) {
            // ELF, SquashFS, uImage, GZIP
            if (i + 4 < data.length) {
                if ((data[i] == 0x7F && data[i + 1] == 'E' && data[i + 2] == 'L' && data[i + 3] == 'F')
                        || (data[i] == 0x68 && data[i + 1] == 0x73 && data[i + 2] == 0x71 && data[i + 3] == 0x73)
                        || (data[i] == 0x27 && data[i + 1] == 0x05 && data[i + 2] == 0x19 && data[i + 3] == 0x56)
                        || (data[i] == 0x1F && data[i + 1] == (byte) 0x8B)) {
                    return true;
                }
            }

            // Texto legible
            if (data[i] >= 32 && data[i] <= 126) {
                hits++;
            }
        }

        return hits > 50; // al menos algo de estructura
    }
}
