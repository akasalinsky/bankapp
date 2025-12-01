@echo off
REM Deploy Bank System to Development Environment (Windows)

set NAMESPACE=dev
set RELEASE_NAME=bank-system
set CHART_PATH=.
set VALUES_FILE=values-dev.yaml

echo ===================================================
echo Deploying Bank System to DEVELOPMENT environment
echo ===================================================
echo.

echo Creating namespace: %NAMESPACE%
kubectl create namespace %NAMESPACE% --dry-run=client -o yaml | kubectl apply -f -
kubectl label namespace %NAMESPACE% environment=development app=bank-system --overwrite

echo.
echo Updating Helm dependencies...
helm dependency update %CHART_PATH%

echo.
echo Deploying to namespace: %NAMESPACE%
helm upgrade --install %RELEASE_NAME% %CHART_PATH% ^
  --namespace %NAMESPACE% ^
  --values %VALUES_FILE% ^
  --wait ^
  --timeout 10m ^
  --create-namespace

echo.
echo Deployment completed!
echo.
echo Checking deployment status...
kubectl get pods -n %NAMESPACE%

echo.
echo Access the application:
echo    Front UI: http://bank.dev.local
echo    Or use port-forward: kubectl port-forward -n %NAMESPACE% svc/bank-system-front-ui 8088:8088
echo.
echo To run Helm tests:
echo    helm test %RELEASE_NAME% --namespace %NAMESPACE%

pause
