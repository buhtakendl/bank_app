package bank.testovoe.service;

public interface EncryptionService {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
}
