package com.fingerprint.service;

import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.security.MessageDigest;

@Service
public class FingerprintService {

public String generateHash(String algorithm,String data) throws Exception{
MessageDigest digest=
MessageDigest.getInstance(algorithm);

return bytesToHex(
digest.digest(data.getBytes("UTF-8"))
);
}

public String generateFileHash(InputStream inputStream) throws Exception{

MessageDigest digest=
MessageDigest.getInstance("SHA-256");

byte[] buffer=new byte[4096];
int read;

while((read=inputStream.read(buffer))!=-1){
digest.update(buffer,0,read);
}

return bytesToHex(digest.digest());
}

private String bytesToHex(byte[] bytes){

StringBuilder sb=new StringBuilder();

for(byte b:bytes){
String hex=Integer.toHexString(0xff & b);

if(hex.length()==1){
sb.append('0');
}

sb.append(hex);
}

return sb.toString();
}
}
