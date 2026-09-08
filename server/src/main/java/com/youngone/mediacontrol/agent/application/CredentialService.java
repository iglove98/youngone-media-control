package com.youngone.mediacontrol.agent.application;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;import java.nio.charset.StandardCharsets;import java.security.*;import java.util.*;
@Service public class CredentialService{
 private final String bootstrapToken;private final SecureRandom random=new SecureRandom();public CredentialService(@Value("${youngone.agent.bootstrap-token}")String token){bootstrapToken=token;}
 public boolean validBootstrap(String token){return constantTime(bootstrapToken,token);}public String issue(){byte[] b=new byte[32];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}public String hash(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}public boolean constantTime(String a,String b){return a!=null&&b!=null&&MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8));}
}
