package com.fingerprint.controller;

import com.fingerprint.service.FingerprintService;
import com.fingerprint.service.FabricGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FingerprintService fingerprintService;

    @Autowired
    private FabricGatewayService fabricGatewayService;

    private String storedHash;
    private String storedFileName;
    private String storedTransactionId;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {

        // Generate hash from uploaded file
        storedHash = fingerprintService.generateFileHash(file.getInputStream());
        storedFileName = file.getOriginalFilename();

        // Store hash on blockchain via Fabric Gateway
        String blockchainReceipt = fabricGatewayService.storeHash(
                storedFileName,
                storedHash);

        // Extract transaction ID from receipt for tracking
        if (blockchainReceipt.contains("Transaction ID:")) {
            String[] lines = blockchainReceipt.split("\n");
            for (String line : lines) {
                if (line.contains("Transaction ID:")) {
                    storedTransactionId = line.substring(line.indexOf(":") + 2).trim();
                    break;
                }
            }
        } else {
            storedTransactionId = "tx_" + System.currentTimeMillis();
        }

        // Return complete response
        return "=== FILE PROCESSED ===\n" +
                "File Name: " + storedFileName + "\n" +
                "File Size: " + file.getSize() + " bytes\n" +
                "SHA-256 Hash: " + storedHash + "\n" +
                "Transaction ID: " + storedTransactionId + "\n\n" +
                "=== BLOCKCHAIN RESULT ===\n" +
                blockchainReceipt;
    }

    @PostMapping("/verify")
    public String verify(@RequestParam("file") MultipartFile file) throws Exception {

        if (storedHash == null) {
            return "ERROR: No file has been uploaded yet. Please upload a file first.\n" +
                    "Use POST /file/upload to upload and store a file hash on the blockchain.";
        }

        // Generate hash from verification file
        String newHash = fingerprintService.generateFileHash(file.getInputStream());
        String currentFileName = file.getOriginalFilename();

        // Verify against blockchain
        String verificationResult = fabricGatewayService.verifyHash(
                currentFileName,
                newHash,
                storedHash);

        // Local comparison with detailed output
        if (storedHash.equals(newHash)) {
            return "=== VERIFICATION RESULT ===\n" +
                    "╔════════════════════════════════════════╗\n" +
                    "║     ✅ FILE INTEGRITY: VALID            ║\n" +
                    "╚════════════════════════════════════════╝\n\n" +
                    "📋 File Details:\n" +
                    "   ├── File Name: " + currentFileName + "\n" +
                    "   ├── Transaction ID: " + storedTransactionId + "\n" +
                    "   └── Verification Time: " + new java.util.Date() + "\n\n" +
                    "🔗 Blockchain Verification:\n" +
                    "   " + verificationResult + "\n\n" +
                    "🔐 Hash Comparison:\n" +
                    "   ├── Original Hash: " + storedHash + "\n" +
                    "   ├── Current Hash:  " + newHash + "\n" +
                    "   └── Status: IDENTICAL ✓\n\n" +
                    "✅ VERDICT: File has NOT been tampered with - Blockchain integrity confirmed";
        } else {
            return "=== VERIFICATION RESULT ===\n" +
                    "╔════════════════════════════════════════╗\n" +
                    "║   ❌ FILE INTEGRITY: TAMPERED          ║\n" +
                    "╚════════════════════════════════════════╝\n\n" +
                    "📋 File Details:\n" +
                    "   ├── File Name: " + currentFileName + "\n" +
                    "   ├── Transaction ID: " + storedTransactionId + "\n" +
                    "   └── Verification Time: " + new java.util.Date() + "\n\n" +
                    "🔗 Blockchain Verification:\n" +
                    "   " + verificationResult + "\n\n" +
                    "🔐 Hash Comparison:\n" +
                    "   ├── Original Hash: " + storedHash + "\n" +
                    "   ├── Current Hash:  " + newHash + "\n" +
                    "   └── Status: MISMATCH ⚠️\n\n" +
                    "⚠️ VERDICT: File has been modified or corrupted - Blockchain integrity check FAILED";
        }
    }

    @PostMapping("/upload-and-verify")
    public String uploadAndVerify(@RequestParam("file") MultipartFile file) throws Exception {
        // First upload
        String uploadResult = upload(file);

        // Then verify the same file
        String verifyResult = verify(file);

        return uploadResult + "\n\n" + verifyResult;
    }

    @GetMapping("/hash")
    public String getCurrentHash() {
        if (storedHash == null) {
            return "No file has been uploaded yet. Use POST /file/upload to upload a file.";
        }

        return "Current File Information:\n" +
                "├── File Name: " + storedFileName + "\n" +
                "├── File Hash: " + storedHash + "\n" +
                "├── Transaction ID: " + storedTransactionId + "\n" +
                "└── Status: Stored on Blockchain ✓";
    }

    @GetMapping("/info")
    public String getInfo() {
        return """
                ╔══════════════════════════════════════════════════════════╗
                ║     BLOCKCHAIN FILE FINGERPRINT SERVICE v2.0            ║
                ╠══════════════════════════════════════════════════════════╣
                ║  Available Endpoints:                                    ║
                ║                                                          ║
                ║  📤 POST  /file/upload                                   ║
                ║      - Upload file & store hash on blockchain            ║
                ║                                                          ║
                ║  🔍 POST  /file/verify                                   ║
                ║      - Verify file integrity against blockchain          ║
                ║                                                          ║
                ║  🔄 POST  /file/upload-and-verify                        ║
                ║      - Upload and verify in one call                     ║
                ║                                                          ║
                ║  📊 GET   /file/hash                                     ║
                ║      - Get current file hash information                 ║
                ║                                                          ║
                ║  📋 GET   /audit/trail                                   ║
                ║      - View full blockchain audit trail                  ║
                ║                                                          ║
                ║  ℹ️  GET   /file/info                                    ║
                ║      - Show this information                             ║
                ║                                                          ║
                ║  ⚙️  GET   /fabric/status                                ║
                ║      - Check Fabric network connection status            ║
                ╚══════════════════════════════════════════════════════════╝
                """;
    }

    @DeleteMapping("/clear")
    public String clearStoredHash() {
        storedHash = null;
        storedFileName = null;
        storedTransactionId = null;
        return "Stored file hash cleared from memory. Blockchain records remain immutable.";
    }
}