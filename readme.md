# Contact Tracing 

This project aims to assist in tracing the spread of a virus during a pandemic. By automatically registering encounters with other users of the app, you are can later be notified in case those users become infected with the virus, and vice-versa. Furthermore, our solution focuses on protecting the users’ data, reducing the risk of sensitive information (such as identity, location and infection status) being leaked. 

\[INSERT GLOSSARY\]

## General Information

Contact Tracing systems can be extremely useful in controlling the spread of a virus in a pandemic scenario. However, due to the nature of the data these systems deal with, they can also constitute a great  
privacy problem for users. 

There are two main pieces of sensitive information that a contact tracing system deals with and stores in some way: a user’s infection status and a user’s contacts (which may include the location and time of those contacts). With our solution, we aspire to mitigate these problems by reducing, as much as possible, the ways in which a malicious or otherwise neglectful actor may attack the system and / or disclose the forementioned information.  

### Built With

* Language: Java
* App ↔ Server Communication: gRPC with SSL/TLS  
* App ↔ App Communication: Bluetooth Low Energy (BLE)
* App Local Storage: SQLite JDBC
* Server-side Storage: PostgreSQL  
* Building and dependencies: Gradle
* Firewall Utility: Iptables
* Tools used in development: Android Studio, IntelliJ IDEA  

---

# Geting Started

Tu run this service we will need 2 virtual machines running [Ubuntu 20.04.3 LTS](https://ubuntu.com/download/desktop)

One of these VMs, which we'll call VMH, will be running the Hub.
The other one, which we'll call VMDB, will be running the PostgreSQL database.

Step-by-step instructions:

1. Create a new Ubuntu (64-bit) VM, call it `VMH`
2. Install [Ubuntu 20.04.3 LTS](https://ubuntu.com/download/desktop)
3. Navigate to **Settings > General > Advanced**, and set both **Shared Clipboard** and **Drag'n'Drop** to `Bidirectional`.
4. Boot the VM and open a terminal.
5. Run the command `sudo apt update && sudo apt upgrade` to update the system.
6. Power off the VM.
7. Create a clone of the VM, with the following configurations:
    * Name: `VMDB`
    * MAC Address Policy: `Generate new MAC addresses for all network adapters`
    * Clone type: `Full clone`
8. Navigate to **VMH > Settings > Network** and enable and configure the following Network Adapters:
    * Adapter 1:
        * Attached to: `Internal Network`
        * Name: `intnet`
    * Adapter 2:
        * Attached to: `NAT`

9. Navigate to **VMDB > Settings > Network** and enable and configure the following Network Adapters:
    * Adapter 1:
        * Attached to: `Internal Network`
        * Name: `intnet`
    * Adapter 2: *(This one is temporary, and will be disabled further ahead)*
        * Attached to: `NAT`
10. We'll configure the VMDB first, so boot the VMDB.
11. Open a terminal and run the commands:

    ```sh
    sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list' &&
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add - &&
    sudo apt update &&
    sudo apt -y install postgresql
    ```

12. Transfer the directory `A28-CT/postgres` to the VMDB through your preferred method. (ex: Drag'n'Drop, Shared Folders, etc.)
13. Change directory to that directory, and change some important permissions for the setup:

    ```sh
    chmod +x *.sh && chmod 777 .
    ```

14. Run ssl.sh with root privileges:

    ```sh
    sudo ./ssl.sh
    ```

15. Change to user "postgres", go to the directory "postgres", and run the script "setup.sh":

    ```sh
    sudo su - postgres
    cd <path to postgres dir>
    ./setup.sh
    ```

16. Edit the file `/etc/network/interfaces` with the following:

    ```sh
    auto enp0s3
    iface enp0s3 inet static
        address 192.168.0.10
        netmask 255.255.255.0
        dns-nameservers 8.8.8.8 8.8.4.4
    ```

17. Restart the VMDB



Hub setup: (Ubuntu LTS VM)
* Install Gradle:
```sh

```
* Install Java 11+:
```sh
sudo apt-get install openjdk-11-jdk
```
* Copy the whole project to the VMH
* Change directory to that directory
* Change Database IP in the configuration file to match the VMDB:
* Execute the shell command:
```sh
gradle :hub:run
```

App Setup: (Physical Android 6+ Devices) Cannot be tested on emulators due to the use of Bluetooth Low Energy (BLE)
* Build the app either on your own machine or in the VMH
* If building on the VMH, and already having done the Hub setup, you only to install the Android SDK and ADB:
```sh

```
* If building on your own machine, you might also need to install Gradle and Java 11+ as described above:
* Connect your Android device(s) to the chosen machine through a USB cable, and run the app:
```sh
adb blabla
```

---

  
# Authors

* Afonso Gomes - IST Master’s student - [Overview](https://github.com/AfonsoG6)
* Miguel Henriques - IST Master’s student - [Overview](https://github.com/miguelchenriques)
* António Martins - IST Master’s student - [Overview](https://github.com/AL-CT)

## Versioning

\[ ¯\\\_(ツ)\_/¯ \]

## License

This project is licensed under the MIT License.  

    MIT License  
      
    Copyright (c) \[2022\] \[A28-CT Team\]  
      
    Permission is hereby granted, free of charge, to any person obtaining a copy  
    of this software and associated documentation files (the "Software"), to deal  
    in the Software without restriction, including without limitation the rights  
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
    copies of the Software, and to permit persons to whom the Software is  
    furnished to do so, subject to the following conditions:  
      
    The above copyright notice and this permission notice shall be included in all  
    copies or substantial portions of the Software.  
      
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  
    SOFTWARE.

##  Acknowledgments

This project was heavily inspired on the original [DP^3T Project](https://github.com/DP-3T/documents). 
Please follow the link above in order to check out their work. 

  
  
  
  
