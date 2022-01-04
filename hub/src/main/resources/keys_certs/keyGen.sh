hubPrivKeyName="hub_privkey"
hubPubKeyName="hub_pubkey"
hubCertName="hub_cert"
caPrivKeyName="ca_privkey"
caPubKeyName="ca_pubkey"
caCertName="ca_cert"

certInput="PT\nLisbon\nLisbon\nIST SIRS\nA28CT\nafonso.gomes@tecnico.ulisboa.pt\n\n\n"

# Generate CA's key pair
openssl genrsa -out ${caPrivKeyName}.pem
# Create a Certificate Signing Request (CSR) for the CA
echo "$certInput" | openssl req -new -key ${caPrivKeyName}.pem -out tmp/${caCertName}.csr
# Self-sign the certificate
openssl x509 -req -days 365 -in tmp/${caCertName}.csr -signkey ${caPrivKeyName}.pem -out ${caCertName}.pem

# Generate Hub's key pair
openssl genrsa -out ${hubPrivKeyName}.pem
# Create a Certificate Signing Request (CSR) for the Hub
echo "$certInput" | openssl req -new -key ${hubPrivKeyName}.pem -out tmp/${hubCertName}.csr
# Sign the server's CSR with the CA's certificate and private key
openssl x509 -req -days 365 -CA ${caCertName}.pem -CAcreateserial -CAkey ${caPrivKeyName}.pem -in tmp/${hubCertName}.csr -out ${hubCertName}.pem

# Convert CA's private key to pkcs8 format
openssl rsa -in ${caPrivKeyName}.pem -text > tmp/${caPrivKeyName}_comp.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in tmp/${caPrivKeyName}_comp.pem -out ${caPrivKeyName}_pk8.pem -nocrypt
# Convert Hub's private key to pkcs8 format
openssl rsa -in ${hubPrivKeyName}.pem -text > tmp/${hubPrivKeyName}_comp.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in tmp/${hubPrivKeyName}_comp.pem -out ${hubPrivKeyName}_pk8.pem -nocrypt

# Generate CA's public key
openssl rsa -in ${caPrivKeyName}.pem -pubout -outform PEM -out ${caPubKeyName}.pem
# Generate Hub's public key
openssl rsa -in ${hubPrivKeyName}.pem -pubout -outform PEM -out ${hubPubKeyName}.pem

# Clean up
#rm tmp/*
