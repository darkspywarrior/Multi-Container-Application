package com.fingerprint.controller;

import com.fingerprint.service.FabricGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private FabricGatewayService fabricGatewayService;

    @GetMapping("/trail")
    public String auditTrail() {
        return fabricGatewayService.getAllBlockchainRecords();
    }

    @GetMapping("/status")
    public String blockchainStatus() {
        return fabricGatewayService.getBlockchainStatus();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {

        return Map.of(
                "status", "ACTIVE",
                "network", "Hyperledger Fabric",
                "channel", "mychannel",
                "chaincode", "fingerprint",
                "version", "2.0",
                "organizations", 2,
                "orderers", 1,
                "security", "TLS 1.3",
                "hashAlgorithm", "SHA-256");
    }
}