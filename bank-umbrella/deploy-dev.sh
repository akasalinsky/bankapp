#!/bin/bash
# Deploy Bank System to Development Environment

set -e

NAMESPACE="dev"
RELEASE_NAME="bank-system"
CHART_PATH="."
VALUES_FILE="values-dev.yaml"

echo "==================================================="
echo "Deploying Bank System to DEVELOPMENT environment"
echo "==================================================="
echo ""

# Create namespace if it doesn't exist
echo "📦 Creating namespace: $NAMESPACE"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Label namespace
kubectl label namespace $NAMESPACE environment=development app=bank-system --overwrite

echo ""
echo "🔨 Updating Helm dependencies..."
helm dependency update $CHART_PATH

echo ""
echo "🚀 Deploying to namespace: $NAMESPACE"
helm upgrade --install $RELEASE_NAME $CHART_PATH \
  --namespace $NAMESPACE \
  --values $VALUES_FILE \
  --wait \
  --timeout 10m \
  --create-namespace

echo ""
echo "✅ Deployment completed!"
echo ""
echo "📊 Checking deployment status..."
kubectl get pods -n $NAMESPACE

echo ""
echo "🔗 Access the application:"
echo "   Front UI: http://bank.dev.local (add to /etc/hosts or use port-forward)"
echo ""
echo "📝 To run Helm tests:"
echo "   helm test $RELEASE_NAME --namespace $NAMESPACE"
echo ""
echo "🔍 To check logs:"
echo "   kubectl logs -n $NAMESPACE -l app.kubernetes.io/name=front-ui"
