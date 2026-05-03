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
import java.util.zip.GZIPInputStream;

/**
 * Extractor para GZIP (muy común en firmwares)
 */
public class GzipExtractor implements ExtractorStrategy {

    private static final int BUFFER_SIZE = 8192;
    private static final long MAX_OUTPUT_SIZE = 64 * 1024 * 1024; // 64 MB

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + 10 > firmwareData.length) {
            return false;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(firmwareData, offset, firmwareData.length - offset); GZIPInputStream gzip = new GZIPInputStream(bais); LimitedByteArrayOutputStream out = new LimitedByteArrayOutputStream(MAX_OUTPUT_SIZE)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int len;

            while ((len = gzip.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            byte[] decompressed = out.toByteArray();

            if (decompressed.length < 512) {
                return false;
            }

            // Guardar archivo
            String fileName = String.format("0x%08X_gzip_decompressed.bin", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), decompressed);

            System.out.printf("✓ GZIP descomprimido: %s  (%,d bytes)%n", fileName, decompressed.length);
            return true;

        } catch (Exception e) {
            // GZIP fallido es común (falsos positivos)
            return false;
        }
    }

    // Clase auxiliar para evitar OOM
    private static class LimitedByteArrayOutputStream extends ByteArrayOutputStream {

        private final long maxSize;

        public LimitedByteArrayOutputStream(long maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            if (count + len > maxSize) {
                throw new RuntimeException("GZIP output too large");
            }
            super.write(b, off, len);
        }
    }
}
