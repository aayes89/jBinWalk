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
import java.nio.file.Files;

/**
 * Extractor para imágenes TIFF
 */
public class TIFFExtractor implements ExtractorStrategy {

    private static final long MAX_TIFF_SIZE = 32 * 1024 * 1024; // 32 MB

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + 8 > firmwareData.length) {
            return false;
        }

        // Detectar endianness TIFF (II* o MM*)
        boolean isLittleEndian = firmwareData[offset] == 'I' && firmwareData[offset + 1] == 'I'
                && firmwareData[offset + 2] == 0x2A && firmwareData[offset + 3] == 0x00;

        boolean isBigEndian = firmwareData[offset] == 'M' && firmwareData[offset + 1] == 'M'
                && firmwareData[offset + 2] == 0x00 && firmwareData[offset + 3] == 0x2A;

        if (!isLittleEndian && !isBigEndian) {
            return false;
        }

        // TIFF no tiene un marcador de fin claro → tomamos hasta un tamaño razonable
        int size = (int) Math.min(MAX_TIFF_SIZE, firmwareData.length - offset);

        try {
            byte[] tiffData = new byte[size];
            System.arraycopy(firmwareData, offset, tiffData, 0, size);

            String fileName = String.format("0x%08X_image.tiff", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), tiffData);

            System.out.printf("✓ TIFF extraído: %s  (%,d bytes)%n", fileName, size);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
