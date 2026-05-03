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
import model.DetectedEntry;
import model.SignatureType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Extractor {

    private final Map<SignatureType, ExtractorStrategy> strategies = new HashMap<>();

    public Extractor() {
        // Registrar estrategias
        strategies.put(SignatureType.UIMAGE, new UImageExtractor());
        strategies.put(SignatureType.SQUASHFS, new SquashFSExtractor());
        strategies.put(SignatureType.GZIP, new GzipExtractor());
        strategies.put(SignatureType.ZIP, new GzipExtractor());
        strategies.put(SignatureType.LZMA, new LzmaExtractor());
        strategies.put(SignatureType.JFFS2, new Jffs2Extractor());
        strategies.put(SignatureType.BMP, new BMPExtractor());
        strategies.put(SignatureType.PNG, new PNGExtractor());
        strategies.put(SignatureType.JPEG, new JPGExtractor());
        strategies.put(SignatureType.JPG, new JPGExtractor());
        strategies.put(SignatureType.GIF, new GIFExtractor());
        strategies.put(SignatureType.TIFF, new TIFFExtractor());
        // añadir más luego
    }

    public void extractAll(byte[] firmwareData, List<DetectedEntry> entries, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        System.out.println("Iniciando extracción de " + entries.size() + " elementos...\n");

        int success = 0;
        for (DetectedEntry entry : entries) {
            ExtractorStrategy strategy = strategies.get(entry.getType());

            if (strategy != null) {
                try {
                    boolean ok = strategy.extract(firmwareData, entry, outputDir);
                    if (ok) {
                        success++;
                    }
                } catch (Exception e) {
                    System.err.println("Error extrayendo " + entry.getDescription()
                            + " @ 0x" + Integer.toHexString(entry.getOffset())
                            + " -> " + e.getMessage());
                }
            } else {
                // Fallback: carving simple
                extractRaw(firmwareData, entry, outputDir);
                success++;
            }
        }

        System.out.println("\nExtracción finalizada. " + success + " elementos procesados.");
    }

    // Carving simple cuando no hay estrategia específica
    private void extractRaw(byte[] data, DetectedEntry entry, File outputDir) {
        try {
            int offset = entry.getOffset();
            int size = entry.getSize() > 0
                    ? Math.min(entry.getSize(), data.length - offset)
                    : Math.min(1024 * 1024, data.length - offset); // max 1MB por defecto

            if (size <= 0) {
                return;
            }

            byte[] content = new byte[size];
            System.arraycopy(data, offset, content, 0, size);

            String fileName = String.format("0x%08X_%s.bin", offset,
                    entry.getType().name().toLowerCase());

            Path outPath = outputDir.toPath().resolve(fileName);
            Files.write(outPath, content);

            System.out.println("✓ Extraído (raw): " + fileName + " (" + size + " bytes)");
        } catch (IOException e) {
            System.err.println("Error en carving raw: " + e.getMessage());
        }
    }
}