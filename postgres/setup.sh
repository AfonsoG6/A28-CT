#!/bin/bash
# run as postgres
# sudo su - postgres

psql -c "DROP DATABASE IF EXISTS sirs;"
psql -c "CREATE DATABASE sirs;"
psql -d sirs -f sirs.sql