package com.meriem.securevaultapp.screens;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CryptoHelper {
    private static final String KEY_ALIAS = Base64.encodeToString("secureVaultiokey".getBytes(), Base64.NO_WRAP);
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    // AES + CBC + Padding
    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";

    // 1. Generate or get secret key
    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            Log.d("CryptoHelper", "Key alias not found, generating key...");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();
            Log.d("CryptoHelper", "Key generated successfully.");
        }else {
            Log.d("CryptoHelper", "Key already exists.");
        }

        return ((SecretKey) keyStore.getKey(KEY_ALIAS, null));
    }

    // 2. Encrypt data
    public static String encrypt(String plainText) {
        try {
            SecretKey key = getSecretKey();
            Log.d("CryptoHelper", "Using key: " + key.getAlgorithm());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(cipher.ENCRYPT_MODE, key);

            byte[] iv = cipher.getIV();

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // Combine IV + encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            String encoded =  Base64.encodeToString(combined, Base64.DEFAULT);
            Log.d("CryptoHelper", "Encrypted output (Base64): " + encoded);
            return encoded;
        } catch (Exception e) {
            Log.e("CryptoHelper", "Encryption failed", e);
            return plainText;
        }
    }

    // 3. Decrypt
    public static String decrypt(String encryptedData) {
        try {
            SecretKey key = getSecretKey();
            byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);

            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];

            // Separate IV and encrypted data
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedData;
        }
    }
}
