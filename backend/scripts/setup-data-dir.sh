#!/bin/bash
# Script to create data processing directory on Linux/macOS

DATA_DIR="/var/log/applications/API/dataprocessing"

echo "Creating data directory: $DATA_DIR"

if [ ! -d "$DATA_DIR" ]; then
    sudo mkdir -p "$DATA_DIR" 2>/dev/null
    if [ -d "$DATA_DIR" ]; then
        echo "Successfully created directory: $DATA_DIR"
        sudo chmod 755 "$DATA_DIR"
        sudo chown $USER:$USER "$DATA_DIR" 2>/dev/null || true
    else
        echo "Failed to create directory: $DATA_DIR"
        echo "Please run with sudo or use a different path"
        exit 1
    fi
else
    echo "Directory already exists: $DATA_DIR"
fi

echo "Setting permissions..."
sudo chmod 755 "$DATA_DIR" 2>/dev/null || true

echo "Data directory setup complete!"
echo ""
echo "You can now start the Student Data Pipeline backend."
echo "Set environment variable DATAPATH_BASE to use a different path."