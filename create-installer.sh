#!/bin/bash

# Build and package the application for Windows
echo "Building and packaging Game Collection app for Windows..."

# Clean and build the application
mvn clean package

# Create the runtime image using jlink
mvn javafx:jlink

# Create the Windows installer
mvn jpackage:jpackage

echo "Build complete! Windows installer can be found in target/installer directory."