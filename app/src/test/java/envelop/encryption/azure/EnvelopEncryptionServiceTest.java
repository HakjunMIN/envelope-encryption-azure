package envelop.encryption.azure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.junit.Test;

import envelope.encryption.azure.EnvelopEncryptionService;

public class EnvelopEncryptionServiceTest {

    @Test
    public void testEncryptionAndDecryption() throws Exception {
        EnvelopEncryptionService service = new EnvelopEncryptionService();
        String algorithm = "AES/CBC/PKCS5Padding";
        String input = "Hello, World!";
        SecretKey key = service.generateKey(128);
        IvParameterSpec iv = service.generateIv();

        String cipherText = service.encrypt(algorithm, input, key, iv);
        assertNotNull(cipherText);

        String decryptedText = service.decrypt(algorithm, cipherText, key, iv);
        assertEquals(input, decryptedText);
    }

    @Test
    public void testGenerateIv() {
        EnvelopEncryptionService service = new EnvelopEncryptionService();
        IvParameterSpec iv = service.generateIv();
        assertNotNull(iv);
    }

    @Test
    public void testGenerateKey() throws Exception {
        EnvelopEncryptionService service = new EnvelopEncryptionService();
        SecretKey key = service.generateKey(128);
        assertNotNull(key);
    }


}
