#!/bin/bash

cd ~/todo-devops-project

echo "Stopping app stack..."
docker compose down

echo "Stopping fabric..."
cd blockchain/fabric/fabric-samples/test-network
./network.sh down

pkill -f "python3 -m http.server"

echo "Everything stopped."
