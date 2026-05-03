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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import utils.ByteUtils;

/**
 * Extractor mejorado para PNG
 */
public class PNGExtractor implements ExtractorStrategy {

    private static final long MAX_PNG_SIZE = 20 * 1024 * 1024; // 20 MB

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + 8 > firmwareData.length) {
            return false;
        }

        // Firma PNG
        if (firmwareData[offset] != (byte) 0x89
                || firmwareData[offset + 1] != 0x50
                || firmwareData[offset + 2] != 0x4E
                || firmwareData[offset + 3] != 0x47
                || firmwareData[offset + 4] != 0x0D
                || firmwareData[offset + 5] != 0x0A
                || firmwareData[offset + 6] != 0x1A
                || firmwareData[offset + 7] != 0x0A) {
            return false;
        }

        // Buscar IEND (mejor heurística)
        int pos = offset + 8;
        boolean foundIEND = false;
        int maxSearch = Math.min(offset + (int) MAX_PNG_SIZE, firmwareData.length);

        while (pos + 12 <= maxSearch) {
            int length = ByteUtils.readIntBigEndian(firmwareData, pos);
            int chunkType = ByteUtils.readIntBigEndian(firmwareData, pos + 4);

            if (length < 0 || length > MAX_PNG_SIZE) {
                break;
            }

            if (chunkType == 0x49454E44) { // IEND
                pos += 8 + length + 4;
                foundIEND = true;
                break;
            }

            pos += 8 + length + 4;
        }

        if (!foundIEND) {
            // Si no encuentra IEND, tomar un tamaño razonable
            pos = Math.min(offset + 1024 * 1024, firmwareData.length); // máximo 1MB si no hay IEND
        }

        int pngSize = pos - offset;
        if (pngSize < 100) {
            return false;
        }

        try {
            byte[] pngData = new byte[pngSize];
            System.arraycopy(firmwareData, offset, pngData, 0, pngSize);

            String fileName = String.format("0x%08X_image.png", offset);
            Files.write(new File(outputDir, fileName).toPath(), pngData);

            System.out.printf("✓ PNG extraído: %s  (%,d bytes)%n", fileName, pngSize);
            return true;

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
