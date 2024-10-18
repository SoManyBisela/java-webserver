package com.simonebasile.sampleapp.security;

import com.simonebasile.sampleapp.assertions.Assertions;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class ArgonUtils {

    public static String hash(String password) {
        return hash(password, generateSalt16Byte());
    }

    private static String hash(String password, byte[] salt) {
        int opsLimit = 3;
        int memLimit = 12288;
        int parallelism = 1;
        int outputLength = 32;
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13) // 19
                .withIterations(opsLimit)
                .withMemoryAsKB(memLimit)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(params);
        byte[] result = new byte[outputLength];
        gen.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
        return base64Encoding(result) + base64Encoding(salt);
    }

    public static boolean verify(String password, String passwordHash) {
        String encSalt = passwordHash.substring(44);
        byte[] salt = base64Decoding(encSalt);
        Assertions.assertTrue(salt.length == 16);
        return hash(password, salt).equals(passwordHash);
    }

    private static byte[] generateSalt16Byte() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }

    private static String base64Encoding(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    private static byte[] base64Decoding(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }


}
