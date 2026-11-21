# eth-wallet-cli

이더리움 지갑의 핵심 동작(키, 주소, 니모닉, 트랜잭션 서명, 전송)을 라이브러리 호출에만 기대지 않고 Kotlin으로 직접 구현해 보는 학습용 CLI.

## 진행 단계

- [x] 1. Kotlin 프로젝트 만들기
- [x] 2. 개인키 → 주소 계산 (`AddressTest` 통과)
- [ ] 3. 테스트용 니모닉 → 주소 여러 개 파생 (BIP-39/32/44)
- [ ] 4. Sepolia 테스트넷 잔액 조회 (JSON-RPC)
- [ ] 5. EIP-1559 트랜잭션을 직접 서명해서 전송

## 실행

```bash
./gradlew run
./gradlew test
```

Windows에서는 `gradlew.bat run`, `gradlew.bat test`.

## 보안 규칙

- 실습에는 이 프로젝트용으로 새로 만든 니모닉만 사용
- 니모닉, 개인키, RPC API 키는 `.env`에만 두고 커밋하지 않음 (`.gitignore`에 포함)
