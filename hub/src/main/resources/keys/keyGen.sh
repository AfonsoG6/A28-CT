# Generate server's key pair
openssl genrsa -out private.key
openssl rsa -in private.key -pubout > public.key
# Create a Certificate Signing Request (CSR) for the server
openssl req -new -key private.key -out server.csr
# Self-sign the certificate
openssl x509 -req -days 365 -in server.csr -signkey private.key -out server.crt

# Convert keys to pkcs8 format
openssl rsa -in private.key -text > private_key_temp.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -in private_key_temp.pem -out private_key.pem -nocrypt
openssl rsa -in private_key_temp.pem -pubout -outform PEM -out public_key.pem
rm private_key_temp.pem
