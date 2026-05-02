Alright — this is your **FINAL CLEAN WALKTHROUGH**
👉 From **start → full demo → close → next day restart in 10 mins → 5 min live demo**
👉 No theory, only **exact flow + commands + what to show**

---

# 🔴 PART 1 — CLOSE EVERYTHING NOW (SAFE)

```bash
cd ~/todo-devops-project
docker compose down

cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network
./network.sh down
```

✔ System fully stopped
✔ No corruption
✔ Ready for tomorrow

---

# 🟢 PART 2 — TOMORROW FULL START (10 MIN SETUP)

---

## 🔥 STEP 1 — START BLOCKCHAIN (FIRST)

```bash
cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

./network.sh down

./network.sh up createChannel -c mychannel -ca
```

---

## 🔥 STEP 2 — DEPLOY CHAINCODE (GO / VERSION 3.1)

```bash
./network.sh deployCC \
-c mychannel \
-ccn fingerprint \
-ccp ../chaincode/fingerprint \
-ccl go \
-ccv 3.1
```

---

## 🔥 STEP 3 — COPY TLS CERT (IMPORTANT)

```bash
cd ~/todo-devops-project/fingerprint-service

cp \
../blockchain/fabric/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/tlsca/tlsca.org1.example.com-cert.pem \
fabric-config/peer0-tls.pem
```

---

## 🔥 STEP 4 — START FULL SYSTEM

```bash
cd ~/todo-devops-project

docker compose down
docker compose up -d --build

sleep 10
```

---

## 🔥 STEP 5 — VERIFY REAL FABRIC CONNECTION

```bash
docker logs todo-devops-project-fingerprint-service-1 | grep Fabric
```

👉 MUST SHOW:

```
Connected to REAL Hyperledger Fabric network
```

👉 IF NOT → STOP DEMO (fix first)

---

# 🟢 PART 3 — BLOCKCHAIN PROOF (CLI)

---

## 🔥 SET ENV

```bash
cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/../config

export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_MSPCONFIGPATH=$PWD/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051
export CORE_PEER_TLS_ROOTCERT_FILE=$PWD/organizations/peerOrganizations/org1.example.com/tlsca/tlsca.org1.example.com-cert.pem
```

---

## 🔥 INVOKE

```bash
peer chaincode invoke \
--waitForEvent \
-o localhost:7050 \
--ordererTLSHostnameOverride orderer.example.com \
--tls \
--cafile $PWD/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem \
-C mychannel \
-n fingerprint \
--peerAddresses localhost:7051 \
--tlsRootCertFiles $PWD/organizations/peerOrganizations/org1.example.com/tlsca/tlsca.org1.example.com-cert.pem \
--peerAddresses localhost:9051 \
--tlsRootCertFiles $PWD/organizations/peerOrganizations/org2.example.com/tlsca/tlsca.org2.example.com-cert.pem \
-c '{"Args":["StoreHash","file99","report.pdf","abc123hash","Ira","2026"]}'
```

---

## 🔥 QUERY

```bash
peer chaincode query \
-C mychannel \
-n fingerprint \
-c '{"Args":["GetHash","file99"]}'
```

---

## 🔥 VERIFY

```bash
peer chaincode query \
-C mychannel \
-n fingerprint \
-c '{"Args":["VerifyHash","file99","abc123hash"]}'
```

---

## 🔥 CHECK DEPLOYMENT

```bash
peer lifecycle chaincode querycommitted \
-C mychannel \
-n fingerprint
```

---

# 🟢 PART 4 — APPLICATION DEMO (MAIN PART)

---

## 🔥 CREATE FILE

```bash
cd ~/todo-devops-project
echo hello > test.txt
```

---

## 🔥 UPLOAD

```bash
curl -F "file=@test.txt" http://localhost:8082/file/upload
```

👉 SHOW:

* Hash
* Transaction ID
* Blockchain stored
* MinIO stored

(Like your real output) 

---

## 🔥 VERIFY (VALID)

```bash
curl -F "file=@test.txt" http://localhost:8082/file/verify
```

👉 SHOW:

```
✅ FILE INTEGRITY: VALID
```

---

## 🔥 TAMPER

```bash
echo hacked >> test.txt
```

---

## 🔥 VERIFY (TAMPERED)

```bash
curl -F "file=@test.txt" http://localhost:8082/file/verify
```

👉 SHOW:

```
❌ FILE INTEGRITY: TAMPERED
```

---

# 🟢 PART 5 — MINIO PROOF

---

```bash
docker ps | grep minio
```

```bash
docker exec -it todo-devops-project-minio-1 sh
```

Inside:

```bash
mc alias set local http://localhost:9000 admin password123
mc ls local/fingerprints
exit
```

👉 SHOW files exist
(proves storage layer)

---

# 🟢 PART 6 — INFO + ARCHITECTURE + AUDIT

---

## 🔥 INFO

```bash
curl http://localhost:8082/file/info
```

---

## 🔥 AUDIT TRAIL

```bash
curl http://localhost:8082/audit/trail
```

👉 SHOW blockchain records
(real ledger data) 

---

## 🔥 ARCHITECTURE (IF EXISTS)

```bash
curl http://localhost:8082/file/architecture
```

---

# 🟢 PART 7 — MONITORING

---

## 🔥 PROMETHEUS

Open:

```
http://localhost:9090
```

---

## 🔥 GRAFANA

Open:

```
http://localhost:3001
```

Login:

```
admin / admin
```

---

# ⚡ FINAL DEMO FLOW (5 MIN FAST VERSION)

If time is less, do ONLY:

```bash
# Start
network.sh up + deployCC
docker compose up -d --build

# Check
docker logs ... | grep Fabric

# Demo
echo hello > test.txt
curl upload
curl verify
echo hacked >> test.txt
curl verify

# Show proof
curl audit/trail
docker exec minio -> mc ls
```

---

# 🔴 FINAL — AFTER DEMO CLOSE

```bash
cd ~/todo-devops-project
docker compose down

cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network
./network.sh down
```

---

# 🚀 WHAT YOU WILL SHOW (FULL SYSTEM)

✔ Hyperledger Fabric network (peers + orderer)
✔ Chaincode deployed (Go v3.1)
✔ Spring Boot fingerprint service
✔ MinIO object storage
✔ File upload → blockchain commit
✔ File verification → tamper detection
✔ Audit trail from blockchain
✔ Monitoring (Prometheus + Grafana)

---

You now have:
👉 **10-min setup plan**
👉 **5-min demo plan**
👉 **full commands matching your real outputs**

---

If you want next:

* 🎤 exact viva answers
* 📄 architecture diagram for report
* ⚡ 60-second explanation

just tell me 👍
