![CI](https://github.com/HakjunMIN/envelope-encryption-azure/actions//workflows/gradle.yml/badge.svg)

# Envelope Encryption using Azure Keyvault

## 시나리오

### 암호화 시 

1. Azure Keyvault에서 KEK(Key Encryption Key)를 생성
2. DEK(Data Encryption Key)를 생성하여 데이터 암호화
3. DEK를 KEK로 암호화(Wrap)
4. 암호화된 DEK를 별도의 Azure Keyvault에 `secret`값으로 관리

### 복호화 시 

5. 1)의 KEK를 이용하여 4)의 DEK를 복호화(Unwrap), 복호화된 DEK로 데이터 복호화

### KEK 로테이션 시 (자동 혹은 수동)

6. 1)의 KEK를 로테이션하면 새로운 버전의 KEK생성되며 버전 없은 키의 정보가 자동으로 latest버전을 참조하게 됨.
7. 직전 버전의 KEK로 암호화된 DEK를 복호화
8. 로테이션된 새 KEK로 DEK를 암호화
9. 암호화된 DEK를 `secret`에 새로 생성. 자동으로 latest버전 참조하게 됨
10. Application쪽에서는 항상 최신의 KEK와 암호화된 DEK를 직접 참조.

## Quick start

* 실행하는 Azure 계정이 Keyvault의 키와 secret정보를 사용할 수 있어야 함. 아래 url참고

> [!Note]
> https://learn.microsoft.com/ko-kr/azure/key-vault/general/rbac-guide?tabs=azure-cli

```bash
$ az login
$ export keyIdentifier=<your-key-identifiler>  # ex: 'https://test.vault.azure.net/keys/mykey'
$ export keyVaultUrl=<your-keyvault-url> # ex: 'https://test.vault.azure.net'
$ ./gradlew clean test
```

* 결과 
    * 단위테스트: EnvelopEncryptionServiceTest
    * 통합테스트: EnvelopEncryptionServiceItTest

```bash
> Task :app:test

envelop.encryption.azure.EnvelopEncryptionServiceItTest

  Test testEnvelopeEncryptionIntegration PASSED (6s)

envelop.encryption.azure.EnvelopEncryptionServiceTest

  Test testGenerateIv PASSED
  Test testGenerateKey PASSED
  Test testEncryptionAndDecryption PASSED

SUCCESS: Executed 4 tests in 7s


BUILD SUCCESSFUL in 8s
```

## 통합테스트 설명

```java 
    ...
    // DEK생성
    String input = "secret";
    SecretKey key = service.generateKey(128);

    // DEK암호화 (Wrap)
    WrapResult wrapResult = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key.getEncoded());
    String encryptedKey = Base64.getEncoder().encodeToString(wrapResult.getEncryptedKey());

    // 암호화된 DEK는 secret으로 저장 (Base64 encoding)
    secretClient.setSecret("encryptedKey", encryptedKey);
    KeyVaultSecret retrievedSecret = secretClient.getSecret("encryptedKey");

    // 암호화된 DEK 사용시 복호화
    UnwrapResult unwrapResult = cryptoClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, Base64.getDecoder().decode(retrievedSecret.getValue()));
    SecretKey finalKey = new SecretKeySpec(unwrapResult.getKey(), "AES");

    // Value를 DEK로 암복호화
    IvParameterSpec ivParameterSpec = service.generateIv();
    String algorithm = "AES/CBC/PKCS5Padding";
    String cipherText = service.encrypt(algorithm, input, finalKey, ivParameterSpec);
    String plainText = service.decrypt(algorithm, cipherText, finalKey, ivParameterSpec);

    ...
```