#!/bin/bash

# Build the Docker image (use the current commit hash as the tag to avoid overwriting images)
IMAGE_TAG="ui-app:$(git rev-parse --short HEAD)"

# Build the Docker image
echo "Building the Docker image..."
docker build -t $IMAGE_TAG .

# Stop and remove the existing container if it exists
echo "Stopping and removing existing container..."
docker ps -q --filter "ancestor=$IMAGE_TAG" | xargs -r docker stop | xargs -r docker rm

# Run the new container
echo "Starting the new container..."
docker run -d -p 82:80 --name ui-app $IMAGE_TAG

echo "Deployment complete!"
