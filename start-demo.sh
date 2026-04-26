#!/bin/bash

echo "=================================="
echo "Starting DevOps + Blockchain Demo"
echo "=================================="

cd ~/todo-devops-project || exit

echo ""
echo "[1] Starting Docker Desktop check..."
docker info >/dev/null 2>&1 || {
echo "Docker not running. Start Docker Desktop first."
exit 1
}

echo ""
echo "[2] Starting application stack..."
docker compose up -d

echo ""
echo "[3] Starting Hyperledger Fabric..."
cd blockchain/fabric/fabric-samples/test-network || exit

./network.sh up createChannel -ca

echo ""
echo "[4] Check containers..."
docker ps

echo ""
echo "[5] Start demo UI server..."
cd ~/todo-devops-project

nohup python3 -m http.server 8000 > /dev/null 2>&1 &

echo ""
echo "=================================="
echo "Demo Ready"
echo "Frontend:"
echo "http://localhost:8000"
echo ""
echo "API:"
echo "http://localhost:3000"
echo ""
echo "Fingerprint:"
echo "http://localhost:8082"
echo ""
echo "Grafana:"
echo "http://localhost:3001"
echo ""
echo "Prometheus:"
echo "http://localhost:9090"
echo "=================================="
