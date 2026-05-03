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
 *
 */
import model.DetectedEntry;
import utils.ByteUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Extractor especializado para uImage (U-Boot legacy format) Extrae tanto el
 * uImage completo como el payload (kernel) por separado.
 */
public class UImageExtractor implements ExtractorStrategy {

    private static final int HEADER_SIZE = 64;
    private static final int MAGIC_UIMAGE = 0x27051956;

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {

        int offset = entry.getOffset();

        if (offset + HEADER_SIZE > firmwareData.length) {
            return false;
        }

        if (ByteUtils.readIntBigEndian(firmwareData, offset) != MAGIC_UIMAGE) {
            return false;
        }

        int payloadSize = ByteUtils.readIntBigEndian(firmwareData, offset + 0x0C);
        int compressionType = firmwareData[offset + 0x1F] & 0xFF;

        // Validación REAL
        if (payloadSize <= 0 || payloadSize > (firmwareData.length - offset - HEADER_SIZE)) {
            return false;
        }

        int totalSize = HEADER_SIZE + payloadSize;

        try {
            // ✔ escribir SIN copiar todo el buffer
            File uimageFile = new File(outputDir,
                    String.format("0x%08X_uImage.bin", offset));

            try (FileOutputStream fos = new FileOutputStream(uimageFile)) {
                fos.write(firmwareData, offset, totalSize);
            }

            // ✔ payload directo sin copiar completo innecesario
            File kernelFile = new File(outputDir,
                    String.format("0x%08X_kernel.%s",
                            offset,
                            getExtension(compressionType)));

            try (FileOutputStream fos = new FileOutputStream(kernelFile)) {
                fos.write(firmwareData, offset + HEADER_SIZE, payloadSize);
            }

            System.out.printf("OK extract: UIMAGE size=%d (comp=%s)%n",
                    payloadSize, getExtension(compressionType));

            return true;

        } catch (IOException e) {
            System.err.println("uImage write error @0x"
                    + Integer.toHexString(offset));
            return false;
        }
    }

    private String getExtension(int comp) {
        return switch (comp) {
            case 0 ->
                "bin";
            case 1 ->
                "gz";
            case 2 ->
                "bz2";
            case 3 ->
                "lzma";
            case 4 ->
                "lzo";
            case 5 ->
                "lz4";
            default ->
                "raw";
        };
    }   
    
}
