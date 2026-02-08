@echo off
echo Starting GeekBank with Docker...
docker compose up --build -d
echo Application started!
echo App running at http://localhost:7070
echo Database running at localhost:5432
pause
