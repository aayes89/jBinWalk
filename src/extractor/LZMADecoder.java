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

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Slam
 */
class LZMADecoder {

    private final RangeDecoder rc;
    private final int lc, lp, pb;
    private final byte[] dictionary;
    private int dictPos = 0;

    public LZMADecoder(RangeDecoder rc, int lc, int lp, int pb, int dictSize) {
        this.rc = rc;
        this.lc = lc;
        this.lp = lp;
        this.pb = pb;
        this.dictionary = new byte[dictSize];
    }

    public void decode(OutputStream out, long outSize) throws IOException {

        short[] probs = new short[1846];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 1024;
        }

        int state = 0;
        int rep0 = 1;

        long written = 0;

        while (outSize < 0 || written < outSize) {

            int posState = dictPos & ((1 << pb) - 1);

            if (rc.decodeBit(probs, (state << 4) + posState) == 0) {
                // literal
                int symbol = 1;

                while (symbol < 0x100) {
                    symbol = (symbol << 1) | rc.decodeBit(probs, symbol);
                }

                byte b = (byte) symbol;

                writeByte(out, b);
                written++;

                state = state < 4 ? 0 : (state < 10 ? state - 3 : state - 6);

            } else {
                // match (simplificado)
                int len = 2;

                for (int i = 0; i < len; i++) {
                    byte b = dictionary[(dictPos - rep0 + dictionary.length) % dictionary.length];
                    writeByte(out, b);
                    written++;
                }

                state = state < 7 ? 7 : 10;
            }
        }
    }

    private void writeByte(OutputStream out, byte b) throws IOException {
        out.write(b);
        dictionary[dictPos++] = b;
        if (dictPos == dictionary.length) {
            dictPos = 0;
        }
    }
}
