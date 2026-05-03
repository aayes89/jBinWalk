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
import entropy.EntropyAnalyzer;
import model.DetectedEntry;
import model.ExtractionNode;
import model.SignatureType;
import scanner.SignatureScanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extractor recursivo (Matryoshka) para firmwares Versión corregida, segura y
 * mantenible.
 */
public class RecursiveExtractor {

    private final SignatureScanner scanner;
    private final Map<SignatureType, ExtractorStrategy> strategies = new HashMap<>();

    public RecursiveExtractor(SignatureScanner scanner) {
        this.scanner = scanner;
        registerDefaultStrategies();
    }

    private void registerDefaultStrategies() {
        strategies.put(SignatureType.UIMAGE, new UImageExtractor());
        strategies.put(SignatureType.SQUASHFS, new SquashFSExtractor());
        strategies.put(SignatureType.GZIP, new GzipExtractor());
        strategies.put(SignatureType.LZMA, new LzmaExtractor());
        strategies.put(SignatureType.JFFS2, new Jffs2Extractor());
        strategies.put(SignatureType.PNG, new PNGExtractor());
        strategies.put(SignatureType.JPG, new JPGExtractor());
        strategies.put(SignatureType.GIF, new GIFExtractor());
        strategies.put(SignatureType.BMP, new BMPExtractor());
        strategies.put(SignatureType.TIFF, new TIFFExtractor());
    }

    public void registerStrategy(SignatureType type, ExtractorStrategy strategy) {
        strategies.put(type, strategy);
    }

    public ExtractionNode extract(byte[] data, File outputDir, int maxDepth) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        ExtractionContext ctx = new ExtractionContext(maxDepth);
        DetectedEntry rootEntry = new DetectedEntry(0, "ROOT_FIRMWARE", data.length, null);

        ExtractionNode root = new ExtractionNode(rootEntry, 0, 0);

        System.out.println("Initiating recursive extraction (max depth = " + maxDepth + ")...\n");

        processNode(data, root, outputDir, ctx);

        System.out.println("\nRecursive extraction success.");
        return root;
    }

    private void processNode(byte[] data,
            ExtractionNode node,
            File outputDir,
            ExtractionContext ctx) {

        if (!ctx.canContinue(node.getDepth())) {
            return;
        }

        List<DetectedEntry> detected = scanner.scan(data);

        for (DetectedEntry entry : detected) {

            int absoluteOffset = node.getAbsoluteOffset() + entry.getOffset();

            if (entry.getOffset() < 0 || entry.getOffset() >= data.length) {
                continue;
            }
            if (entry.getOffset() == 0 && node.getDepth() == 0) {
                continue;
            }
            if (ctx.isVisited(absoluteOffset, entry.getSize())) {
                continue;
            }
            if (shouldSkip(entry, node.getDepth())) {
                continue;
            }

            ExtractionNode child = new ExtractionNode(
                    entry,
                    node.getDepth() + 1,
                    absoluteOffset
            );

            node.addChild(child);

            ExtractorStrategy strategy = strategies.get(entry.getType());

            try {

                // 🔥 snapshot BEFORE
                Set<String> before = snapshot(outputDir);

                boolean success = false;

                if (strategy != null) {
                    success = strategy.extract(data, entry, outputDir);
                } else if (entry.getType() == SignatureType.LZMA) {
                    success = new LzmaRawExtractor().extract(data, entry, outputDir);
                }

                if (!success) {
                    continue;
                }

                ctx.markVisited(absoluteOffset, entry.getSize());

                // 🔥 detectar archivos nuevos
                List<File> newFiles = diff(outputDir, before);

                for (File f : newFiles) {

                    if (!shouldRecurseFile(f)) {
                        continue;
                    }

                    byte[] nextData = Files.readAllBytes(f.toPath());

                    processNode(nextData, child, outputDir, ctx);
                }

            } catch (Exception e) {
                System.err.println("Extraction error "
                        + entry.getType()
                        + " @ 0x" + Integer.toHexString(absoluteOffset));
            }
        }
    }

    /**
     * Filtros inteligentes para reducir ruido y falsos positivos
     */
    private boolean shouldSkip(DetectedEntry entry, int depth) {

        // Permitir siempre imágenes aunque el tamaño sea 0
        if (entry.getType() == SignatureType.PNG
                || entry.getType() == SignatureType.JPG
                || entry.getType() == SignatureType.GIF
                || entry.getType() == SignatureType.BMP
                || entry.getType() == SignatureType.TIFF) {
            return false;
        }
        if (entry.getType() == SignatureType.UIMAGE && depth > 0) {
            return true;
        }
        if (entry.getType() == SignatureType.LZMA
                && entry.getOffset() == 0
                && depth > 1) {
            return true;
        }

        // Saltar entradas sin tamaño útil
        if (entry.getSize() == 0
                && entry.getType() != SignatureType.LZMA
                && entry.getType() != SignatureType.GZIP) {
            return true;
        }

        // Evitar recursión profunda en compresiones (evita explosión)
        if ((entry.getType() == SignatureType.LZMA
                || entry.getType() == SignatureType.GZIP) && depth > 3) {
            return true;
        }

        return false;
    }

    /**
     * Helpers *
     */
    private List<File> diff(File dir, Set<String> before) {
        List<File> result = new ArrayList<>();

        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                if (!before.contains(f.getAbsolutePath())) {
                    result.add(f);
                }
            }
        }

        return result;
    }

    private Set<String> snapshot(File dir) {
        Set<String> files = new HashSet<>();
        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                files.add(f.getAbsolutePath());
            }
        }
        return files;
    }

    private boolean shouldRecurseFile(File f) {

        if (f == null || !f.exists() || !f.isFile()) {
            return false;
        }

        long size = f.length();

        // límites duros
        if (size < 512) {
            return false;
        }
        if (size > 128L * 1024 * 1024) {
            return false;
        }

        String name = f.getName().toLowerCase();

        // obtener extensión segura
        int dot = name.lastIndexOf('.');
        String ext = (dot != -1 && dot < name.length() - 1)
                ? name.substring(dot + 1)
                : "";

        // 🔴 NO recursar formatos terminales (no contienen más estructuras)
        switch (ext) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
            case "tiff":
            case "ico":
                return false;
        }

        // 🔴 evitar texto plano (ruido)
        if (ext.equals("txt") || ext.equals("html") || ext.equals("xml")) {
            return false;
        }

        // 🔴 evitar loops típicos
        if (name.contains("raw") || name.contains("dump")) {
            return false;
        }

        // 🔥 FILTRO DE ENTROPÍA (clave para rendimiento)
        try {
            byte[] sample = readSample(f, 4096);

            double entropy = EntropyAnalyzer.calculateEntropy(sample);

            // muy baja → texto / padding
            if (entropy < 3.5) {
                return false;
            }

            // muy alta → ya comprimido → evitar reintentos inútiles
            if (entropy > 7.9 && !isCompressedCandidate(ext)) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private byte[] readSample(File f, int maxBytes) throws IOException {

        try (FileInputStream fis = new FileInputStream(f)) {

            byte[] buffer = new byte[Math.min(maxBytes, (int) f.length())];

            int read = fis.read(buffer);

            if (read <= 0) {
                return new byte[0];
            }

            if (read < buffer.length) {
                return java.util.Arrays.copyOf(buffer, read);
            }

            return buffer;
        }
    }

    private boolean isCompressedCandidate(String ext) {
        return ext.equals("lzma")
                || ext.equals("gz")
                || ext.equals("xz")
                || ext.equals("bin")
                || ext.equals("img");
    }
}
