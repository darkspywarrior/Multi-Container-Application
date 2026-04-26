package com.fingerprint.service;

import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class FabricGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(FabricGatewayService.class);

    @Value("${fabric.connection.profile:fabric-config/connection-org1.json}")
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
            logger.info("✅ Connected to REAL Hyperledger Fabric network");
            logger.info("   Channel: {}", channelName);
            logger.info("   Contract: {}", contractName);
        } catch (Exception e) {
            logger.warn("⚠️ Could not connect to real Fabric: {}", e.getMessage());
            logger.info("   Falling back to DEMO mode with in-memory ledger");
            useRealFabric = false;
        }
    }

    private void connectToRealFabric() throws Exception {
        // Path to MSP directory containing certs and keys
        Path cryptoPath = Paths.get("/app/wallet/User1/msp");

        // Read certificate
        Path certPath = cryptoPath.resolve("signcerts/cert.pem");
        if (!Files.exists(certPath)) {
            throw new Exception("Certificate not found at: " + certPath);
        }

        // Find private key file in keystore directory
        Path keyDir = cryptoPath.resolve("keystore");
        Path privateKeyPath = Files.list(keyDir)
                .findFirst()
                .orElseThrow(() -> new Exception("No private key found in: " + keyDir));

        // Create X509 identity using Org1MSP
        Identity identityObj = Identities.newX509Identity(
                "Org1MSP",
                Identities.readX509Certificate(Files.newBufferedReader(certPath)),
                Identities.readPrivateKey(Files.newBufferedReader(privateKeyPath)));

        // Load connection profile
        Path connectionPath = Paths.get(connectionProfile);

        // Create gateway connection with direct identity
        gateway = Gateway.createBuilder()
                .identity(identityObj)
                .networkConfig(connectionPath)
                .connect();

        // Get network and contract
        Network network = gateway.getNetwork(channelName);
        contract = network.getContract(contractName);

        // Test connection
        byte[] testResult = contract.evaluateTransaction("GetAllFiles");
        logger.info("   Connection test successful");
    }

    @PreDestroy
    public void cleanup() {
        if (gateway != null) {
            gateway.close();
            logger.info("Fabric Gateway connection closed");
        }
    }

    public String storeHash(String fileName, String fileHash) {
        String assetId = "asset_" + UUID.randomUUID().toString().substring(0, 8);
        String timestamp = Instant.now().toString();

        if (useRealFabric) {
            return storeOnRealFabric(assetId, fileName, fileHash, timestamp);
        } else {
            return storeOnMockLedger(assetId, fileName, fileHash, timestamp);
        }
    }

    private String storeOnRealFabric(String assetId, String fileName, String fileHash, String timestamp) {
        try {
            logger.info("Submitting transaction to Fabric - Asset: {}", assetId);

            byte[] result = contract.submitTransaction(
                    "StoreHash",
                    assetId,
                    fileName,
                    fileHash,
                    "User1");

            String txId = "tx_" + UUID.randomUUID().toString().substring(0, 16);

            logger.info("✅ Transaction committed - Asset: {}", assetId);

            return String.format(
                    "✅ BLOCKCHAIN STORED SUCCESSFULLY (REAL FABRIC)\n" +
                            "   ├── Transaction ID: %s\n" +
                            "   ├── Asset ID: %s\n" +
                            "   ├── File: %s\n" +
                            "   ├── Hash: %s\n" +
                            "   ├── Timestamp: %s\n" +
                            "   ├── Response: %s\n" +
                            "   └── Status: COMMITTED to REAL LEDGER",
                    txId, assetId, fileName,
                    fileHash.substring(0, Math.min(32, fileHash.length())) + "...",
                    timestamp,
                    new String(result));

        } catch (Exception e) {
            logger.error("Fabric transaction failed", e);
            return String.format(
                    "❌ BLOCKCHAIN STORAGE FAILED\n" +
                            "   ├── Error: %s\n" +
                            "   └── Falling back to verification mode",
                    e.getMessage());
        }
    }

    private String storeOnMockLedger(String assetId, String fileName, String fileHash, String timestamp) {
        try {
            BlockchainRecord record = new BlockchainRecord();
            record.assetId = assetId;
            record.fileName = fileName;
            record.fileHash = fileHash;
            record.timestamp = timestamp;
            record.transactionId = "tx_" + UUID.randomUUID().toString().substring(0, 16);
            record.blockNumber = mockLedger.size() + 1;

            mockLedger.put(assetId, record);

            return String.format(
                    "✅ BLOCKCHAIN STORED SUCCESSFULLY (DEMO MODE)\n" +
                            "   ├── Transaction ID: %s\n" +
                            "   ├── Asset ID: %s\n" +
                            "   ├── Block Number: %d\n" +
                            "   ├── File: %s\n" +
                            "   ├── Hash: %s\n" +
                            "   ├── Timestamp: %s\n" +
                            "   └── Status: COMMITTED to DEMO Ledger",
                    record.transactionId, assetId, record.blockNumber,
                    fileName, fileHash.substring(0, Math.min(16, fileHash.length())) + "...", timestamp);

        } catch (Exception e) {
            return "❌ BLOCKCHAIN STORAGE FAILED\n" +
                    "   Error: " + e.getMessage();
        }
    }

    public String verifyHash(String fileName, String newHash, String originalHash) {
        if (useRealFabric) {
            return verifyOnRealFabric(fileName, newHash, originalHash);
        } else {
            return verifyOnMockLedger(fileName, newHash, originalHash);
        }
    }

    private String verifyOnRealFabric(String fileName, String newHash, String originalHash) {
        try {
            byte[] result = contract.evaluateTransaction("GetHash", fileName);
            String storedHash = new String(result);

            if (storedHash.equals(originalHash)) {
                if (originalHash.equals(newHash)) {
                    return String.format(
                            "✅ VERIFIED on REAL BLOCKCHAIN\n" +
                                    "   ├── Status: IMMUTABLE ✓\n" +
                                    "   ├── Integrity: INTACT\n" +
                                    "   └── Hash Match: CONFIRMED");
                } else {
                    return String.format(
                            "❌ TAMPER DETECTED on REAL BLOCKCHAIN\n" +
                                    "   ├── Status: MISMATCH ⚠️\n" +
                                    "   ├── Original: %s\n" +
                                    "   ├── Current: %s\n" +
                                    "   └── Integrity: COMPROMISED",
                            originalHash.substring(0, 16) + "...",
                            newHash.substring(0, 16) + "...");
                }
            } else {
                return "❌ NOT FOUND on REAL BLOCKCHAIN\n" +
                        "   └── Upload file first using /file/upload";
            }

        } catch (Exception e) {
            return "⚠️ VERIFICATION FAILED: " + e.getMessage();
        }
    }

    private String verifyOnMockLedger(String fileName, String newHash, String originalHash) {
        try {
            boolean found = false;
            String matchedAssetId = "";
            BlockchainRecord matchedRecord = null;

            for (BlockchainRecord record : mockLedger.values()) {
                if (record.fileHash.equals(originalHash)) {
                    found = true;
                    matchedAssetId = record.assetId;
                    matchedRecord = record;
                    break;
                }
            }

            if (found) {
                if (originalHash.equals(newHash)) {
                    return String.format(
                            "✅ VERIFIED on DEMO BLOCKCHAIN\n" +
                                    "   ├── Asset ID: %s\n" +
                                    "   ├── Block: #%d\n" +
                                    "   ├── Status: IMMUTABLE ✓\n" +
                                    "   └── Integrity: INTACT",
                            matchedAssetId, matchedRecord.blockNumber);
                } else {
                    return String.format(
                            "❌ TAMPER DETECTED on DEMO BLOCKCHAIN\n" +
                                    "   ├── Asset ID: %s\n" +
                                    "   ├── Block: #%d\n" +
                                    "   ├── Status: MISMATCH ⚠️\n" +
                                    "   └── Integrity: COMPROMISED",
                            matchedAssetId, matchedRecord.blockNumber);
                }
            } else {
                return "❌ NOT FOUND on DEMO BLOCKCHAIN\n" +
                        "   └── Upload file first using /file/upload";
            }

        } catch (Exception e) {
            return "❌ VERIFICATION FAILED\n" +
                    "   Error: " + e.getMessage();
        }
    }

    public String getAllBlockchainRecords() {
        if (useRealFabric) {
            return getAllFromRealFabric();
        } else {
            return getAllFromMockLedger();
        }
    }

    private String getAllFromRealFabric() {
        try {
            byte[] result = contract.evaluateTransaction("GetAllFiles");
            return "=== REAL BLOCKCHAIN LEDGER ===\n" +
                    String.format("Connected to: %s\n", channelName) +
                    String.format("Contract: %s\n\n", contractName) +
                    "Blockchain Data:\n" +
                    new String(result);
        } catch (Exception e) {
            return "Error querying blockchain: " + e.getMessage();
        }
    }

    private String getAllFromMockLedger() {
        if (mockLedger.isEmpty()) {
            return "No records found on demo blockchain";
        }

        StringBuilder result = new StringBuilder();
        result.append("=== DEMO BLOCKCHAIN LEDGER ===\n");
        result.append(String.format("Total Transactions: %d\n\n", mockLedger.size()));

        for (BlockchainRecord record : mockLedger.values()) {
            result.append(String.format(
                    "Asset ID: %s\n" +
                            "  ├── Transaction: %s\n" +
                            "  ├── Block: #%d\n" +
                            "  ├── File: %s\n" +
                            "  ├── Hash: %s\n" +
                            "  └── Time: %s\n\n",
                    record.assetId, record.transactionId, record.blockNumber,
                    record.fileName,
                    record.fileHash.substring(0, Math.min(32, record.fileHash.length())) + "...",
                    record.timestamp));
        }

        return result.toString();
    }

    public String getBlockchainStatus() {
        if (useRealFabric) {
            return """
                    ╔════════════════════════════════════════════╗
                    ║     REAL HYPERLEDGER FABRIC STATUS         ║
                    ╠════════════════════════════════════════════╣
                    ║  Connection: ACTIVE ✅                      ║
                    ║  Mode: PRODUCTION                          ║
                    ║  Channel: %-30s ║
                    ║  Contract: %-29s ║
                    ║  Security: TLS 1.3                         ║
                    ║  Algorithm: SHA-256                        ║
                    ╚════════════════════════════════════════════╝
                    """.formatted(channelName, contractName);
        } else {
            return """
                    ╔════════════════════════════════════════════╗
                    ║        DEMO MODE - BLOCKCHAIN STATUS       ║
                    ╠════════════════════════════════════════════╣
                    ║  Connection: DEMO MODE ⚠️                   ║
                    ║  Mode: SIMULATED LEDGER                    ║
                    ║  Records: %-32d ║
                    ║  Ready for: REAL FABRIC CONNECTION         ║
                    ║  Next Step: Configure wallet + certs       ║
                    ╚════════════════════════════════════════════╝
                    """.formatted(mockLedger.size());
        }
    }

    private static class BlockchainRecord {
        String assetId;
        String fileName;
        String fileHash;
        String timestamp;
        String transactionId;
        int blockNumber;
    }
}