#!/bin/bash
echo "Starting GeekBank with Docker..."
# Clean macOS metadata files that cause Docker build errors on external drives
# Clean macOS metadata files that cause Docker build errors on external drives
echo "Cleaning macOS metadata files..."
find . -name "._*" -type f -delete
docker compose up --build -d
echo "Application started!"
echo "App running at http://localhost:7070"
echo "Database running at localhost:5432"
