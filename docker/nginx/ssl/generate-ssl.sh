#!/bin/bash

# Generate self-signed SSL certificates for development
# For production, use proper SSL certificates from a CA

echo "Generating self-signed SSL certificates for development..."

# Create private key
openssl genrsa -out talki.key 2048

# Create certificate signing request
openssl req -new -key talki.key -out talki.csr -subj "/C=KR/ST=Seoul/L=Seoul/O=Talki/OU=Development/CN=localhost"

# Create self-signed certificate
openssl x509 -req -days 365 -in talki.csr -signkey talki.key -out talki.crt

# Set proper permissions
chmod 600 talki.key
chmod 644 talki.crt

# Clean up CSR file
rm talki.csr

echo "SSL certificates generated successfully!"
echo "Certificate: talki.crt"
echo "Private Key: talki.key"
echo ""
echo "Note: These are self-signed certificates for development only."
echo "For production, please use proper SSL certificates from a trusted CA."

