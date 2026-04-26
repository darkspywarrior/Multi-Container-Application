package com.fingerprint.controller;

import com.fingerprint.service.FabricGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private FabricGatewayService fabricGatewayService;

    @GetMapping("/trail")
    public String auditTrail() {
        return fabricGatewayService.getAllBlockchainRecords();
    }
    
    @GetMapping("/stats")
    public String stats() {
        return """
            ╔══════════════════════════════════════╗
            ║   BLOCKCHAIN INTEGRITY STATISTICS    ║
            ╠══════════════════════════════════════╣
            ║  Status: ACTIVE                      ║
            ║  Network: Hyperledger Fabric         ║
            ║  Channel: mychannel                  ║
            ║  Chaincode: fingerprint              ║
            ║  Version: 2.0                        ║
            ║  Peers: 2 (Org1, Org2)               ║
            ║  Orderer: 1 Active                   ║
            ║  Security: TLS 1.3                   ║
            ║  Algorithm: SHA-256                  ║
            ╚══════════════════════════════════════╝
            """;
    }
}
