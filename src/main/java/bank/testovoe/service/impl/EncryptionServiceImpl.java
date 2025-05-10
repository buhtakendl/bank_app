package bank.testovoe.service.impl;

import bank.testovoe.exception.EncryptionException;
import bank.testovoe.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final String ALGORITHM = "AES";

    private final SecretKeySpec secretKey;

    public EncryptionServiceImpl(@Value("${encryption.secret}") String secret) {
        byte[] key = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 16);
        this.secretKey = new SecretKeySpec(key, ALGORITHM);
    }

    @Override
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt", e);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        try {
            log.debug("Trying to decrypt: {}", encryptedText);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
             log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt", e);
        }
    }
}

