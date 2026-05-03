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
package model;

/**
 *
 * @author Slam
 */
public class DetectedEntry {

    private final int offset;
    private final String description;
    private final int size;
    private final SignatureType type;

    public DetectedEntry(int offset, String description, int size, SignatureType type) {
        this.offset = offset;
        this.description = description;
        this.size = size;
        this.type = type;
    }

    public int getOffset() {
        return offset;
    }

    public String getDescription() {
        return description;
    }

    public int getSize() {
        return size;
    }

    public SignatureType getType() {
        return type;
    }

    public boolean hasSize() {
        return size > 0;
    }

    public int getEndOffset() {
        return hasSize() ? offset + size : offset;
    }

    @Override
    public String toString() {
        return String.format(
                "0x%08X  |  %-40s  |  %10d bytes  |  %s",
                offset,
                description,
                size,
                type
        );
    }

    /*
    Tipo                Magic Bytes     Endianness  Notas
    uImage (U-Boot)     27 05 19 56       Big      Header de 64 bytes
    SquashFS (v4)       68 73 71 73       (hsqs)   LittleEl más común
    SquashFS (v4)       73 71 73 68       (sqsh)   BigMenos común
    JFFS2               85 19             Little   Aparece en cleanmarker
    UBI (erase count)   31 18 10 06       -        Para NAND
    LZMA (compressed)   5D 00 00 + props  -        Común después de uImage
    GZIP                1F 8B 08          -      
    ZIP                 50 4B 03 04       -
     */
}
