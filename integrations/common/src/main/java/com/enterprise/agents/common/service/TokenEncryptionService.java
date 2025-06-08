package com.enterprise.agents.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TokenEncryptionService {
    private final SecretKeySpec secretKey;
    private final Cipher cipher;

    public TokenEncryptionService(@Value("${app.encryption.key}") String encryptionKey) throws Exception {
        // Ensure the key is 32 bytes (256 bits) for AES-256
        byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[32];
        System.arraycopy(key, 0, paddedKey, 0, Math.min(key.length, paddedKey.length));

        this.secretKey = new SecretKeySpec(paddedKey, "AES");
        this.cipher = Cipher.getInstance("AES");
    }

    public String encrypt(String value) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encrypted) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decryptedBytes);
    }
} 