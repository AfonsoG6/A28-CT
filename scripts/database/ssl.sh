#!/bin/bash
# Run as sudo

# Create the CA
openssl genrsa -out rootCA.key 4096
openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 1024 -out rootCA.crt -subj "/C=PT"

# Create server key
openssl genrsa -out server.key 2048
# Generate csr
openssl req -new -sha256 -key server.key -subj '/C=PT/ST=Lisboa/CN=192.168.0.10' -out server.csr
# Generate the certificate
openssl x509 -req -in server.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out server.crt -days 500 -sha256

sudo chmod 400 server.key rootCA.key
chmod +r rootCA.crt
chmod 640 pg_hba.conf
chmod 644 postgresql.conf
sudo chown postgres pg_hba.conf postgresql.conf server.key server.crt

sudo cp pg_hba.conf /etc/postgresql/14/main/
sudo cp postgresql.conf /etc/postgresql/14/main/
sudo cp rootCA.crt /etc/postgresql/14/main/
sudo mv -t /etc/postgresql/14/main/ server.key server.crt
sudo /etc/init.d/postgresql restart