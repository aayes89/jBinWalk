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
 * Extractor básico para nodos JFFS2 (común en firmwares chinos)
 */
public class Jffs2Extractor implements ExtractorStrategy {

    private static final int MIN_NODE_SIZE = 12;
    private static final int MAX_NODE_SIZE = 1024 * 1024; // 1MB por nodo
    private static final int MAX_NODES = 2048;           // límite de seguridad

    @Override
    public boolean extract(byte[] firmwareData, DetectedEntry entry, File outputDir) {
        int offset = entry.getOffset();
        if (offset + MIN_NODE_SIZE > firmwareData.length) {
            return false;
        }

        int current = offset;
        int totalSize = 0;
        int nodeCount = 0;

        while (current + MIN_NODE_SIZE <= firmwareData.length && nodeCount < MAX_NODES) {
            int magic = ByteUtils.readShortLittleEndian(firmwareData, current) & 0xFFFF;

            if (magic != 0x1985) {  // Magic JFFS2
                break;
            }

            int nodeLength = ByteUtils.readIntLittleEndian(firmwareData, current + 4);

            if (nodeLength < MIN_NODE_SIZE || nodeLength > MAX_NODE_SIZE) {
                break;
            }

            if (current + nodeLength > firmwareData.length) {
                break;
            }

            totalSize += nodeLength;
            current += nodeLength;
            nodeCount++;
        }

        if (totalSize == 0 || nodeCount == 0) {
            return false;
        }

        try {
            byte[] jffs2Data = new byte[totalSize];
            System.arraycopy(firmwareData, offset, jffs2Data, 0, totalSize);

            String fileName = String.format("0x%08X_jffs2.bin", offset);
            File outputFile = new File(outputDir, fileName);

            Files.write(outputFile.toPath(), jffs2Data);

            System.out.printf("✓ JFFS2 extraído: %s  (%,d bytes | %d nodos)%n",
                    fileName, totalSize, nodeCount);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
