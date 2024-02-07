package envelop.encryption.azure;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;
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

public class EnvelopEncryptionServiceItTest {

    private String keyIdentifier;
    private String keyVaultUrl;

    @Before
    public void setUp() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            prop.load(input);
            keyIdentifier = prop.getProperty("keyIdentifier");
            keyVaultUrl = prop.getProperty("keyVaultUrl");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testEnvelopeEncryptionIntegration() throws Exception {
        EnvelopEncryptionService service = new EnvelopEncryptionService();

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .keyIdentifier(keyIdentifier)
        .buildClient();

        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(keyVaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();    

        String input = "secret";
        SecretKey key = service.generateKey(128);

        WrapResult wrapResult = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key.getEncoded());
        String encryptedKey = Base64.getEncoder().encodeToString(wrapResult.getEncryptedKey());
    
        secretClient.setSecret("encryptedKey", encryptedKey);
        KeyVaultSecret retrievedSecret = secretClient.getSecret("encryptedKey");

        UnwrapResult unwrapResult = cryptoClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, Base64.getDecoder().decode(retrievedSecret.getValue()));
        SecretKey finalKey = new SecretKeySpec(unwrapResult.getKey(), "AES");

        IvParameterSpec ivParameterSpec = service.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";
        String cipherText = service.encrypt(algorithm, input, finalKey, ivParameterSpec);
        String plainText = service.decrypt(algorithm, cipherText, finalKey, ivParameterSpec);

        assertEquals(input, plainText);
     
    }


}
