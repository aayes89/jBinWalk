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
public class Signature {

    private final String name;
    private final byte[] magic;
    private final String description;
    private final Endianness endianness;
    private final SignatureType type;

    public Signature(String name, byte[] magic, String description,
            Endianness endianness, SignatureType type) {
        this.name = name;
        this.magic = magic.clone(); // defensive copy
        this.description = description;
        this.endianness = endianness;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public byte[] getMagic() {
        return magic.clone();
    }

    public String getDescription() {
        return description;
    }

    public Endianness getEndianness() {
        return endianness;
    }

    public SignatureType getType() {
        return type;
    }
}
