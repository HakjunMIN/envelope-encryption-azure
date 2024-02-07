package envelope.encryption.azure;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class EnvelopEncryptionService {
   
    public static void main(String[] args) throws Exception {

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("https://kvspr.vault.azure.net/keys/mykey/")
            .buildClient();

        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("https://kvspr.vault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();    

        String input = "secret";
        SecretKey key = generateKey(128);

        WrapResult wrapResult = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key.getEncoded());
        String encryptedKey = Base64.getEncoder().encodeToString(wrapResult.getEncryptedKey());
      
        KeyVaultSecret secret = secretClient.setSecret("encryptedKey", encryptedKey);
        KeyVaultSecret retrievedSecret = secretClient.getSecret("encryptedKey");

        UnwrapResult unwrapResult = cryptoClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, Base64.getDecoder().decode(retrievedSecret.getValue()));
        SecretKey finalKey = new SecretKeySpec(unwrapResult.getKey(), "AES");

        IvParameterSpec ivParameterSpec = generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";
        String cipherText = encrypt(algorithm, input, finalKey, ivParameterSpec);
        String plainText = decrypt(algorithm, cipherText, finalKey, ivParameterSpec);
        System.out.printf("Comparing origin '%s' and decrypted from envelop ecnryption '%s'%n", input, plainText);


    }

    public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,   BadPaddingException, IllegalBlockSizeException {
    
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
            .encodeToString(cipherText);
    }   

    public static String decrypt(String algorithm, String cipherText, SecretKey key,IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,BadPaddingException, IllegalBlockSizeException {
    
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
            .decode(cipherText));
        return new String(plainText);
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }


}