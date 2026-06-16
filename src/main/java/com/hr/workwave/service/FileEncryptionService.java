package com.hr.workwave.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provides AES-256-GCM encryption and decryption for contract files stored on disk.
 * Each file gets its own unique random key and IV stored in the database.
 * Files on disk are meaningless without the corresponding database records.
 */
@Service
public class FileEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_BYTES = 32;  // 256-bit key
    private static final int IV_BYTES  = 12;  // 96-bit IV (recommended for GCM)
    private static final int TAG_BITS  = 128; // 16-byte authentication tag

    /**
     * Encrypts the given input stream and writes the ciphertext to the output stream.
     *
     * @return metadata (base64 key + IV) that must be persisted in the database
     */
    public EncryptionMetadata encrypt(InputStream plaintext, OutputStream ciphertext) {
        try {
            byte[] key = randomBytes(KEY_BYTES);
            byte[] iv  = randomBytes(IV_BYTES);

            Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE, key, iv);

            try (CipherOutputStream cos = new CipherOutputStream(ciphertext, cipher)) {
                plaintext.transferTo(cos);
            }

            return new EncryptionMetadata(
                    Base64.getEncoder().encodeToString(key),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt contract file.", e);
        }
    }

    /**
     * Decrypts a previously encrypted file and returns it as a Spring Resource ready for streaming.
     */
    public Resource decryptAsResource(InputStream ciphertext, String base64Key, String base64Iv) {
        try {
            byte[] key            = Base64.getDecoder().decode(base64Key);
            byte[] iv             = Base64.getDecoder().decode(base64Iv);
            byte[] encryptedBytes = readAllBytes(ciphertext);

            Cipher cipher = buildCipher(Cipher.DECRYPT_MODE, key, iv);
            byte[] decrypted = cipher.doFinal(encryptedBytes);

            return new InputStreamResource(new ByteArrayInputStream(decrypted));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt contract file.", e);
        }
    }

    private Cipher buildCipher(int mode, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BITS, iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, keySpec, gcmSpec);
        return cipher;
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        return is.readAllBytes();
    }

    public record EncryptionMetadata(String base64Key, String base64Iv) {}
}

