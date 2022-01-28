#!/bin/bash
#Run as sudo

# Policies
sudo iptables -P INPUT DROP
sudo iptables -P OUTPUT DROP
sudo iptables -P FORWARD DROP

# DB <-> Hub
sudo iptables -A INPUT -i enp0s3 -p tcp --dport 5432 -s 192.168.0.20 -j ACCEPT
sudo iptables -A OUTPUT -d 192.168.0.20/32 -o enp0s3 -p tcp -m tcp --sport 5432 -m state --state ESTABLISHED -j ACCEPT

# Loopback
sudo iptables -A INPUT -i lo -j ACCEPT
sudo iptables -A OUTPUT -o lo -j ACCEPT


