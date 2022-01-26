#!/bin/bash
# Run as sudo

openssl genrsa -out server.key
sudo chmod 400 server.key
openssl req -new -key server.key -days 3650 -out server.crt -x509 -subj '/C=PT/ST=Lisboa/CN=a28.sirs'

chmod 400 pg_hba.conf
chmod 640 pg_hba.conf
chmod 644 postgresql.conf
sudo chown postgres pg_hba.conf postgresql.conf server.key server.crt

sudo cp pg_hba.conf /etc/postgresql/14/main/
sudo cp postgresql.conf /etc/postgresql/14/main/
sudo mv -t /etc/postgresql/14/main/ server.key server.crt
sudo /etc/init.d/postgresql restart