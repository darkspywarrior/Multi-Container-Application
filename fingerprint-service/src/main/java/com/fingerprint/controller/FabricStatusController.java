package com.fingerprint.controller;

import com.fingerprint.service.FabricGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fabric")
public class FabricStatusController {

    @Autowired
    private FabricGatewayService fabricGatewayService;

    @GetMapping("/ping")
    public String ping() {
        return "Fabric Gateway Connected";
    }

    @GetMapping("/status")
    public String status() {
        return fabricGatewayService.getBlockchainStatus();
    }

    @GetMapping("/records")
    public String records() {
        return fabricGatewayService.getAllBlockchainRecords();
    }
}