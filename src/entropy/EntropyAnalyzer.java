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
package entropy;

/**
 *
 * @author Slam
 */
public class EntropyAnalyzer {

    private static final int WINDOW_SIZE = 4096;
    private static final double HIGH_ENTROPY_THRESHOLD = 7.85;
    private static final double VERY_HIGH_ENTROPY = 8.0;

    // =========================================================
    // ENTROPÍA SHANNON
    // =========================================================
    public static double calculateShannonEntropy(byte[] data, int offset, int length) {
        if (offset + length > data.length || length <= 0) {
            length = Math.min(WINDOW_SIZE, data.length - offset);
        }

        int[] freq = new int[256];
        int actualLen = 0;

        for (int i = 0; i < length; i++) {
            if (offset + i >= data.length) {
                break;
            }
            freq[data[offset + i] & 0xFF]++;
            actualLen++;
        }

        if (actualLen == 0) {
            return 0.0;
        }

        double entropy = 0.0;
        for (int f : freq) {
            if (f == 0) {
                continue;
            }
            double p = (double) f / actualLen;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        return entropy;
    }

    public static double calculateEntropy(byte[] data) {
        return calculateShannonEntropy(data, 0, data.length);
    }

    // =========================================================
    // DETECCIÓN DE ALTA ENTROPÍA (basura / comprimido roto)
    // =========================================================
    public static boolean isHighEntropy(byte[] data, int offset, int length) {
        double entropy = calculateShannonEntropy(data, offset, length);
        return entropy > HIGH_ENTROPY_THRESHOLD;
    }

    public static boolean isHighEntropy(byte[] data) {
        return isHighEntropy(data, 0, data.length);
    }

    public static boolean isHighEntropyFast(byte[] data, int offset, int length) {
        int maxCheck = Math.min(length, 2048);
        int changes = 0;

        for (int i = 1; i < maxCheck; i++) {
            if (data[offset + i] != data[offset + i - 1]) {
                changes++;
            }
        }
        return (double) changes / maxCheck > 0.92; // umbral ajustado
    }

    // =========================================================
    // DETECCIÓN DE ESTRUCTURAS ÚTILES
    // =========================================================
    public static boolean containsUsefulStructure(byte[] data, int offset, int length) {
        int limit = Math.min(offset + length, data.length) - 4;

        for (int i = offset; i < limit; i++) {
            // ELF
            if (data[i] == 0x7F && data[i + 1] == 'E' && data[i + 2] == 'L' && data[i + 3] == 'F') {
                return true;
            }
            // SquashFS hsqs
            if (data[i] == (byte) 0x68 && data[i + 1] == (byte) 0x73
                    && data[i + 2] == (byte) 0x71 && data[i + 3] == (byte) 0x73) {
                return true;
            }
            // SquashFS sqsh
            if (data[i] == (byte) 0x73 && data[i + 1] == (byte) 0x71
                    && data[i + 2] == (byte) 0x73 && data[i + 3] == (byte) 0x68) {
                return true;
            }
            // uImage
            if (data[i] == (byte) 0x27 && data[i + 1] == (byte) 0x05
                    && data[i + 2] == (byte) 0x19 && data[i + 3] == (byte) 0x56) {
                return true;
            }
            // GZIP
            if (data[i] == (byte) 0x1F && data[i + 1] == (byte) 0x8B) {
                return true;
            }
            // ASCII (heurística simple)
            if (isAsciiBlock(data, i)) {
                return true;
            }
            // PNG
            if ((data[i] & 0xFF) == 0x89
                    && data[i + 1] == 0x50
                    && data[i + 2] == 0x4E
                    && data[i + 3] == 0x47) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsUsefulStructure(byte[] data) {
        return containsUsefulStructure(data, 0, data.length);
    }

    // =========================================================
    // DETECCIÓN TEXTO ASCII
    // =========================================================
    private static boolean isAsciiBlock(byte[] data, int offset) {

        int printable = 0;
        int len = 0;

        for (int i = offset; i < data.length && len < 64; i++, len++) {

            int b = data[i] & 0xFF;

            if (b >= 32 && b <= 126) {
                printable++;
            }
        }

        return printable > 48; // 75% printable
    }

    // =========================================================
    // ANALISIS POR VENTANA
    // =========================================================
    public static void printEntropyMap(byte[] data, int windowSize, int step) {
        for (int i = 0; i < data.length; i += step) {
            double ent = calculateShannonEntropy(data, i, windowSize);
            System.out.printf("0x%08X -> %.3f%n", i, ent);
        }
    }
}
