package com.unz.eval.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Chiffrement AES-256-GCM pour les données personnelles sensibles.
 * ENF-6 : les données personnelles (email, nom) sont chiffrées en base.
 *
 * Utilisation : annoter le champ JPA avec @Convert(converter = AesEncryptor.class)
 *
 * Algorithme : AES-256-GCM (authentifié, résistant aux manipulations)
 * IV  : 12 bytes aléatoires par chiffrement (jamais réutilisé)
 * Tag : 128 bits (garantit l'intégrité du chiffré)
 */
@Component
@Converter
public class AesEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    @Value("${app.encryption.key}")
    private String encryptionKey;

    private SecretKeySpec getKey() {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        // Toujours 32 bytes pour AES-256
        byte[] key32 = new byte[32];
        System.arraycopy(keyBytes, 0, key32, 0, Math.min(keyBytes.length, 32));
        return new SecretKeySpec(key32, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Format stocké : IV (12 bytes) + ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            // log.error("Erreur de chiffrement AES", e);
            throw new RuntimeException("Erreur de chiffrement des données personnelles", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encrypted) {
        if (encrypted == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // log.error("Erreur de déchiffrement AES", e);
            throw new RuntimeException("Erreur de déchiffrement des données personnelles", e);
        }
    }
}
