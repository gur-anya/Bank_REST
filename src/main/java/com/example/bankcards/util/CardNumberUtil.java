package com.example.bankcards.util;

import com.example.bankcards.exception.CardEncryptingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class CardNumberUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "BankCardsSecretKey123";
    private static final int IV_LENGTH = 16;

    public static byte[] encryptCardNumber(String cardNumber) {
        try {
            SecretKeySpec secretKey = generateKey();
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            byte[] ivAndEncryptedBytes = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, ivAndEncryptedBytes, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, ivAndEncryptedBytes, IV_LENGTH, encryptedBytes.length);

            return ivAndEncryptedBytes;
        } catch (Exception e) {
            throw new CardEncryptingException("Card encoding error", e);
        }
    }

    public static String decryptCardNumber(byte[] ivAndEncryptedBytes) {
        try {
            if (ivAndEncryptedBytes == null || ivAndEncryptedBytes.length <= IV_LENGTH) {
                throw new CardEncryptingException("Invalid encrypted data");
            }

            SecretKeySpec secretKey = generateKey();

            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivAndEncryptedBytes, 0, IV_LENGTH);
            int encryptedSize = ivAndEncryptedBytes.length - IV_LENGTH;
            byte[] encryptedBytes = new byte[encryptedSize];
            System.arraycopy(ivAndEncryptedBytes, IV_LENGTH, encryptedBytes, 0, encryptedSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CardEncryptingException("Card decrypting error", e);
        }
    }

    private static SecretKeySpec generateKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }
}
