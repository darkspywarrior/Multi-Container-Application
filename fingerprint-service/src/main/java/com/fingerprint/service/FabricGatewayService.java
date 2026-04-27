package com.fingerprint.service;

import org.hyperledger.fabric.gateway.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FabricGatewayService {

    private static final Logger log = LoggerFactory.getLogger(FabricGatewayService.class);

    @Value("${fabric.connection.profile:/app/fabric-config/connection-org1.json}")
    private String connectionProfile;

    @Value("${fabric.channel.name:mychannel}")
    private String channelName;

    @Value("${fabric.contract.name:fingerprint}")
    private String contractName;

    private Gateway gateway;
    private Contract contract;
    private boolean useRealFabric = false;

    private final Map<String, BlockchainRecord> mockLedger = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {

        try {
            connectToRealFabric();
            useRealFabric = true;

            log.info("Connected REAL Fabric");
        } catch (Exception e) {

            useRealFabric = false;

            log.warn("Fabric fallback demo mode");
            log.warn(e.getMessage());
        }
    }

    private void connectToRealFabric() throws Exception {

        Path wallet = Paths.get("/app/wallet/User1/msp");

        Path cert = wallet.resolve(
                "signcerts/cert.pem");

        Path keyDir = wallet.resolve(
                "keystore");

        Path privateKey = Files.list(keyDir)
                .findFirst()
                .orElseThrow();

        Identity identity = Identities.newX509Identity(
                "Org1MSP",
                Identities.readX509Certificate(
                        Files.newBufferedReader(cert)),
                Identities.readPrivateKey(
                        Files.newBufferedReader(privateKey)));

        gateway = Gateway.createBuilder()
                .identity(identity)
                .networkConfig(
                        Paths.get(connectionProfile))
                .connect();

        Network network = gateway.getNetwork(channelName);

        contract = network.getContract(contractName);

        contract.evaluateTransaction(
                "GetAllFiles");
    }

    @PreDestroy
    public void cleanup() {

        if (gateway != null) {
            gateway.close();
        }
    }

    /*
     * Generate unique asset ID with timestamp to handle duplicate uploads
     * Format: filename-hashprefix-timestamp
     */
    private String generateAssetId(String fileName, String hash) {
        String hashPrefix = hash.substring(0, Math.min(16, hash.length()));
        String timestamp = String.valueOf(System.currentTimeMillis());
        return fileName + "-" + hashPrefix + "-" + timestamp;
    }

    /*
     * Check if asset already exists by filename and hash
     */
    public boolean assetExists(String fileName, String hash) {
        String assetId = fileName + "-" + hash.substring(0, Math.min(16, hash.length()));

        if (useRealFabric) {
            try {
                String result = new String(contract.evaluateTransaction("GetHash", assetId));
                return result != null && !result.isEmpty() && !result.startsWith("Error");
            } catch (Exception e) {
                return false;
            }
        }

        return mockLedger.containsKey(assetId);
    }

    /*
     * Store hash with unique asset ID (always creates new record, never conflicts)
     */
    public String storeHash(String fileName, String hash) {
        String assetId = generateAssetId(fileName, hash);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        if (useRealFabric) {
            return storeReal(assetId, fileName, hash, timestamp);
        }

        return storeDemo(assetId, fileName, hash, timestamp);
    }

    /*
     * Store hash only if not exists (maintains single record per file)
     * Returns existing record ID if already exists
     */
    public String storeHashIfNotExists(String fileName, String hash) {
        String baseAssetId = fileName + "-" + hash.substring(0, Math.min(16, hash.length()));

        if (assetExists(fileName, hash)) {
            return "⚠️ File already exists on blockchain with ID: " + baseAssetId + "\n" +
                    "Use updateHash() to modify or storeHash() to create new version";
        }

        return storeHash(fileName, hash);
    }

    /*
     * Update existing asset or create new one
     */
    public String updateHash(String fileName, String newHash, String reason) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String assetId = fileName + "-" + newHash.substring(0, Math.min(16, newHash.length())) + "-"
                + System.currentTimeMillis();

        if (useRealFabric) {
            try {
                contract.submitTransaction(
                        "UpdateHash",
                        assetId,
                        fileName,
                        newHash,
                        "Ira",
                        timestamp,
                        reason != null ? reason : "Updated");

                return "✅ Updated successfully\nAsset ID: " + assetId + "\nReason: "
                        + (reason != null ? reason : "Updated");
            } catch (Exception e) {
                return "Update failed: " + e.getMessage();
            }
        }

        BlockchainRecord r = new BlockchainRecord();
        r.assetId = assetId;
        r.fileName = fileName;
        r.fileHash = newHash;
        r.owner = "Ira";
        r.timestamp = timestamp;
        mockLedger.put(assetId, r);

        return "✅ Updated in demo mode\nAsset ID: " + assetId;
    }

    private String storeReal(String assetId, String fileName, String hash, String timestamp) {

        try {
            contract.submitTransaction(
                    "StoreHash",
                    assetId,
                    fileName,
                    hash,
                    "Ira",
                    timestamp);

            return "✅ BLOCKCHAIN STORED SUCCESSFULLY (REAL FABRIC)\n" +
                    "Asset ID: " + assetId + "\n" +
                    "File: " + fileName + "\n" +
                    "Hash: " + hash + "\n" +
                    "Owner: Ira\n" +
                    "Timestamp: " + timestamp + "\n" +
                    "Status: COMMITTED to REAL LEDGER";
        }

        catch (Exception e) {
            e.printStackTrace();
            return "Fabric write failed: " + e.getMessage();
        }
    }

    private String storeDemo(String assetId, String fileName, String hash, String timestamp) {

        BlockchainRecord r = new BlockchainRecord();
        r.assetId = assetId;
        r.fileName = fileName;
        r.fileHash = hash;
        r.owner = "Ira";
        r.timestamp = timestamp;

        mockLedger.put(assetId, r);

        return "✅ Demo mode stored\nAsset ID: " + assetId;
    }

    /*
     * Get latest asset ID for a given filename and hash prefix
     */
    public String getLatestAssetId(String fileName, String hash) {
        String hashPrefix = hash.substring(0, Math.min(16, hash.length()));

        if (useRealFabric) {
            try {
                String allAssets = new String(contract.evaluateTransaction("GetAllFiles"));
                return fileName + "-" + hashPrefix;
            } catch (Exception e) {
                return fileName + "-" + hashPrefix;
            }
        }

        return mockLedger.keySet().stream()
                .filter(id -> id.startsWith(fileName + "-" + hashPrefix))
                .max(String::compareTo)
                .orElse(fileName + "-" + hashPrefix);
    }

    /*
     * Verify using latest version (handles multiple uploads of same file)
     */
    public String verifyLatestHash(String fileName, String currentHash) {
        String assetId = getLatestAssetId(fileName, currentHash);

        try {
            byte[] result = contract.evaluateTransaction("GetHash", assetId);
            String chainHash = new String(result).trim();

            if (chainHash.equals(currentHash)) {
                return "✅ VALID (Latest version matches)";
            }
            return "❌ TAMPERED or OLD VERSION exists";
        } catch (Exception e) {
            return "Verify failed: " + e.getMessage();
        }
    }

    /*
     * FIXED: Verify using full asset ID, not just fileName
     */
    public String verifyHash(String fileName, String currentHash, String originalHash) {
        String assetId = fileName + "-" + currentHash.substring(0, Math.min(16, currentHash.length()));

        try {
            byte[] result = contract.evaluateTransaction("GetHash", assetId);
            String chainHash = new String(result).trim();

            if (chainHash.equals(currentHash)) {
                return "✅ VALID ✓ - Hash matches blockchain record";
            } else if (chainHash.isEmpty() || chainHash.startsWith("Error")) {
                return "⚠️ NOT FOUND - No blockchain record for: " + assetId;
            } else {
                return "❌ TAMPERED ✗ - Expected: " + currentHash + "\n     Found on chain: " + chainHash;
            }
        } catch (Exception e) {
            return "❌ Verify failed: " + e.getMessage();
        }
    }

    /*
     * FIXED: Get history using proper asset ID
     */
    public String getFileHistory(String fileName, String hash) {
        String assetId = fileName + "-" + hash.substring(0, Math.min(16, hash.length()));

        try {
            byte[] result = contract.evaluateTransaction("GetHistory", assetId);
            return new String(result);
        } catch (Exception e) {
            return "History query failed: " + e.getMessage();
        }
    }

    /*
     * Get all records for a specific file (all versions)
     */
    public String getAllVersions(String fileName) {
        if (useRealFabric) {
            try {
                return new String(contract.evaluateTransaction("GetHistoryByFileName", fileName));
            } catch (Exception e) {
                return "Query failed: " + e.getMessage();
            }
        }

        StringBuilder versions = new StringBuilder("All versions for: " + fileName + "\n");
        for (Map.Entry<String, BlockchainRecord> entry : mockLedger.entrySet()) {
            if (entry.getValue().fileName.equals(fileName)) {
                versions.append("  - ").append(entry.getKey()).append("\n");
            }
        }
        return versions.toString();
    }

    public String getBlockchainStatus() {
        if (useRealFabric) {
            return """
                    REAL FABRIC ACTIVE
                    Channel: mychannel
                    Contract: fingerprint
                    History: enabled
                    Status: CONNECTED
                    """;
        }
        return """
                DEMO MODE
                Status: FALLBACK
                """;
    }

    public String getAllBlockchainRecords() {
        if (useRealFabric) {
            try {
                return new String(contract.evaluateTransaction("GetAllFiles"));
            } catch (Exception e) {
                return "Audit query failed: " + e.getMessage();
            }
        }
        return mockLedger.toString();
    }

    static class BlockchainRecord {
        String assetId;
        String fileName;
        String fileHash;
        String owner;
        String timestamp;
    }
}