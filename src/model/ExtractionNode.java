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
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en el árbol de extracción recursiva
 */
public class ExtractionNode {

    private final DetectedEntry entry;
    private final List<ExtractionNode> children = new ArrayList<>();
    private final int depth;
    private final int absoluteOffset;

    public ExtractionNode(DetectedEntry entry, int depth, int parentAbsoluteOffset) {
        this.entry = entry;
        this.depth = depth;
        this.absoluteOffset = parentAbsoluteOffset + entry.getOffset();
    }

    public DetectedEntry getEntry() {
        return entry;
    }

    public List<ExtractionNode> getChildren() {
        return children;
    }

    public int getDepth() {
        return depth;
    }

    public int getAbsoluteOffset() {
        return absoluteOffset;
    }

    public void addChild(ExtractionNode node) {
        if (node != null) {
            children.add(node);
        }
    }

    @Override
    public String toString() {
        return String.format("Depth=%d | Offset=0x%08X | %s (%d bytes)",
                depth, absoluteOffset, entry.getDescription(), entry.getSize());
    }
}
