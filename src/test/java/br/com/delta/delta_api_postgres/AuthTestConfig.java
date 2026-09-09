package br.com.delta.delta_api_postgres;

import java.security.KeyPairGenerator;
import java.nio.file.Files;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class AuthTestConfig {
    @DynamicPropertySource
    static void signingKeys(DynamicPropertyRegistry registry) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        var privateFile = Files.createTempFile("delta-test-private-", ".pem");
        var publicFile = Files.createTempFile("delta-test-public-", ".pem");
        privateFile.toFile().deleteOnExit();
        publicFile.toFile().deleteOnExit();
        Files.writeString(privateFile, pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        Files.writeString(publicFile, pem("PUBLIC KEY", pair.getPublic().getEncoded()));
        registry.add("auth.private-key", () -> privateFile.toUri().toString());
        registry.add("auth.public-key", () -> publicFile.toUri().toString());
    }

    private static String pem(String type, byte[] bytes) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{10}).encodeToString(bytes)
                + "\n-----END " + type + "-----\n";
    }
}
