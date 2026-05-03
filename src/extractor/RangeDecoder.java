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
import java.io.InputStream;

/**
 *
 * @author Slam
 */
class RangeDecoder {

    private final InputStream in;
    private int code = 0;
    private int range = 0xFFFFFFFF;

    public RangeDecoder(InputStream in) throws IOException {
        this.in = in;

        for (int i = 0; i < 5; i++) {
            code = (code << 8) | in.read();
        }
    }

    public int decodeBit(short[] probs, int index) throws IOException {
        int prob = probs[index] & 0xFFFF;

        int bound = (range >>> 11) * prob;

        if ((code ^ 0x80000000) < (bound ^ 0x80000000)) {
            range = bound;
            probs[index] += (2048 - prob) >>> 5;

            if ((range & 0xFF000000) == 0) {
                range <<= 8;
                code = (code << 8) | in.read();
            }
            return 0;
        } else {
            range -= bound;
            code -= bound;
            probs[index] -= prob >>> 5;

            if ((range & 0xFF000000) == 0) {
                range <<= 8;
                code = (code << 8) | in.read();
            }
            return 1;
        }
    }
}
