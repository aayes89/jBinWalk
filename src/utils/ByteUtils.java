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
package utils;

/**
 *
 * @author Slam
 */
public class ByteUtils {

    private static boolean isWithinBounds(byte[] data, int offset, int length) {
        return offset >= 0 && offset + length <= data.length;
    }

    public static int readIntBE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    public static int readIntLittleEndian(byte[] data, int offset) {
        if (!isWithinBounds(data, offset, 4)) {
            return 0;
        }

        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    public static int readIntBigEndian(byte[] data, int offset) {
        if (!isWithinBounds(data, offset, 4)) {
            return 0;
        }

        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    public static long readLongLittleEndian(byte[] data, int offset) {
        if (!isWithinBounds(data, offset, 8)) {
            return 0;
        }

        return (data[offset] & 0xFFL)
                | ((data[offset + 1] & 0xFFL) << 8)
                | ((data[offset + 2] & 0xFFL) << 16)
                | ((data[offset + 3] & 0xFFL) << 24)
                | ((data[offset + 4] & 0xFFL) << 32)
                | ((data[offset + 5] & 0xFFL) << 40)
                | ((data[offset + 6] & 0xFFL) << 48)
                | ((data[offset + 7] & 0xFFL) << 56);
    }

    public static long readLongBigEndian(byte[] data, int offset) {
        if (!isWithinBounds(data, offset, 8)) {
            return 0;
        }

        return ((data[offset] & 0xFFL) << 56)
                | ((data[offset + 1] & 0xFFL) << 48)
                | ((data[offset + 2] & 0xFFL) << 40)
                | ((data[offset + 3] & 0xFFL) << 32)
                | ((data[offset + 4] & 0xFFL) << 24)
                | ((data[offset + 5] & 0xFFL) << 16)
                | ((data[offset + 6] & 0xFFL) << 8)
                | (data[offset + 7] & 0xFFL);
    }

    public static short readShortLittleEndian(byte[] data, int offset) {
        if (!isWithinBounds(data, offset, 2)) {
            return 0;
        }

        return (short) ((data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8));
    }

    public static short readShortBigEndian(byte[] data, int offset) {
        if (!isWithinBounds(data, offset, 2)) {
            return 0;
        }

        return (short) (((data[offset] & 0xFF) << 8)
                | (data[offset + 1] & 0xFF));
    }

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        if (bytes == null || offset < 0 || length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(length * 3);

        for (int i = 0; i < length && offset + i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[offset + i]));
        }

        return sb.toString().trim();
    }
}
