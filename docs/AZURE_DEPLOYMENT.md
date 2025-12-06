# Deploy task-management-app to Azure

This guide explains two supported ways to deploy the app to Azure:

- Azure Kubernetes Service (AKS) using the provided Kubernetes manifests (recommended)
- Azure Container Apps (ACA) using your container images

The app has two components:
- Frontend: React app served by nginx
- Backend: Spring Boot API (default H2 in-memory DB)

Important behavior:
- The nginx in the frontend proxies any request under `/api` to the Kubernetes Service `backend` on port 8080. This means the frontend can issue relative requests like `/api/auth/...` and avoid CORS issues.
- We changed the frontend auth service to default to a relative path. You can still override the API base URL with `REACT_APP_API_BASE_URL` at build time if needed.

## 1) Deploy to Azure Kubernetes Service (AKS)

Prerequisites:
- Azure CLI installed and logged in: `az login`
- kubectl installed
- (Optional) kustomize installed; otherwise use `kubectl apply -k`

### Step 1: Create a Resource Group
```powershell
az group create -n my-taskapp-rg -l westeurope
```

### Step 2: Create Azure Container Registry (ACR)
```powershell
az acr create -n <ACR_NAME> -g my-taskapp-rg --sku Basic
az acr login -n <ACR_NAME>
```

Replace `<ACR_NAME>` with a unique name (only letters and numbers). After creation, the login server will be `<ACR_NAME>.azurecr.io`.

### Step 3: Build and push images to ACR
You can use ACR Build or local Docker. ACR Build (no local Docker required):
```powershell
# From repo root
az acr build -r <ACR_NAME> -t task-management-app-frontend:<TAG> -f frontend/Dockerfile .
az acr build -r <ACR_NAME> -t task-management-app-backend:<TAG> -f backend/Dockerfile .
```
Or with local Docker, then push:
```powershell
# Build
docker build -t <ACR_NAME>.azurecr.io/task-management-app-frontend:<TAG> -f frontend/Dockerfile .
docker build -t <ACR_NAME>.azurecr.io/task-management-app-backend:<TAG> -f backend/Dockerfile .

# Login & push
az acr login -n <ACR_NAME>
docker push <ACR_NAME>.azurecr.io/task-management-app-frontend:<TAG>
docker push <ACR_NAME>.azurecr.io/task-management-app-backend:<TAG>
```
Choose any `<TAG>` you like, e.g., `v1`.

### Step 4: Create AKS and attach ACR
```powershell
az aks create -g my-taskapp-rg -n my-aks --node-count 1 --attach-acr <ACR_NAME>
az aks get-credentials -g my-taskapp-rg -n my-aks
```

### Step 5: Configure kustomize overlay for Azure
We provide an Azure overlay at `bridge/overlays/azure` that:
- exposes the frontend Service via a public Azure LoadBalancer
- rewrites images to use your ACR registry and tag

Edit `bridge/overlays/azure/kustomization.yaml` and set your values:
```yaml
images:
  - name: task-management-app-frontend
    newName: <ACR_NAME>.azurecr.io/task-management-app-frontend
    newTag: <TAG>
  - name: task-management-app-backend
    newName: <ACR_NAME>.azurecr.io/task-management-app-backend
    newTag: <TAG>
```

If you used a private ACR and did not attach it to AKS, create an imagePullSecret and reference it in the Deployments (not needed if you used `--attach-acr`).

### Step 6: Deploy
```powershell
# Create namespace and all resources
kubectl apply -k bridge/overlays/azure

# Watch for EXTERNAL-IP on the frontend-published Service
kubectl get svc -n task-management-app -w
```

Once `frontend-published` has an `EXTERNAL-IP`, open it in your browser.

Notes:
- The backend uses an in-memory H2 DB by default. For production, use Azure Database for PostgreSQL/MySQL and set appropriate `SPRING_DATASOURCE_*` env vars via a Secret and envFrom/secretKeyRef.
- JWT secret is currently set via an env var in the Deployment manifest. For production, store it in a Kubernetes Secret and reference it.
- The frontend already proxies `/api` to the internal `backend` Service; no extra configuration for the frontend to reach the backend is needed.

## 2) Deploy to Azure Container Apps (ACA)
If you prefer serverless containers without managing Kubernetes:

Prerequisites:
- Azure CLI extension: `az extension add --name containerapp`
- Resource group from earlier
- Container Apps Environment

### Step 1: Build and push images to ACR
Use the same Step 3 from the AKS section.

### Step 2: Create a Container Apps Environment
```powershell
az containerapp env create -g my-taskapp-rg -n taskapp-env -l westeurope
```

### Step 3: Deploy backend container app
```powershell
az containerapp create \
  -g my-taskapp-rg -n taskapp-backend \
  --environment taskapp-env \
  --image <ACR_NAME>.azurecr.io/task-management-app-backend:<TAG> \
  --ingress internal --target-port 8080 \
  --registry-server <ACR_NAME>.azurecr.io \
  --registry-username $(az acr credential show -n <ACR_NAME> --query username -o tsv) \
  --registry-password $(az acr credential show -n <ACR_NAME> --query passwords[0].value -o tsv) \
  --env-vars JWT_SECRET=YourStrongSecret
```

### Step 4: Deploy frontend container app and route /api to backend
For ACA, the simplest approach is to make both public and set the frontend to call the backend via its FQDN. Set `REACT_APP_API_BASE_URL` at build time.

Option A: Build-time env and single public frontend
- Rebuild the frontend image with `REACT_APP_API_BASE_URL` set to the backend URL.
  - If you used CRA build process, set the env during build. Alternatively, make the frontend call relative paths and add an nginx location to proxy to the backend FQDN.

Option B: Use a Dapr/ingress rewrite (advanced)

Basic public frontend deployment:
```powershell
az containerapp create \
  -g my-taskapp-rg -n taskapp-frontend \
  --environment taskapp-env \
  --image <ACR_NAME>.azurecr.io/task-management-app-frontend:<TAG> \
  --ingress external --target-port 3000 \
  --registry-server <ACR_NAME>.azurecr.io \
  --registry-username $(az acr credential show -n <ACR_NAME> --query username -o tsv) \
  --registry-password $(az acr credential show -n <ACR_NAME> --query passwords[0].value -o tsv)
```
Then browse to the frontend URL. If using ACA, ensure your frontend is configured with the backend URL via `REACT_APP_API_BASE_URL`.

## Frontend API base configuration
- By default, the frontend uses a relative API base (`/api`). In AKS this works out-of-the-box due to the nginx proxy in the frontend container.
- To override, set `REACT_APP_API_BASE_URL` during build, e.g.:
```powershell
# Example for local build
$env:REACT_APP_API_BASE_URL = "https://my-frontend.example.com"
yarn build
```

## Troubleshooting
- `frontend-published` has no EXTERNAL-IP: Ensure the AKS cluster has a working LoadBalancer (Azure Standard LB) and you are in a supported region.
- 502/504 from frontend: Check that the `backend` Service is `Ready` and pods are healthy: `kubectl get pods -n task-management-app` and `kubectl logs`.
- CORS errors: With AKS deployment, the frontend proxies to the backend within the cluster, so CORS should not be triggered. If you directly call the backend from the browser using a different domain, configure backend CORS or keep using the proxy.

### Azure OpenAI model deprecation: "Prepare GPT Deployment" failures
If your Azure pipeline or release includes a task named similar to "Prepare GPT Deployment" and you see an error like:

> The model 'Format:OpenAI,Name:gpt-35-turbo,Version:1106' has been deprecated since 04/30/2025 00:00:00.

This application does not use Azure OpenAI directly, so this error originates from your external pipeline configuration. Update the model/deployment your pipeline references to a currently supported model. Recommended choices:
- gpt-4o-mini (cost-efficient, general purpose)
- gpt-4o (higher quality, multimodal)
- gpt-4.1-mini (if available in your region/subscription)

Typical fix steps:
1. In Azure AI Foundry or Azure OpenAI resource, create a new deployment for the chosen model (example for CLI):
   - az cognitiveservices account list -g <RG> to find your resource (or use the portal)
   - az cognitiveservices account deployment create \
     -g <RG> -n <AZURE_OPENAI_RESOURCE_NAME> \
     --deployment-name <NEW_DEPLOYMENT_NAME> \
     --model-format OpenAI \
     --model-name gpt-4o-mini \
     --model-version 2024-08-06
2. Update your pipeline/task variables to reference the new deployment name/model (e.g., variables like AZURE_OPENAI_MODEL, AZURE_OPENAI_DEPLOYMENT_NAME, or the task’s inline settings).
3. Remove or disable references to the deprecated 'gpt-35-turbo' 1106 deployment.
4. Re-run the pipeline stage.

Notes:
- Model availability and exact version strings vary by region. Check Azure documentation for the latest supported models and API versions.
- If your task expects "Format:OpenAI", keep the format as OpenAI for o-series models.
