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
import model.Endianness;
import utils.ByteUtils;
import java.io.File;
import java.nio.file.Files;

/**
 * Extractor para sistemas de archivos SquashFS (el más crítico para firmwares)
 */
public class SquashFSExtractor implements ExtractorStrategy {

    private static final int SUPERBLOCK_SIZE = 96;
    private static final long MAX_SQUASHFS_SIZE = 128L * 1024 * 1024; // 128 MB límite seguro

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();

        if (offset + SUPERBLOCK_SIZE > firmwareData.length) {
            return false;
        }

        // Detectar endianness
        Endianness endian = detectEndianness(firmwareData, offset);
        if (endian == null) {
            return false;
        }

        // Leer tamaño real del filesystem desde el superblock
        long bytesUsed = (endian == Endianness.LITTLE)
                ? ByteUtils.readLongLittleEndian(firmwareData, offset + 40)
                : ByteUtils.readLongBigEndian(firmwareData, offset + 40);

        if (bytesUsed <= SUPERBLOCK_SIZE || bytesUsed > MAX_SQUASHFS_SIZE) {
            return false;
        }

        int realSize = (int) Math.min(bytesUsed, firmwareData.length - offset);

        try {
            byte[] squashfsData = new byte[realSize];
            System.arraycopy(firmwareData, offset, squashfsData, 0, realSize);

            String fileName = String.format("0x%08X_squashfs.img", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), squashfsData);

            System.out.printf("✓ SquashFS extraído: %s  (%,d bytes | %s)%n",
                    fileName, realSize, endian);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Endianness detectEndianness(byte[] data, int offset) {
        if (offset + 4 > data.length) {
            return null;
        }

        // hsqs → Little Endian (el más común)
        if (data[offset] == 0x68 && data[offset + 1] == 0x73
                && data[offset + 2] == 0x71 && data[offset + 3] == 0x73) {
            return Endianness.LITTLE;
        }

        // sqsh → Big Endian
        if (data[offset] == 0x73 && data[offset + 1] == 0x71
                && data[offset + 2] == 0x73 && data[offset + 3] == 0x68) {
            return Endianness.BIG;
        }

        return null;
    }
}
