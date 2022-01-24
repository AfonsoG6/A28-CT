#!/bin/bash
# sudo passwd postgres      -> To setup postgres password
# su - postgres
# cd project

psql -c "DROP DATABASE IF EXISTS sirs;"
psql -c "CREATE DATABASE sirs;"
psql -d sirs -f sirs.sql