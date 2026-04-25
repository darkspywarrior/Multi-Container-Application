package com.fingerprint.controller;

import com.fingerprint.service.FingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileUploadController {

@Autowired
private FingerprintService fingerprintService;

private String storedHash;

@PostMapping("/upload")
public String upload(
@RequestParam("file") MultipartFile file
)throws Exception{

storedHash=
fingerprintService.generateFileHash(
file.getInputStream()
);

return "Stored Hash:\n"+storedHash;
}

@PostMapping("/verify")
public String verify(
@RequestParam("file") MultipartFile file
)throws Exception{

if(storedHash==null){
return "No file uploaded yet";
}

String newHash=
fingerprintService.generateFileHash(
file.getInputStream()
);

if(storedHash.equals(newHash)){
return "VALID - File Not Tampered";
}

return "TAMPERED FILE DETECTED";
}
}
