package com.fingerprint.controller;

import java.security.MessageDigest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fingerprint")
public class FingerprintController {

    private String generateHash(String input, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes());

        StringBuilder sb = new StringBuilder();

        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    @GetMapping("/SHA-256/{input}")
    public String sha256(@PathVariable String input) throws Exception {
        return generateHash(input,"SHA-256");
    }

    @GetMapping("/SHA-512/{input}")
    public String sha512(@PathVariable String input) throws Exception {
        return generateHash(input,"SHA-512");
    }

    @GetMapping("/SHA-1/{input}")
    public String sha1(@PathVariable String input) throws Exception {
        return generateHash(input,"SHA-1");
    }

    @GetMapping("/MD5/{input}")
    public String md5(@PathVariable String input) throws Exception {
        return generateHash(input,"MD5");
    }

}