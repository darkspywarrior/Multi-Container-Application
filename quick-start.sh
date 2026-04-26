#!/bin/bash
cd ~/todo-devops-project
docker compose up -d
nohup python3 -m http.server 8000 >/dev/null 2>&1 &

