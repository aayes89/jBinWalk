
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
import java.util.HashSet;
import java.util.Set;

/**
 * Controla el estado de la extracción recursiva (profundidad, visitados,
 * límites)
 */
public class ExtractionContext {

    private final int maxDepth;
    private final Set<Long> visited = new HashSet<>();
    private int extractionCount = 0;
    private long totalBytesProcessed = 0;

    private static final int MAX_EXTRACTIONS = 300;           // límite razonable
    private static final long MAX_TOTAL_BYTES = 256L * 1024 * 1024; // 256 MB

    public ExtractionContext(int maxDepth) {
        this.maxDepth = Math.max(1, maxDepth);
    }

    public boolean canContinue(int currentDepth) {
        return currentDepth < maxDepth;
    }

    public boolean isVisited(int offset, int size) {
        long key = buildKey(offset, size);
        return visited.contains(key);
    }

    public void markVisited(int offset, int size) {
        visited.add(buildKey(offset, size));
    }

    private long buildKey(int offset, int size) {
        // Combinación segura de offset + tamaño
        return (((long) offset) << 32) | (size & 0xFFFFFFFFL);
    }

    public boolean canExtractMore() {
        return extractionCount++ < MAX_EXTRACTIONS;
    }

    public boolean allowBytes(long bytes) {
        totalBytesProcessed += bytes;
        return totalBytesProcessed <= MAX_TOTAL_BYTES;
    }

    // Getters útiles para reportes
    public int getExtractionCount() {
        return extractionCount;
    }

    public long getTotalBytesProcessed() {
        return totalBytesProcessed;
    }

    public int getVisitedCount() {
        return visited.size();
    }
}
