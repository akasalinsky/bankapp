@echo off
REM Deploy Bank System to Production Environment (Windows)

set NAMESPACE=prod
set RELEASE_NAME=bank-system
set CHART_PATH=.
set VALUES_FILE=values-prod.yaml

echo ===================================================
echo Deploying Bank System to PRODUCTION environment
echo ===================================================
echo.
echo WARNING: You are about to deploy to PRODUCTION!
echo.
set /p CONFIRM="Are you sure you want to continue? (yes/no): "

if not "%CONFIRM%"=="yes" (
    echo Deployment cancelled
    pause
    exit /b 1
)

echo.
echo Creating namespace: %NAMESPACE%
kubectl create namespace %NAMESPACE% --dry-run=client -o yaml | kubectl apply -f -
kubectl label namespace %NAMESPACE% environment=production app=bank-system --overwrite

echo.
echo Updating Helm dependencies...
helm dependency update %CHART_PATH%

echo.
echo Deploying to namespace: %NAMESPACE%
helm upgrade --install %RELEASE_NAME% %CHART_PATH% ^
  --namespace %NAMESPACE% ^
  --values %VALUES_FILE% ^
  --wait ^
  --timeout 15m ^
  --create-namespace

echo.
echo Deployment completed!
echo.
echo Checking deployment status...
kubectl get pods -n %NAMESPACE%

echo.
echo Running Helm tests...
helm test %RELEASE_NAME% --namespace %NAMESPACE%

echo.
echo Access the application:
echo    Front UI: http://bank.prod.local

pause
