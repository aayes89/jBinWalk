package jbinwalk;

import extractor.*;
import model.DetectedEntry;
import model.ExtractionNode;
import model.SignatureType;
import scanner.SignatureScanner;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
//import javax.swing.JFileChooser;
import utils.TreePrinter;

public class JBinWalk {

    public static void main(String[] args) throws Exception {
        // Solucionar caracteres raros en consola
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("A binwalk Java version.\nMade by Slam 2026\n");
        /*
        JFileChooser jfc = new JFileChooser();
        jfc.showOpenDialog(null);
        String binPath = jfc.getSelectedFile().getAbsolutePath();
         */
        String firmwarePath = "D:\\APBios\\modded.bin";

        byte[] data = Files.readAllBytes(Paths.get(firmwarePath));
        System.out.println("Firmware loaded: " + data.length + " bytes\n");

        SignatureScanner scanner = new SignatureScanner();
        List<DetectedEntry> results = scanner.scan(data);

        System.out.println("=== FIRMWARE DATA ===\n"); // AC1200-U11
        System.out.printf("%-12s %-45s %-10s %s%n", "Offset", "Description", "Size", "Type");
        System.out.println("=".repeat(90));

        for (DetectedEntry entry : results) {
            System.out.printf("0x%08X  %-45s %8d bytes  %s%n",
                    entry.getOffset(),
                    entry.getDescription(),
                    entry.getSize(),
                    entry.getType());
        }

        // ==================== EXTRACTOR ====================
        RecursiveExtractor extractor = new RecursiveExtractor(scanner);

        File outputDir = new File("extracted");
        outputDir.mkdirs();

        System.out.println("\nInitiating Recursive extraction (depth=5)...");
        ExtractionNode root = extractor.extract(data, outputDir, 5);

        System.out.println("\n=== SUCCESS ===");
        TreePrinter.print(root);
        System.out.println("\nFiles saves on: " + outputDir.getAbsolutePath());
    }
}
