#!/bin/bash
# Deploy Bank System to Production Environment

set -e

NAMESPACE="prod"
RELEASE_NAME="bank-system"
CHART_PATH="."
VALUES_FILE="values-prod.yaml"

echo "==================================================="
echo "Deploying Bank System to PRODUCTION environment"
echo "==================================================="
echo ""
echo "⚠️  WARNING: You are about to deploy to PRODUCTION!"
echo ""
read -p "Are you sure you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "❌ Deployment cancelled"
    exit 1
fi

# Create namespace if it doesn't exist
echo ""
echo "📦 Creating namespace: $NAMESPACE"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Label namespace
kubectl label namespace $NAMESPACE environment=production app=bank-system --overwrite

echo ""
echo "🔨 Updating Helm dependencies..."
helm dependency update $CHART_PATH

echo ""
echo "🚀 Deploying to namespace: $NAMESPACE"
helm upgrade --install $RELEASE_NAME $CHART_PATH \
  --namespace $NAMESPACE \
  --values $VALUES_FILE \
  --wait \
  --timeout 15m \
  --create-namespace

echo ""
echo "✅ Deployment completed!"
echo ""
echo "📊 Checking deployment status..."
kubectl get pods -n $NAMESPACE

echo ""
echo "🧪 Running Helm tests..."
helm test $RELEASE_NAME --namespace $NAMESPACE || echo "⚠️  Some tests failed"

echo ""
echo "🔗 Access the application:"
echo "   Front UI: http://bank.prod.local"
echo ""
echo "📈 Monitor the application:"
echo "   kubectl get pods -n $NAMESPACE -w"
echo ""
echo "🔍 To check logs:"
echo "   kubectl logs -n $NAMESPACE -l app.kubernetes.io/name=front-ui"
