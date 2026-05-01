package com.fingerprint.controller;

import com.fingerprint.service.FingerprintService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fingerprint")
public class FingerprintController {

    private final FingerprintService fingerprintService;

    public FingerprintController(FingerprintService fingerprintService) {
        this.fingerprintService = fingerprintService;
    }

    @GetMapping("/{algorithm}/{data}")
    public Map<String, String> hash(
            @PathVariable String algorithm,
            @PathVariable String data) throws Exception {

        String hash = fingerprintService.generateHash(algorithm, data);

        Map<String, String> res = new HashMap<>();
        res.put("algorithm", algorithm);
        res.put("data", data);
        res.put("hash", hash);

        return res;
    }
}