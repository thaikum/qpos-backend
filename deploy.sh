#!/bin/bash

# Change to app directory
cd /home/pos || exit 1

# Kill existing process on port 8080
echo "Stopping existing application..."
fuser -k 8080/tcp
sleep 2

# Find and start latest JAR
LATEST_JAR=$(ls -t QposBackend-*.jar 2>/dev/null | head -1)

if [ -z "$LATEST_JAR" ]; then
    echo "Error: No JAR file found"
    exit 1
fi

echo "Starting $LATEST_JAR..."
nohup java -jar "$LATEST_JAR" &

echo "Application started with PID $!"
