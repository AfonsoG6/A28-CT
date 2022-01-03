privKeyName="hub_privkey"
pubKeyName="hub_pubkey"
certName="hub_cert"

# Generate server's key pair
openssl genrsa -out ${privKeyName}.key
# Create a Certificate Signing Request (CSR) for the server
openssl req -new -key ${privKeyName}.key -out ${certName}.csr
# Self-sign the certificate
openssl x509 -req -days 365 -in ${certName}.csr -signkey ${privKeyName}.key -out ${certName}.crt

# Convert keys to pkcs8 format
openssl rsa -in ${privKeyName}.key -text > ${privKeyName}_temp.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in ${privKeyName}_temp.pem -out ${privKeyName}.pem -nocrypt
openssl rsa -in ${privKeyName}_temp.pem -pubout -outform PEM -out ${pubKeyName}.pem
rm ${privKeyName}_temp.pem
