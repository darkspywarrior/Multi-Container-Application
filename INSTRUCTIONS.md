# Blockchain File Integrity Platform — Runbook / Recovery / Demo Guide

Author: Ira
Stack:
- Hyperledger Fabric
- Spring Boot Fingerprint Service
- MinIO
- Mongo
- Docker Compose
- Prometheus + Grafana

--------------------------------------------------
1. PROJECT STARTUP (NORMAL START)
--------------------------------------------------

Open terminal:

cd ~/todo-devops-project

Start docker services:

docker compose up -d

Check containers:

docker ps

Expected:
api
fingerprint-service
mongo
minio
prometheus
grafana
peer0.org1
peer0.org2
orderer

Check app:

curl localhost:8082/fabric/status

Expected:

REAL FABRIC ACTIVE
Channel: mychannel
Contract: fingerprint
Status: CONNECTED

If "DEMO MODE"
go to section 6.

--------------------------------------------------
2. START FABRIC IF BLOCKCHAIN IS DOWN
--------------------------------------------------

cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

Bring network up:

./network.sh up createChannel -ca

Deploy chaincode:

./network.sh deployCC \
-c mychannel \
-ccn fingerprint \
-ccp ../asset-transfer-basic/chaincode-go \
-ccl go

Check peers:

docker ps | grep peer

Check channel:

peer channel list

Should show:

mychannel

Return:

cd ~/todo-devops-project

Restart service:

docker compose restart fingerprint-service

Verify:

curl localhost:8082/fabric/status

Should switch to REAL FABRIC ACTIVE

--------------------------------------------------
3. FILE UPLOAD TEST
--------------------------------------------------

Create file:

echo hello > proof.txt

Upload:

curl -F "file=@proof.txt" \
http://localhost:8082/file/upload

Expected:
- hash generated
- stored in MinIO
- committed to Fabric

--------------------------------------------------
4. VERIFY TAMPER DETECTION
--------------------------------------------------

Verify valid file:

curl -F "file=@proof.txt" \
http://localhost:8082/file/verify

Should show:

VALID

Tamper test:

echo hacked >> proof.txt

Verify again:

curl -F "file=@proof.txt" \
http://localhost:8082/file/verify

Should show:

TAMPERED

--------------------------------------------------
5. CHECK MINIO
--------------------------------------------------

Open:

http://localhost:9001

login:
viyaa
Praam09

Bucket:
fingerprints

Should show uploaded files.

CLI check:

docker exec -it todo-devops-project-minio-1 sh

mc alias set local http://localhost:9000 viyaa Praam09

mc ls local/fingerprints

--------------------------------------------------
6. IF SYSTEM FALLS INTO DEMO MODE
--------------------------------------------------

Check:

curl localhost:8082/fabric/status

If:

DEMO MODE

Possible causes:

A.
Fabric network down

Fix:

cd blockchain/fabric/fabric-samples/test-network

./network.sh up createChannel -ca

B.
Peer containers dead

docker ps | grep peer

Restart:

docker start peer0.org1.example.com
docker start peer0.org2.example.com
docker start orderer.example.com

C.
Gateway lost wallet

Check:

ls fingerprint-service/wallet/User1/msp

Must have:
signcerts
keystore

Restart service:

docker compose restart fingerprint-service

Check again:

curl localhost:8082/fabric/status

Must return REAL FABRIC ACTIVE

--------------------------------------------------
7. IF DOCKER BREAKS
--------------------------------------------------

Clean reset:

docker compose down

docker network prune -f

docker volume prune -f

docker compose up -d --build

Check:

docker ps

--------------------------------------------------
8. IF MINIO FAILS
--------------------------------------------------

Check logs:

docker logs todo-devops-project-minio-1

Restart:

docker compose restart minio

Health:

curl localhost:9000/minio/health/live

--------------------------------------------------
9. IF CHAINCODE CHANGED
--------------------------------------------------

Re-deploy:

cd blockchain/fabric/fabric-samples/test-network

./network.sh deployCC \
-c mychannel \
-ccn fingerprint \
-ccp ../asset-transfer-basic/chaincode-go \
-ccl go

Restart app:

cd ~/todo-devops-project

docker compose restart fingerprint-service

--------------------------------------------------
10. CHECK LEDGER MANUALLY
--------------------------------------------------

cd blockchain/fabric/fabric-samples/test-network

Query:

peer chaincode query \
-C mychannel \
-n fingerprint \
-c '{"Args":["GetHash","proof.txt"]}'

History:

peer chaincode query \
-C mychannel \
-n fingerprint \
-c '{"Args":["GetHistory","proof.txt"]}'

--------------------------------------------------
11. PERFORMANCE DEMO
--------------------------------------------------

for i in {1..10}
do
time curl -s -F "file=@proof.txt" \
http://localhost:8082/file/upload >/dev/null
done

--------------------------------------------------
12. LIVE DEMO SCRIPT FOR PRESENTATION
--------------------------------------------------

Demo flow:

1 Show containers

docker ps

2 Show blockchain live

curl localhost:8082/fabric/status

3 Upload document

curl -F "file=@proof.txt" \
http://localhost:8082/file/upload

4 Show MinIO object

mc ls local/fingerprints

5 Verify file

curl -F "file=@proof.txt" \
http://localhost:8082/file/verify

6 Tamper file

echo hacked >> proof.txt

7 Verify again

curl -F "file=@proof.txt" \
http://localhost:8082/file/verify

Shows tamper detection.

--------------------------------------------------
13. GIT PUSH
--------------------------------------------------

Check status:

git status

Add:

git add .

Commit:

git commit -m "Working Fabric + MinIO integrity platform"

Push:

git push origin main

If first push:

git remote add origin <repo-url>

git branch -M main

git push -u origin main

--------------------------------------------------
14. BEFORE EVERY PUSH
--------------------------------------------------

Run:

mvn clean package

docker compose up -d --build

Test:

upload
verify
tamper
fabric status
minio check

Then push.

--------------------------------------------------
15. IF SOMETHING BREAKS BADLY
--------------------------------------------------

Golden reset:

Stop everything:

docker compose down

Fabric down:

cd blockchain/fabric/fabric-samples/test-network

./network.sh down

Bring back:

./network.sh up createChannel -ca

Deploy cc

Return:

cd ~/todo-devops-project

docker compose up -d --build

System should fully recover.

--------------------------------------------------
16. USEFUL URLS
--------------------------------------------------

App:
http://localhost:8082

MinIO:
http://localhost:9001

Grafana:
http://localhost:3001

Prometheus:
http://localhost:9090

--------------------------------------------------
17. WHAT "REAL" MEANS
--------------------------------------------------

REAL mode means:
- actual Fabric peers endorse
- orderer commits blocks
- ledger stores hashes
- MinIO stores file
- verification queries blockchain

DEMO means:
mock fallback in service
not real ledger.

Always show REAL in presentations.

--------------------------------------------------
18. QUICK HEALTH CHECK
--------------------------------------------------

Run:

docker ps

curl localhost:8082/fabric/status

curl localhost:9000/minio/health/live

All green -> ready.

--------------------------------------------------
19. NEVER FORGET
--------------------------------------------------

If:
Expected 5 params got 4

Chaincode changed.
Redeploy chaincode.

If:
DEMO MODE

Restart Fabric.

If:
UnknownHost minio

Docker network issue.

Rebuild compose.

--------------------------------------------------
20. BACKUP BEFORE BIG CHANGES
--------------------------------------------------

git add .
git commit -m "backup before changes"

Always.

--------------------------------------------------
DONE
--------------------------------------------------
