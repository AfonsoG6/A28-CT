hubPrivKeyName="hub_privkey"
hubPubKeyName="hub_pubkey"
hubCertName="hub_cert"

certInput="PT\nLisbon\nLisbon\nIST SIRS\nA28CT\nafonso.gomes@tecnico.ulisboa.pt\n\n\n"

# Generate Hub's key pair
openssl genrsa -out ${hubPrivKeyName}.pem
# Create a Certificate Signing Request (CSR) for the Hub
echo "$certInput" | openssl req -new -key ${hubPrivKeyName}.pem -out tmp/${hubCertName}.csr
# Sign the server's CSR with the CA's certificate and private key
openssl x509 -req -days 365 -in tmp/${hubCertName}.csr -signkey ${hubPrivKeyName}.pem -out ${hubCertName}.pem

# Convert Hub's private key to pkcs8 format
openssl rsa -in ${hubPrivKeyName}.pem -text > tmp/${hubPrivKeyName}_comp.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in tmp/${hubPrivKeyName}_comp.pem -out ${hubPrivKeyName}_pk8.pem -nocrypt

# Generate Hub's public key
openssl rsa -in ${hubPrivKeyName}.pem -pubout -outform PEM -out ${hubPubKeyName}.pem

# Clean up
#rm tmp/*
