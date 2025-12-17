#!/bin/bash
set -e


IMAGE_TAG="ui-app:local"
CONTAINER_NAME="ui-app"
PORT=82

echo "Building Docker image..."
docker build -t $IMAGE_TAG .

echo "Stopping existing container (if any)..."
docker rm -f $CONTAINER_NAME 2>/dev/null || true

echo "Starting container..."
docker run -d \
  --name $CONTAINER_NAME \
  -p $PORT:80 \
  --restart unless-stopped \
  $IMAGE_TAG

echo "Deployment complete."
echo "App running at: http://localhost:$PORT"
