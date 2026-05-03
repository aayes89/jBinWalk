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
import utils.ByteUtils;

/**
 * Extractor para imágenes BMP
 */
public class BMPExtractor implements ExtractorStrategy {

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + 54 > firmwareData.length) {  // Header mínimo BMP
            return false;
        }

        // Firma "BM"
        if (firmwareData[offset] != 'B' || firmwareData[offset + 1] != 'M') {
            return false;
        }

        int fileSize = ByteUtils.readIntLittleEndian(firmwareData, offset + 2);
        int pixelOffset = ByteUtils.readIntLittleEndian(firmwareData, offset + 10);
        int dibHeaderSize = ByteUtils.readIntLittleEndian(firmwareData, offset + 14);

        // Validaciones fuertes
        if (fileSize < 54 || fileSize > firmwareData.length - offset) {
            return false;
        }
        if (pixelOffset < 54 || pixelOffset > fileSize) {
            return false;
        }
        if (dibHeaderSize < 40 || dibHeaderSize > 124) {
            return false;
        }

        try {
            byte[] bmpData = new byte[fileSize];
            System.arraycopy(firmwareData, offset, bmpData, 0, fileSize);

            String fileName = String.format("0x%08X_image.bmp", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), bmpData);

            System.out.printf("✓ BMP extraído: %s  (%,d bytes)%n", fileName, fileSize);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
