package com.fingerprint.controller;

import java.security.MessageDigest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fingerprint")
public class FingerprintController {

@GetMapping("/SHA-256/{input}")
public String hash(@PathVariable String input) throws Exception{
 MessageDigest md = MessageDigest.getInstance("SHA-256");
 byte[] digest = md.digest(input.getBytes());
 StringBuilder sb=new StringBuilder();
 for(byte b:digest){
   sb.append(String.format("%02x",b));
 }
 return sb.toString();
}
}
