package com.fingerprint.controller;

import com.fingerprint.service.FingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fingerprint")
public class FingerprintController {

@Autowired
private FingerprintService fingerprintService;

@GetMapping("/{algorithm}/{data}")
public String hash(
@PathVariable String algorithm,
@PathVariable String data
){
try{
return fingerprintService.generateHash(
algorithm.toUpperCase(),
data
);
}
catch(Exception e){
return "Use SHA-256 SHA-512 SHA-1 MD5";
}
}
}
