import java.nio.file.*;
import java.security.*;
import java.util.Base64;
public class GenerateLocalKeys {
 public static void main(String[] args) throws Exception {
  Path priv = Path.of(".local/private.pem"), pub = Path.of(".local/public.pem");
  if (Files.exists(priv) || Files.exists(pub)) throw new IllegalStateException("Existing keys will not be overwritten");
  KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
  generator.initialize(2048);
  KeyPair pair = generator.generateKeyPair();
  write(priv, "PRIVATE KEY", pair.getPrivate().getEncoded());
  write(pub, "PUBLIC KEY", pair.getPublic().getEncoded());
  Signature signature = Signature.getInstance("SHA256withRSA");
  signature.initSign(pair.getPrivate()); signature.update(new byte[]{1,2,3});
  byte[] signed = signature.sign();
  signature.initVerify(pair.getPublic()); signature.update(new byte[]{1,2,3});
  if (!signature.verify(signed)) throw new IllegalStateException("Invalid key pair");
  System.out.println("RSA 2048 PKCS#8 / X.509 keys generated and signature verified.");
 }
 static void write(Path path, String type, byte[] bytes) throws Exception {
  Files.writeString(path, "-----BEGIN " + type + "-----\n" + Base64.getMimeEncoder(64, new byte[]{10}).encodeToString(bytes) + "\n-----END " + type + "-----\n", StandardOpenOption.CREATE_NEW);
 }
}
