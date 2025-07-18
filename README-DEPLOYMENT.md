# Foyer Application Deployment Guide

This guide provides instructions for deploying the Foyer application to Azure Cloud using Docker containers.

## Prerequisites

1. **Docker and Docker Compose** installed on your local machine
2. **Azure CLI** installed and configured
3. **Docker Hub account** (aakefi) with push permissions
4. **Azure subscription** with Container Instances service enabled

## Local Development Setup

### 1. Clone the Repository
```bash
git clone https://github.com/AhmedAmineKefi/AhmedAmine-Kefi-Foyer.git
cd AhmedAmine-Kefi-Foyer
git checkout 2ALINFO2OT
```

### 2. Build and Run Locally
```bash
# Build the Docker image
docker-compose build

# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs foyer-app
```

### 3. Access the Application
- **Application URL**: http://localhost:8086/Foyer
- **Health Check**: http://localhost:8086/Foyer/actuator/health
- **Grafana Dashboard**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Jaeger Tracing**: http://localhost:16686

## Troubleshooting Local Issues

### Issue 1: Application Not Accessible
**Problem**: `curl: (56) Recv failure: Connection reset by peer`

**Solution**: 
1. Check if the application is binding to `0.0.0.0` instead of `127.0.0.1`
2. Verify database connection credentials match MySQL container settings
3. Ensure MySQL dialect is configured in `application.properties`

### Issue 2: Database Connection Failed
**Problem**: `Access denied for user 'foyer_user'@'172.19.0.4'`

**Solution**:
1. Update database credentials in `docker-compose.yml`:
   ```yaml
   - SPRING_DATASOURCE_USERNAME=root
   - SPRING_DATASOURCE_PASSWORD=root
   ```
2. Ensure MySQL service name matches in the connection URL

### Issue 3: Container Name Invalid
**Problem**: `Invalid container name (aakefi/foyer-app)`

**Solution**:
```yaml
container_name: foyer-app  # Use simple name, not Docker Hub format
```

## Azure Deployment

### Method 1: Automated Script (Recommended)

1. **Login to Docker Hub**:
```bash
docker login
# Username: aakefi
# Password: .21090990Aa
```

2. **Login to Azure**:
```bash
az login
# Use credentials: ahmedamine.kefi@esprit.tn/.21090990Aa
```

3. **Run Deployment Script**:
```bash
./deploy-to-azure.sh
```

### Method 2: Manual Deployment

1. **Build and Push Docker Image**:
```bash
# Build the image
docker build -t aakefi/foyer-app:latest .

# Push to Docker Hub
docker push aakefi/foyer-app:latest
```

2. **Create Azure Resource Group**:
```bash
az group create --name foyer-app-rg --location eastus
```

3. **Deploy to Azure Container Instances**:
```bash
az container create \
    --resource-group foyer-app-rg \
    --name foyer-app-deployment \
    --image aakefi/foyer-app:latest \
    --dns-name-label foyer-app-$(date +%s) \
    --ports 8080 \
    --environment-variables \
        SPRING_DATASOURCE_URL="jdbc:mysql://mysql-foyer:3306/foyer?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
        SPRING_DATASOURCE_USERNAME="root" \
        SPRING_DATASOURCE_PASSWORD="root" \
        SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
    --cpu 1 \
    --memory 2
```

4. **Get Public URL**:
```bash
az container show --resource-group foyer-app-rg --name foyer-app-deployment --query ipAddress.fqdn --output tsv
```

### Method 3: Production Docker Compose

For production deployment with external database:

```bash
# Copy environment file
cp .env.example .env

# Edit .env with production values
nano .env

# Deploy with production configuration
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoring and Maintenance

### Check Application Status
```bash
# Azure Container Instances
az container show --resource-group foyer-app-rg --name foyer-app-deployment

# View logs
az container logs --resource-group foyer-app-rg --name foyer-app-deployment

# Local Docker Compose
docker-compose ps
docker-compose logs foyer-app
```

### Health Checks
- **Application Health**: `http://your-app-url:8080/Foyer/actuator/health`
- **Database Connection**: Check application logs for connection errors

### Scaling and Updates

1. **Update Application**:
```bash
# Build new image
docker build -t aakefi/foyer-app:v2 .

# Push to registry
docker push aakefi/foyer-app:v2

# Update Azure deployment
az container create --resource-group foyer-app-rg --name foyer-app-deployment --image aakefi/foyer-app:v2 [other-options]
```

2. **Scale Resources**:
```bash
# Update CPU and memory in deployment script or Azure portal
--cpu 2 --memory 4
```

## Security Considerations

1. **Environment Variables**: Never commit sensitive data to version control
2. **Database Passwords**: Use strong passwords in production
3. **Network Security**: Configure Azure Network Security Groups as needed
4. **SSL/TLS**: Consider adding HTTPS termination for production

## Cost Optimization

1. **Resource Sizing**: Start with minimal resources and scale as needed
2. **Auto-shutdown**: Consider implementing auto-shutdown for development environments
3. **Monitoring**: Use Azure Cost Management to track expenses

## Support and Troubleshooting

### Common Issues

1. **Container Won't Start**: Check environment variables and image availability
2. **Database Connection**: Verify network connectivity and credentials
3. **Port Access**: Ensure security groups allow traffic on required ports

### Getting Help

1. Check application logs: `az container logs --resource-group foyer-app-rg --name foyer-app-deployment`
2. Verify Azure resource status in Azure Portal
3. Test connectivity: `curl -v http://your-app-url:8080/Foyer/actuator/health`

## Files Overview

- `docker-compose.yml`: Development configuration with monitoring stack
- `docker-compose.prod.yml`: Production configuration (simplified)
- `deploy-to-azure.sh`: Automated Azure deployment script
- `azure-deployment.yml`: Azure Container Instances template
- `.env.example`: Environment variables template
- `Dockerfile`: Application container definition

