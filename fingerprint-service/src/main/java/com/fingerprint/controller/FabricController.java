package com.fingerprint.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fabric")
public class FabricController {

@GetMapping("/ping")
public String ping() {
return "Fabric Gateway Connected";
}

@GetMapping("/health")
public String health() {
return "OK";
}

}
