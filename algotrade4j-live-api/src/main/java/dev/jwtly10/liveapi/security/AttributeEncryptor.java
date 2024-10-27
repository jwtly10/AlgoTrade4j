package dev.jwtly10.liveapi.security;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static String SECRET_KEY;
    @Value("${security.encryption.key}")
    private String secretKey;

    @PostConstruct
    private void initSecretKey() {
        SECRET_KEY = secretKey;
    }

    @Override
    public String convertToDatabaseColumn(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            byte[] encryptedData = cipher.doFinal(data.getBytes());

            byte[] ivAndEncryptedData = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, ivAndEncryptedData, 0, iv.length);
            System.arraycopy(encryptedData, 0, ivAndEncryptedData, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(ivAndEncryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            byte[] ivAndEncryptedData = Base64.getDecoder().decode(encryptedData);

            byte[] iv = new byte[16];
            System.arraycopy(ivAndEncryptedData, 0, iv, 0, iv.length);
            IvParameterSpec ivParams = new IvParameterSpec(iv);

            byte[] actualEncryptedData = new byte[ivAndEncryptedData.length - iv.length];
            System.arraycopy(ivAndEncryptedData, iv.length, actualEncryptedData, 0, actualEncryptedData.length);

            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            return new String(cipher.doFinal(actualEncryptedData));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}