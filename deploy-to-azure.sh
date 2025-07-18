#!/bin/bash

# Azure deployment script for Foyer application
# This script deploys the Foyer app to Azure Container Instances

set -e

echo "Starting Azure deployment for Foyer application..."

# Variables
RESOURCE_GROUP="foyer-app-rg"
LOCATION="eastus"
CONTAINER_GROUP_NAME="foyer-app-deployment"
DOCKER_IMAGE="aakefi/foyer-app:latest"

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo "Azure CLI is not installed. Please install it first."
    echo "Visit: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

# Login to Azure (if not already logged in)
echo "Checking Azure login status..."
if ! az account show &> /dev/null; then
    echo "Please login to Azure:"
    az login
fi

# Create resource group
echo "Creating resource group: $RESOURCE_GROUP"
az group create --name $RESOURCE_GROUP --location $LOCATION

# Build and push Docker image to Docker Hub
echo "Building Docker image..."
docker build -t $DOCKER_IMAGE .

echo "Pushing Docker image to Docker Hub..."
docker push $DOCKER_IMAGE

# Deploy to Azure Container Instances
echo "Deploying to Azure Container Instances..."
az container create \
    --resource-group $RESOURCE_GROUP \
    --name $CONTAINER_GROUP_NAME \
    --image $DOCKER_IMAGE \
    --dns-name-label foyer-app-$(date +%s) \
    --ports 8080 \
    --environment-variables \
        SPRING_DATASOURCE_URL="jdbc:mysql://mysql-foyer:3306/foyer?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
        SPRING_DATASOURCE_USERNAME="root" \
        SPRING_DATASOURCE_PASSWORD="root" \
        SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
        SPRING_JPA_SHOW_SQL="true" \
    --cpu 1 \
    --memory 2

# Get the public IP address
echo "Getting public IP address..."
PUBLIC_IP=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_GROUP_NAME --query ipAddress.ip --output tsv)
FQDN=$(az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_GROUP_NAME --query ipAddress.fqdn --output tsv)

echo "Deployment completed successfully!"
echo "Public IP: $PUBLIC_IP"
echo "FQDN: $FQDN"
echo "Application URL: http://$FQDN:8080/Foyer"
echo "Health Check URL: http://$FQDN:8080/Foyer/actuator/health"

# Wait for the container to be ready
echo "Waiting for container to be ready..."
sleep 30

# Test the deployment
echo "Testing the deployment..."
if curl -f "http://$FQDN:8080/Foyer/actuator/health" &> /dev/null; then
    echo "✅ Application is running successfully!"
else
    echo "⚠️  Application might still be starting up. Please check the logs:"
    echo "az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_GROUP_NAME"
fi

echo "Deployment script completed."

