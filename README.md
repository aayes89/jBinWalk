# jBinWalk (Firmware Recursive Extractor)

jBinWalk is a Java-based recursive firmware analysis and extraction engine inspired by tools like **binwalk**.  
It is designed to detect, extract, and recursively analyze embedded structures inside firmware images.

---

## 🚀 Features

- Multi-signature detection engine (magic byte scanning)
- Recursive extraction (Matryoshka-style unpacking)
- Support for common firmware components:
  - U-Boot uImage
  - SquashFS
  - LZMA / LZMA raw
  - GZIP / ZIP
  - JFFS2
- Image extraction support:
  - PNG
  - JPG / JPEG
  - GIF
  - BMP
  - TIFF
- Entropy-based filtering to reduce noise
- Visit tracking system to avoid recursion loops
- Disk-based extraction pipeline (no full in-memory recursion)
- Heuristic validation per format
- Extensible strategy pattern architecture

---

## 🧠 Architecture

The project is structured around modular components:
* SignatureScanner → detects magic bytes in firmware
* ExtractorStrategy → format-specific extraction logic
* RecursiveExtractor → recursive analysis engine
* ExtractionContext → loop prevention + depth control
* EntropyAnalyzer → noise filtering
* DetectedEntry → detected signature metadata
* ExtractionNode → recursive tree structure


---

## 🔄 Workflow

1. Load firmware as byte array
2. Scan for known signatures
3. Validate each detection
4. Extract content using strategy pattern
5. Save extracted files to disk
6. Re-scan extracted files recursively
7. Apply entropy + size filtering to avoid noise explosion

---

## 📦 Example Output
```
Firmware loaded: 8388608 bytes

=== FIRMWARE DATA ===

Offset       Description                                   Size       Type
==========================================================================================
0x0000A2BC  LZMA compressed data                                 0 bytes  LZMA
0x00016999  PNG file                                           425 bytes  PNG
0x00050000  uImage header (U-Boot)                         5389996 bytes  UIMAGE

Initiating Recursive extraction (depth=5)...
Initiating recursive extraction (max depth = 5)...

? PNG extracted: 0x00016999_image.png  (425 bytes)
OK extract: UIMAGE size=5389932 (comp=lzma)

Recursive extraction success.

=== SUCCESS ===
[0] 0x00000000  |  ROOT_FIRMWARE                             |     8388608 bytes  |  null
  [1] 0x0000A2BC  |  LZMA compressed data                      |           0 bytes  |  LZMA  
  [1] 0x00016999  |  PNG file                                  |         425 bytes  |  PNG  
  [1] 0x00050000  |  uImage header (U-Boot)                    |     5389996 bytes  |  UIMAGE  

Files saves on: C:\jBinWalk\extracted
```
## Entropy filter thresholds
* Low entropy (< 3.5) → ignored (padding/text)
* High entropy (> 7.9) → likely compressed/encrypted

## 🧪 Design Patterns Used
Strategy Pattern → Extractor implementations
Tree Structure → recursive extraction graph
Scanner Engine → signature detection layer

## ⚠️ Notes
LZMA raw detection uses heuristic parameter guessing
Some false positives are expected in firmware blobs
Extraction is intentionally conservative to avoid OOM and infinite recursion loops

## 🛠 Future Improvements
Confidence scoring system for detections
Parallel extraction pipeline
YARA-based signature integration
GUI visualization of extraction tree
Incremental streaming scanner (memory optimization)

## 🧩 License

MIT License
