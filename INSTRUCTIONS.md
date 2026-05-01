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
admin
password123


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
--------------------------------------------------
21. IF "peer channel list" TIMES OUT / PEER COMMAND FAILS
--------------------------------------------------

Symptom:

```bash
peer channel list
```

Error:

```text
error getting endorser client for channel:
failed to connect to localhost:7051:
context deadline exceeded
```

OR

```text
Command 'peer' not found
```

--------------------------------------------------
CASE A — "peer: command not found"
--------------------------------------------------

Cause:
Fabric CLI environment not loaded.

Fix:

```bash
cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config/

export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_MSPCONFIGPATH=$PWD/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051
```

Test:

```bash
which peer
```

Should return fabric-samples/bin/peer

Then:

```bash
peer channel list
```

--------------------------------------------------
CASE B — "context deadline exceeded"
--------------------------------------------------

Cause:
Peer unreachable OR TLS env missing.

First check peers:

```bash
docker ps | grep peer
```

Should show:

- peer0.org1.example.com
- peer0.org2.example.com

If missing:

```bash
cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

./network.sh down
./network.sh up createChannel -ca
```

Redeploy chaincode:

```bash
./network.sh deployCC \
-c mychannel \
-ccn fingerprint \
-ccl go \
-ccp ../asset-transfer-basic/chaincode-go
```

--------------------------------------------------
CASE C — PEER RUNNING BUT CLI STILL FAILS
--------------------------------------------------

Cause:
TLS settings missing.

Add:

```bash
export CORE_PEER_TLS_ENABLED=true

export CORE_PEER_TLS_ROOTCERT_FILE=$PWD/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
```

Retry:

```bash
peer channel list
```

Should show:

```text
mychannel
```

--------------------------------------------------
VERIFY PEER HEALTH
--------------------------------------------------

Check logs:

```bash
docker logs peer0.org1.example.com --tail 50
```

Healthy logs show:

- committed block messages
- chaincode proposal processing
- channel mychannel activity

These indicate network is healthy.

--------------------------------------------------
ALTERNATIVE TESTS (if channel list flaky)
--------------------------------------------------

Use:

```bash
peer lifecycle chaincode querycommitted -C mychannel
```

or

```bash
peer node status
```

or

```bash
peer chaincode query \
-C mychannel \
-n fingerprint \
-c '{"Args":["GetHash","proof.txt"]}'
```

--------------------------------------------------
KNOWN NON-FATAL WARNINGS (SAFE TO IGNORE)
--------------------------------------------------

If logs show:

```text
tls: first record does not look like a TLS handshake
```

or

```text
too_many_pings
```

These are common dev-network warnings.
Not fatal.

--------------------------------------------------
EMERGENCY FULL RECOVERY
--------------------------------------------------

```bash
cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

./network.sh down
./network.sh up createChannel -ca

./network.sh deployCC \
-c mychannel \
-ccn fingerprint \
-ccl go \
-ccp ../asset-transfer-basic/chaincode-go
```

Then:

```bash
cd ~/todo-devops-project
docker compose restart fingerprint-service
```

Check:

```bash
curl localhost:8082/fabric/status
```

Must return:

REAL FABRIC ACTIVE

--------------------------------------------------
PERMANENT FIX (optional)
--------------------------------------------------

Add to ~/.bashrc

```bash
echo 'export PATH=$HOME/todo-devops-project/blockchain/fabric/fabric-samples/bin:$PATH' >> ~/.bashrc

echo 'export FABRIC_CFG_PATH=$HOME/todo-devops-project/blockchain/fabric/fabric-samples/config' >> ~/.bashrc

source ~/.bashrc
```

Then peer command persists.

--------------------------------------------------
🚨 Clean stuck Fabric network
From test-network:

./network.sh down
Now force-remove leftovers:

docker ps -a
Kill Fabric leftovers + old CA containers:

docker rm -f ca_org1 ca_org2 ca_orderer peer0.org1.example.com peer0.org2.example.com orderer.example.com
Remove orphan dev containers:

docker container prune -f
Remove dangling chaincode images:

docker image prune -a -f
Remove stuck network:

docker network rm fabric_test
If “still in use”:

See who uses it:

docker network inspect fabric_test
Then remove offending containers shown there:

docker rm -f <container-name>
then again:

docker network rm fabric_test
Restart Fresh
Bring Fabric up again:

./network.sh up createChannel -ca
(Use -ca since you’re using Fabric CAs)

Check:

docker ps
You should see:

orderer.example.com

peer0.org1.example.com

peer0.org2.example.com

ca_org1

ca_org2

ca_orderer

all Up

If nginx is crashing (I noticed restarting)
Quick ignore for demo unless needed, or fix:

docker logs todo-devops-project-nginx-1
Likely bad config. For now if not needed:

docker stop todo-devops-project-nginx-1
Deploy chaincode again (if showing full demo)
./network.sh deployCC \
-ccn fingerprint \
-ccp ../asset-transfer-basic/chaincode-go \
-ccl go
(or your chaincode path)

Quick Demo Sequence
Show:

docker ps
Then:

docker exec -it peer0.org1.example.com bash
(optional if professor asks)

Show channel:

peer channel list
Nuclear reset (if anything still weird)
Fastest guaranteed fix:

./network.sh down
docker system prune -af --volumes
./network.sh up createChannel -ca
Usually works.

Why “Resource still in use”
Because:

fabric_test network
↑
still attached to orphan containers
Not a blockchain issue.

For your professor say:
“Previous chaincode dev containers held the bridge network, I cleaned orphan resources and restarted the Fabric test network.”

Sounds professional.

If you want quickest safest demo route:
Run exactly:

./network.sh down
docker system prune -af --volumes
./network.sh up createChannel -ca
docker ps
Done.

If deployCC fails, paste it fast and we can rescue it.