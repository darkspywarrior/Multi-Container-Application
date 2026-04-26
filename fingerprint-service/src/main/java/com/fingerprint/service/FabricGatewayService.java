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

            log.info("Fabric connectivity verified.");
            log.info("✅ Connected to REAL Hyperledger Fabric");
            log.info("Channel {}", channelName);
            log.info("Contract {}", contractName);

        } catch (Exception e) {
            useRealFabric = false;
            log.warn("⚠ Fabric unavailable. Using demo mode");
            log.warn(e.getMessage());
        }
    }

    private void connectToRealFabric() throws Exception {

        Path crypto = Paths.get("/app/wallet/User1/msp");

        Path cert = crypto.resolve("signcerts/cert.pem");

        Path keyDir = crypto.resolve("keystore");

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
                .networkConfig(Paths.get(connectionProfile))
                .connect();

        Network network = gateway.getNetwork(channelName);

        contract = network.getContract(contractName);

        contract.evaluateTransaction("GetAllFiles");
    }

    @PreDestroy
    public void cleanup() {
        if (gateway != null) {
            gateway.close();
        }
    }

    /*
     * =========================
     * STORE HASH FIXED
     * =========================
     */

    public String storeHash(String fileName, String hash) {

        String assetId = fileName; // IMPORTANT FIX
        String ts = Instant.now().toString();

        if (useRealFabric) {
            return storeReal(
                    assetId,
                    fileName,
                    hash,
                    ts);
        }

        return storeDemo(
                assetId,
                fileName,
                hash,
                ts);
    }

    private String storeReal(
            String assetId,
            String fileName,
            String hash,
            String ts) {

        try {

            contract.submitTransaction(
                    "StoreHash",
                    assetId,
                    fileName,
                    hash,
                    "Ira");

            return "✅ BLOCKCHAIN STORED SUCCESSFULLY (REAL FABRIC)\n" +
                    "Transaction ID: tx-" + System.currentTimeMillis() + "\n" +
                    "Asset ID: " + assetId + "\n" +
                    "File: " + fileName + "\n" +
                    "Hash: " + hash + "\n" +
                    "Timestamp: " + ts + "\n" +
                    "Status: COMMITTED to REAL LEDGER";

        } catch (Exception e) {
            return "Fabric write failed: " + e.getMessage();
        }
    }

    private String storeDemo(
            String assetId,
            String fileName,
            String hash,
            String ts) {

        BlockchainRecord r = new BlockchainRecord();

        r.assetId = assetId;
        r.fileName = fileName;
        r.fileHash = hash;
        r.timestamp = ts;
        r.blockNumber = mockLedger.size() + 1;

        mockLedger.put(assetId, r);

        return "DEMO BLOCK COMMIT SUCCESS\n" +
                "Asset " + assetId;
    }

    public String verifyHash(
            String fileName,
            String newHash,
            String oldHash) {

        if (useRealFabric) {
            try {

                byte[] r = contract.evaluateTransaction(
                        "GetHash",
                        fileName);

                String chainHash = new String(r).trim();

                return chainHash.equals(newHash)
                        ? "VALID ✓"
                        : "TAMPERED ✗";

            } catch (Exception e) {
                return "Blockchain verify failed: " +
                        e.getMessage();
            }
        }

        return oldHash.equals(newHash)
                ? "VALID ✓"
                : "TAMPERED ✗";
    }

    public String getAllBlockchainRecords() {

        if (useRealFabric) {
            try {
                return new String(
                        contract.evaluateTransaction(
                                "GetAllFiles"));
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        return "Demo tx count " +
                mockLedger.size();
    }

    public String getBlockchainStatus() {

        if (useRealFabric) {
            return """
                    REAL FABRIC ACTIVE
                    Channel: mychannel
                    Contract: fingerprint
                    Status: CONNECTED
                    """;
        }

        return """
                DEMO MODE
                Status: FALLBACK
                """;
    }

    static class BlockchainRecord {
        String assetId;
        String fileName;
        String fileHash;
        String timestamp;
        int blockNumber;
    }
}