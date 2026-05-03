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
import java.io.FileOutputStream;
import java.nio.file.Files;

/**
 * Extractor para imágenes JPEG/JFIF
 */
public class JPGExtractor implements ExtractorStrategy {

    private static final long MAX_JPG_SIZE = 32 * 1024 * 1024; // 32 MB

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + 4 > firmwareData.length) {
            return false;
        }

        // Verificar SOI (Start of Image): FF D8
        if ((firmwareData[offset] & 0xFF) != 0xFF || (firmwareData[offset + 1] & 0xFF) != 0xD8) {
            return false;
        }

        int pos = offset + 2;
        boolean foundEOI = false;

        while (pos + 1 < firmwareData.length && (pos - offset) < MAX_JPG_SIZE) {
            if ((firmwareData[pos] & 0xFF) != 0xFF) {
                pos++;
                continue;
            }

            int marker = firmwareData[pos + 1] & 0xFF;

            // End of Image (EOI)
            if (marker == 0xD9) {
                pos += 2;
                foundEOI = true;
                break;
            }

            // Markers sin longitud (RST0-RST7, SOI, EOI, etc.)
            if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) {
                pos += 2;
                continue;
            }

            // Markers con longitud
            if (pos + 4 > firmwareData.length) {
                break;
            }

            int length = ((firmwareData[pos + 2] & 0xFF) << 8) | (firmwareData[pos + 3] & 0xFF);
            if (length < 2) {
                break;
            }

            pos += 2 + length;
        }

        if (!foundEOI) {
            return false; // No se encontró el final del JPEG
        }

        int jpgSize = pos - offset;
        if (jpgSize < 100) {
            return false;
        }

        try {
            byte[] jpgData = new byte[jpgSize];
            System.arraycopy(firmwareData, offset, jpgData, 0, jpgSize);

            String fileName = String.format("0x%08X_image.jpg", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), jpgData);

            System.out.printf("✓ JPEG extraído: %s  (%,d bytes)%n", fileName, jpgSize);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
