#!/bin/bash

# Get the current GCP project ID
PROJECT_ID=$(gcloud config get-value project)

if [ -z "$PROJECT_ID" ]; then
  echo "No GCP project set. Please run 'gcloud config set project <PROJECT_ID>'"
  exit 1
fi

IMAGE_NAME=$PROJECT_ID/mock-server
REGION=us-central1

echo "Project ID: $PROJECT_ID"
echo "Image: $IMAGE_NAME"
echo "Region: $REGION"

# Build the application
echo "Building the application..."
mvn clean package -DskipTests

# Build and tag Docker image for Cloud Run
echo "Building Docker image..."
docker build --platform linux/amd64 -t $IMAGE_NAME .
if [ $? -ne 0 ]; then
  echo "Docker build failed. Exiting."
  exit 1
fi

# Tag for Cloud Build
echo "Tagging image for Cloud Build..."
docker tag $IMAGE_NAME gcr.io/$PROJECT_ID/mock-server

# Push to Google Container Registry
echo "Pushing image to GCR..."
docker push gcr.io/$PROJECT_ID/mock-server
if [ $? -ne 0 ]; then
  echo "Docker push failed. Exiting."
  exit 1
fi

# Deploy to Cloud Run
echo "Deploying to Cloud Run..."
gcloud run deploy mock-server \
  --image gcr.io/$PROJECT_ID/mock-server \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --port 8080 \
  --memory 1Gi \
  --cpu 1 \
  --timeout 300 \
  --max-instances 10 \
  --no-cpu-throttling \
  --execution-environment gen2

if [ $? -eq 0 ]; then
  echo "Deployment complete. Your app is running on Cloud Run."
  echo "App URL:"
  gcloud run services describe mock-server --region=$REGION --format="value(status.url)"
else
  echo "Deployment failed."
  exit 1
fi
