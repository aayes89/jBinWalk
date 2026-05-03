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
import model.DetectedEntry;
import java.io.*;
import java.nio.file.Files;

/**
 * Extractor para streams LZMA raw (sin cabecera .lzma) Versión segura y robusta
 * para análisis de firmwares.
 */
public class LzmaRawExtractor implements ExtractorStrategy {

    private static final int DEFAULT_DICT_SIZE = 8 * 1024 * 1024;  // 8MB
    private static final int MAX_OUTPUT_SIZE = 32 * 1024 * 1024;   // 32MB límite de seguridad
    private static final int MIN_VALID_OUTPUT = 512;

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset < 0 || offset >= firmwareData.length) {
            return false;
        }

        try {
            // Intentar descompresión
            byte[] decompressed = decompressLZMARaw(firmwareData, offset);

            if (decompressed == null || !isValidDecompressedData(decompressed)) {
                return false;
            }

            // Guardar resultado
            String fileName = String.format("0x%08X_lzma_decompressed.bin", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), decompressed);

            System.out.printf("✓ LZMA Raw descomprimido: %s  (%,d bytes)%n",
                    fileName, decompressed.length);

            return true;

        } catch (Exception e) {
            // Silencioso en la mayoría de casos (muchos falsos positivos)
            return false;
        }
    }

    /**
     * Descomprime un stream LZMA raw
     */
    private byte[] decompressLZMARaw(byte[] data, int offset) throws IOException {
        // Aquí necesitarás una implementación real de LZMA decoder.
        // Como no quieres librerías externas, te dejo dos opciones:

        // Opción 1: Placeholder (reemplaza cuando implementes el decoder)
        throw new UnsupportedOperationException(
                "Implementa un LZMA Decoder puro en Java (RangeDecoder + LZMADecoder) o usa Apache Commons Compress");

        // Opción 2: Si ya tienes las clases RangeDecoder y LZMADecoder en tu proyecto:
        /*
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, data.length - offset);
             LimitedByteArrayOutputStream out = new LimitedByteArrayOutputStream(MAX_OUTPUT_SIZE)) {

            // Ejemplo con tus clases anteriores
            RangeDecoder rangeDecoder = new RangeDecoder(bis);
            LZMADecoder decoder = new LZMADecoder(rangeDecoder, 3, 0, 2, DEFAULT_DICT_SIZE);
            
            decoder.decode(out, MAX_OUTPUT_SIZE);
            return out.toByteArray();
        }
         */
    }

    /**
     * Validación fuerte para evitar basura
     */
    private boolean isValidDecompressedData(byte[] data) {
        if (data == null || data.length < MIN_VALID_OUTPUT) {
            return false;
        }

        // Si es demasiado grande → probablemente falló
        if (data.length >= MAX_OUTPUT_SIZE - 1024) {
            return false;
        }

        int usefulHits = 0;
        int limit = Math.min(data.length - 4, 8192);

        for (int i = 0; i < limit; i++) {
            // ELF
            if (data[i] == 0x7F && data[i + 1] == 'E' && data[i + 2] == 'L' && data[i + 3] == 'F') {
                return true;
            }
            // SquashFS
            if (data[i] == 0x68 && data[i + 1] == 0x73 && data[i + 2] == 0x71 && data[i + 3] == 0x73) {
                return true;
            }
            if (data[i] == 0x73 && data[i + 1] == 0x71 && data[i + 2] == 0x73 && data[i + 3] == 0x68) {
                return true;
            }
            // uImage
            if (data[i] == 0x27 && data[i + 1] == 0x05 && data[i + 2] == 0x19 && data[i + 3] == 0x56) {
                return true;
            }
            // GZIP
            if (data[i] == 0x1F && data[i + 1] == (byte) 0x8B) {
                return true;
            }
            // Texto ASCII (heurística)
            if (isPrintableAscii(data, i)) {
                usefulHits++;
            }
        }

        return usefulHits > 20; // al menos algo de texto estructurado
    }

    private boolean isPrintableAscii(byte[] data, int i) {
        if (i + 32 >= data.length) {
            return false;
        }
        int printable = 0;
        for (int j = 0; j < 32; j++) {
            int b = data[i + j] & 0xFF;
            if (b >= 32 && b <= 126) {
                printable++;
            }
        }
        return printable > 24;
    }

    // Clase interna de protección
    private static class LimitedByteArrayOutputStream extends ByteArrayOutputStream {

        private final int maxSize;

        public LimitedByteArrayOutputStream(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            if (count + len > maxSize) {
                throw new RuntimeException("LZMA output exceeded maximum allowed size");
            }
            super.write(b, off, len);
        }
    }
}
