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
package scanner;

/**
 *
 * @author Slam
 */
import model.DetectedEntry;
import model.Signature;
import model.SignatureType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Endianness;
import utils.ByteUtils;

public class SignatureScanner {

    private final List<Signature> signatures;

    public SignatureScanner() {
        this.signatures = loadDefaultSignatures();
    }

    private List<Signature> loadDefaultSignatures() {
        List<Signature> list = new ArrayList<>();
        // uImage (U-Boot)
        list.add(new Signature("uImage",
                new byte[]{(byte) 0x27, (byte) 0x05, (byte) 0x19, (byte) 0x56},
                "uImage header (U-Boot)",
                Endianness.BIG, SignatureType.UIMAGE));

        // SquashFS little endian (hsqs) - el más común
        list.add(new Signature("SquashFS_hsqs",
                new byte[]{0x68, 0x73, 0x71, 0x73},
                "SquashFS LE",
                Endianness.LITTLE, SignatureType.SQUASHFS));

        // SquashFS big endian (sqsh)
        list.add(new Signature("SquashFS_sqsh",
                new byte[]{0x73, 0x71, 0x73, 0x68},
                "SquashFS BE",
                Endianness.BIG, SignatureType.SQUASHFS));

        // GZIP
        list.add(new Signature("GZIP",
                new byte[]{0x1F, (byte) 0x8B},
                "GZIP compressed",
                Endianness.NONE, SignatureType.GZIP));
        list.add(new Signature("ZIP",
                new byte[]{0x50, 0x4B, 0x03, 0x04},
                "ZIP archive",
                Endianness.NONE, SignatureType.ZIP));
        list.add(new Signature("TAR",
                new byte[]{0x75, 0x73, 0x74, 0x61, 0x72}, // ustar
                "TAR archive",
                Endianness.NONE, SignatureType.OTHER));

        // JFFS2
        list.add(new Signature("JFFS2",
                new byte[]{(byte) 0x85, 0x19},
                "JFFS2 node",
                Endianness.LITTLE, SignatureType.JFFS2));
        list.add(new Signature("UBI",
                new byte[]{0x55, 0x42, 0x49, 0x23}, // "UBI#"
                "UBI erase count header",
                Endianness.NONE, SignatureType.UBI));
        list.add(new Signature("UBI", new byte[]{'U', 'B', 'I', '#'},
                "UBI erase count header", Endianness.NONE, SignatureType.UBI));
        list.add(new Signature("CPIO",
                new byte[]{0x30, 0x37, 0x30, 0x37, 0x30, 0x31}, // "070701"
                "CPIO archive",
                Endianness.NONE, SignatureType.OTHER));
        list.add(new Signature("LZ4",
                new byte[]{0x04, 0x22, 0x4D, 0x18},
                "LZ4 compressed",
                Endianness.NONE, SignatureType.OTHER));
        list.add(new Signature("XZ",
                new byte[]{(byte) 0xFD, 0x37, 0x7A, 0x58, 0x5A, 0x00},
                "XZ compressed",
                Endianness.NONE, SignatureType.LZMA));

        // PNG
        list.add(new Signature("PNG",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
                "PNG file",
                Endianness.NONE, SignatureType.PNG));

        // JPG/JPEG
        list.add(new Signature("JPG/JPEG",
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0},
                "Joint photographic experts group",
                Endianness.NONE, SignatureType.JPG));

        list.add(new Signature("JFIF",
                new byte[]{(byte) 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46},
                "Joint photographic experts group",
                Endianness.NONE, SignatureType.JPEG));
        // GIF (GIF87a / GIF89a)
        list.add(new Signature("GIF87a",
                new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61},
                "GIF image",
                Endianness.NONE, SignatureType.GIF));

        list.add(new Signature("GIF89a",
                new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61},
                "GIF image",
                Endianness.NONE, SignatureType.GIF));

        // BMP
        list.add(new Signature("BMP",
                new byte[]{0x42, 0x4D}, // "BM"
                "Bitmap image",
                Endianness.LITTLE, SignatureType.BMP));

        // TIFF (II / MM)
        list.add(new Signature("TIFF_II",
                new byte[]{0x49, 0x49, 0x2A, 0x00},
                "TIFF image (LE)",
                Endianness.LITTLE, SignatureType.TIFF));

        list.add(new Signature("TIFF_MM",
                new byte[]{0x4D, 0x4D, 0x00, 0x2A},
                "TIFF image (BE)",
                Endianness.BIG, SignatureType.TIFF));
        // HTML
        list.add(new Signature("HTML",
                new byte[]{(byte) 0x3C, 0x21, 0x44, 0x4F, 0x43, 0x54, 0x59, 0x50, 0x45, 0x20, 0x68, 0x74, 0x6D, 0x6C, 0x3E},
                "HTML document",
                Endianness.NONE, SignatureType.OTHER));

        // LZMA (cabecera típica)
        list.add(new Signature("LZMA",
                new byte[]{(byte) 0x5D, (byte) 0x00, (byte) 0x00},
                "LZMA compressed data",
                Endianness.NONE, SignatureType.LZMA));

        list.add(new Signature("ELF",
                new byte[]{0x7F, 0x45, 0x4C, 0x46},
                "ELF executable",
                Endianness.NONE, SignatureType.OTHER));
        return list;
    }

    public List<DetectedEntry> scan(byte[] data) {
        List<DetectedEntry> findings = new ArrayList<>();

        for (Signature sig : signatures) {
            byte[] magic = sig.getMagic();
            for (int i = 0; i <= data.length - magic.length; i++) {

                if (!matches(data, i, magic)) {
                    continue;
                }

                if (!validate(data, i, sig)) {
                    continue;
                }

                int estimatedSize = estimateSize(data, i, sig);

                findings.add(new DetectedEntry(i, sig.getDescription(), estimatedSize, sig.getType()));
            }
        }

        // Orden correcto: siempre por offset
        findings.sort(Comparator.comparingInt(DetectedEntry::getOffset));
        findings = removeOverlaps(findings);

        return findings;
    }

    private boolean matches(byte[] data, int offset, byte[] magic) {
        for (int j = 0; j < magic.length; j++) {
            if (data[offset + j] != magic[j]) {
                return false;
            }
        }
        return true;
    }

    private boolean validate(byte[] data, int offset, Signature sig) {

        switch (sig.getType()) {

            case UIMAGE:
                if (offset + 64 > data.length) {
                    return false;
                }

                int magic = ByteUtils.readIntBigEndian(data, offset);
                int size = ByteUtils.readIntBigEndian(data, offset + 0x0C);

                return magic == 0x27051956
                        && size > 1024
                        && size < (data.length - offset)
                        && offset % 4 == 0;

            case SQUASHFS:
                if (offset + 48 > data.length) {
                    return false;
                }

                long bytesUsed = (sig.getEndianness() == Endianness.LITTLE)
                        ? ByteUtils.readLongLittleEndian(data, offset + 40)
                        : ByteUtils.readLongBigEndian(data, offset + 40);

                return bytesUsed > 0 && bytesUsed < (data.length - offset);

            case GZIP:
                if (offset + 10 > data.length) {
                    return false;
                }

                int method = data[offset + 2] & 0xFF;
                return method == 8;

            case JFFS2:
                if (offset + 12 > data.length) {
                    return false;
                }

                int magicJ = ByteUtils.readShortLittleEndian(data, offset) & 0xFFFF;
                if (magicJ != 0x1985) {
                    return false;
                }

                int nodeLength = ByteUtils.readIntLittleEndian(data, offset + 4);

                if (nodeLength < 12 || nodeLength > 0x10000) {
                    return false;
                }

                // eliminar ruido típico NAND
                if ((offset % 0x10000) == 0) {
                    return false;
                }

                return true;

            case PNG:
                if (offset + 8 > data.length) {
                    return false;
                }

                // validación real de firma PNG completa
                return data[offset] == (byte) 0x89
                        && data[offset + 1] == 0x50
                        && data[offset + 2] == 0x4E
                        && data[offset + 3] == 0x47;
            case GIF:
                return offset + 6 <= data.length;

            case BMP:
                if (offset + 54 > data.length) {
                    return false; // header mínimo BMP real
                }
                int fileSize = ByteUtils.readIntLittleEndian(data, offset + 2);
                int pixelOffset = ByteUtils.readIntLittleEndian(data, offset + 10);
                int dibSize = ByteUtils.readIntLittleEndian(data, offset + 14);
                int width = ByteUtils.readIntLittleEndian(data, offset + 18);
                int height = ByteUtils.readIntLittleEndian(data, offset + 22);

                // Validaciones duras
                if (fileSize <= 54 || fileSize > (data.length - offset)) {
                    return false;
                }
                if (pixelOffset < 54 || pixelOffset > fileSize) {
                    return false;
                }
                if (dibSize < 40 || dibSize > 124) {
                    return false;
                }
                if (width <= 0 || height <= 0) {
                    return false;
                }
                if (width > 10000 || height > 10000) {
                    return false;
                }

                return true;

            case TIFF:
                if (offset + 8 > data.length) {
                    return false;
                }

                int magicTIFF = (sig.getEndianness() == Endianness.LITTLE)
                        ? ByteUtils.readShortLittleEndian(data, offset + 2)
                        : ByteUtils.readShortBigEndian(data, offset + 2);

                return magicTIFF == 42;

            case ZIP:
                if (offset + 4 > data.length) {
                    return false;
                }

                int sigZip = ByteUtils.readIntLittleEndian(data, offset);
                return sigZip == 0x04034B50;

            case UBI:
                if (offset + 64 > data.length) {
                    return false;
                }

                return data[offset] == 0x55
                        && // U
                        data[offset + 1] == 0x42
                        && // B
                        data[offset + 2] == 0x49
                        && // I
                        data[offset + 3] == 0x23;   // #

            default:
                return true;
        }
    }

    private int estimateSize(byte[] data, int offset, Signature sig) {

        switch (sig.getType()) {

            case UIMAGE:
                int payload = ByteUtils.readIntBigEndian(data, offset + 0x0C);
                if (payload <= 0) {
                    return 0;
                }

                int total = 64 + payload;
                return Math.min(total, data.length - offset);

            case SQUASHFS:
                return calculateSquashFSSize(data, offset, sig.getEndianness());

            case ZIP:
                // tamaño desconocido → dejar que extractor lo maneje
                return 0;

            case PNG:
                // tamaño dinámico → extractor lo calcula por chunks
                return 0;
            case BMP:
                int sizeBmp = ByteUtils.readIntLittleEndian(data, offset + 2);

                if (sizeBmp > 54 && sizeBmp <= (data.length - offset)) {
                    return sizeBmp;
                }

                return 0;

            case GIF:
                return 0; // variable → extractor

            case TIFF:
                return 0; // complejo → extractor

            default:
                return 0;
        }
    }

    private int calculateSquashFSSize(byte[] data, int offset, Endianness endian) {

        if (offset + 48 > data.length) {
            return 0;
        }

        long bytesUsed = (endian == Endianness.LITTLE)
                ? ByteUtils.readLongLittleEndian(data, offset + 40)
                : ByteUtils.readLongBigEndian(data, offset + 40);

        if (bytesUsed > 0 && bytesUsed < data.length) {
            return (int) bytesUsed;
        }

        return 0;
    }

    private List<DetectedEntry> removeOverlaps(List<DetectedEntry> list) {
        List<DetectedEntry> result = new ArrayList<>();

        for (DetectedEntry current : list) {

            boolean overlapped = false;

            for (DetectedEntry accepted : result) {

                if (accepted.getSize() == 0 || current.getSize() == 0) {
                    continue;
                }

                int startA = accepted.getOffset();
                int endA = startA + accepted.getSize();

                int startB = current.getOffset();
                int endB = startB + current.getSize();

                // overlap completo (bidireccional)
                if ((startB >= startA && startB < endA)
                        || (startA >= startB && startA < endB)) {
                    overlapped = true;
                    break;
                }
            }

            if (!overlapped) {
                result.add(current);
            }
        }

        return result;
    }

    // --- LZMA validación heurística
    private boolean isValidLZMA(byte[] data, int offset) {

        if (offset + 13 > data.length) {
            return false;
        }

        int props = data[offset] & 0xFF;

        int lc = props % 9;
        int remainder = props / 9;
        int lp = remainder % 5;
        int pb = remainder / 5;

        if (lc > 8 || lp > 4 || pb > 4) {
            return false;
        }

        int dict = ByteUtils.readIntLittleEndian(data, offset + 1);

        // endurecer validación (evita falsos positivos masivos)
        if (dict < 4096 || dict > (64 * 1024 * 1024)) {
            return false;
        }

        long size = ByteUtils.readLongLittleEndian(data, offset + 5);

        if (size == -1) {
            return true;
        }

        if (size <= 0 || size > (256L * 1024 * 1024)) {
            return false;
        }

        return true;
    }

}
