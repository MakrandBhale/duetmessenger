package com.makarand.duetmessenger.Helper;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EndToEnd {
    private final String key;
    private final String ALGO = "AES";
    private final Cipher cipher;
    private final SecretKeySpec secretKeySpec;

    @SuppressLint("GetInstance")
    public EndToEnd(String key) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.key = key;
        secretKeySpec = generateKey();
        cipher = Cipher.getInstance(ALGO);
    }

    public String decrypt(String encryptedMessage) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        byte[] inputByte = encryptedMessage.getBytes(StandardCharsets.UTF_8);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return new String (cipher.doFinal(Base64.decode(inputByte, Base64.DEFAULT)));

    }
    public String encrypt(String text) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] inputByte = text.getBytes(StandardCharsets.UTF_8);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return new String (Base64.encode(cipher.doFinal(inputByte), Base64.DEFAULT));

    }

    private SecretKeySpec generateKey() throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = this.key.getBytes(StandardCharsets.UTF_8);
        md.update(bytes, 0, bytes.length);
        byte[] key = md.digest();
        return new SecretKeySpec(key, ALGO);
    }


}
