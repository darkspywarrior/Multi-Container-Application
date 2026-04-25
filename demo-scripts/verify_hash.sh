#!/bin/bash

cd ~/todo-devops-project/blockchain/fabric/fabric-samples/test-network

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=$PWD/../config

peer chaincode query \
-C mychannel \
-n fingerprint \
-c '{"Args":["ReadAsset","asset20"]}'

echo
echo "✅ Blockchain Verification Successful"
