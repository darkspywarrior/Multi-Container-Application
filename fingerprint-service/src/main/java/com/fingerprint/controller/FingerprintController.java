package com.fingerprint.controller;

import com.fingerprint.service.FingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/fingerprint")
public class FingerprintController {

    @Autowired
    private FingerprintService fingerprintService;

    private static final Set<String> SUPPORTED = Set.of("SHA-256", "SHA-512", "SHA-1", "MD5");

    // Example:
    // POST /fingerprint/hash
    // {
    // "algorithm":"SHA-256",
    // "data":"hello"
    // }

    @PostMapping("/hash")
    public ResponseEntity<?> hash(@RequestBody HashRequest request) {

        try {

            String algorithm = request.getAlgorithm().toUpperCase();

            if (!SUPPORTED.contains(algorithm)) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "error", "Unsupported algorithm",
                                "supported", SUPPORTED));
            }

            String hash = fingerprintService.generateHash(
                    algorithm,
                    request.getData());

            return ResponseEntity.ok(
                    Map.of(
                            "algorithm", algorithm,
                            "data", request.getData(),
                            "hash", hash,
                            "status", "success"));

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "error", e.getMessage()));
        }
    }

    public static class HashRequest {

        private String algorithm;
        private String data;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}