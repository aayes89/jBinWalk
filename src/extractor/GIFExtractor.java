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
import java.io.IOException;

import java.nio.file.Files;

/**
 * Extractor simple para imágenes GIF embebidas
 */
public class GIFExtractor implements ExtractorStrategy {

    private static final long MAX_GIF_SIZE = 16 * 1024 * 1024; // 16 MB

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + 10 > firmwareData.length) {
            return false;
        }

        // Verificar firma GIF87a o GIF89a
        if (!(firmwareData[offset] == 'G'
                && firmwareData[offset + 1] == 'I'
                && firmwareData[offset + 2] == 'F'
                && firmwareData[offset + 3] == '8'
                && (firmwareData[offset + 4] == '7' || firmwareData[offset + 4] == '9')
                && firmwareData[offset + 5] == 'a')) {
            return false;
        }

        // Buscar el trailer GIF (0x3B)
        int end = offset + 6;
        while (end < firmwareData.length && (end - offset) < MAX_GIF_SIZE) {
            if (firmwareData[end] == 0x3B) { // Trailer GIF
                end++;
                break;
            }
            end++;
        }

        int gifSize = end - offset;
        if (gifSize < 20) {
            return false;
        }

        try {
            byte[] gifData = new byte[gifSize];
            System.arraycopy(firmwareData, offset, gifData, 0, gifSize);

            String fileName = String.format("0x%08X_image.gif", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), gifData);

            System.out.printf("✓ GIF extraído: %s  (%,d bytes)%n", fileName, gifSize);
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
