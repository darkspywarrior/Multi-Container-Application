#!/bin/bash
cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network
./network.sh up createChannel -ca
./network.sh deployCC -ccn fingerprint -ccp ../asset-transfer-basic/chaincode-go -ccl go
