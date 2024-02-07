# Envelope Encryption using Azure Kevvault

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

```bash

```
