package kr.co.itid.cms.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public final class CryptoUtil {
    // 키 파일 고정 경로 (필요하면 이 줄만 바꾸면 됨)
    private static final Path KEY_PATH = Path.of("/data/egovframe-cms/install/crypto_key/crypto.key");

    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;
    private static final byte VERSION = 1;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile SecretKey CACHED_KEY;

    private CryptoUtil() {}

    public static byte[] encryptToBytes(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(1 + IV_LEN + ct.length);
            bb.put(VERSION).put(iv).put(ct);
            return bb.array();
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public static String decryptToString(byte[] blob) {
        if (blob == null) return null;
        try {
            if (blob.length > 0 && blob[0] == VERSION) {
                ByteBuffer bb = ByteBuffer.wrap(blob);
                bb.get(); // version
                byte[] iv = new byte[IV_LEN]; bb.get(iv);
                byte[] ct = new byte[bb.remaining()]; bb.get(ct);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
                byte[] pt = cipher.doFinal(ct);
                return new String(pt, StandardCharsets.UTF_8);
            }
            return new String(blob, StandardCharsets.UTF_8); // 레거시 안전처리
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private static SecretKey getKey() {
        if (CACHED_KEY == null) {
            synchronized (CryptoUtil.class) {
                if (CACHED_KEY == null) {
                    byte[] keyBytes = loadKeyBytes();
                    CACHED_KEY = new SecretKeySpec(keyBytes, "AES");
                }
            }
        }
        return CACHED_KEY;
    }

    private static byte[] loadKeyBytes() {
        try {
            if (!Files.exists(KEY_PATH)) {
                throw new IllegalStateException("Crypto key file not found: " + KEY_PATH);
            }
            String hex = Files.readString(KEY_PATH).trim();
            byte[] key = hexToBytes(hex);
            int len = key.length;
            if (len != 16 && len != 24 && len != 32) {
                throw new IllegalStateException("Key must be 16/24/32 bytes (HEX). actual=" + len);
            }
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load crypto key from " + KEY_PATH, e);
        }
    }

    private static byte[] hexToBytes(String hex) {
        String s = (hex.startsWith("0x") || hex.startsWith("0X")) ? hex.substring(2) : hex;
        if ((s.length() & 1) != 0) throw new IllegalArgumentException("Invalid HEX length");
        byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            out[i / 2] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
        }
        return out;
    }
}