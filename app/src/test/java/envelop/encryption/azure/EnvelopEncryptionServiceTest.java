package envelop.encryption.azure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

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
