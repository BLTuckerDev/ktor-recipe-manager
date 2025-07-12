#!/bin/bash
set -e

# Check if .env.dev exists
if [ ! -f ../.env.dev ]; then
    echo "Error: .env.dev file not found!"
    echo "Copy .env.dev.template to .env.dev and fill in your values"
    exit 1
fi

# Load environment variables and run
export $(cat .env.dev | xargs)
cd ..
./gradlew run