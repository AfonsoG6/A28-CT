#!/bin/bash
# Run as sudo

# Policies
sudo iptables -P INPUT DROP
sudo iptables -P OUTPUT DROP
sudo iptables -P FORWARD DROP

# App <-> Hub
sudo iptables -A INPUT -p tcp --dport 29292 -j ACCEPT -i enp0s8
sudo iptables -A OUTPUT -m state --state ESTABLISHED -j ACCEPT

# Hub <-> DB
sudo iptables -A OUTPUT -o enp0s3 -p tcp --dport 5432 -d 192.168.0.10/24 -j ACCEPT
sudo iptables -A INPUT -m state --state ESTABLISHED -j ACCEPT

# Loopback
sudo iptables -A INPUT -i lo -j ACCEPT
sudo iptables -A OUTPUT -o lo -j ACCEPT