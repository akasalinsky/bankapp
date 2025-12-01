@echo off
echo Building Docker images for Bank App services...

docker build -t bank-app/accounts-service:v1 ./accounts-service
docker build -t bank-app/cash-service:v1 ./cash-service
docker build -t bank-app/transfer-service:v1 ./transfer-service
docker build -t bank-app/exchange-service:v1 ./exchange-service
docker build -t bank-app/exchange-generator:v1 ./exchange-generator
docker build -t bank-app/blocker-service:v1 ./blocker-service
docker build -t bank-app/notification-service:v1 ./notification-service

echo All images built successfully!
pause
