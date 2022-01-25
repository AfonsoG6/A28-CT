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

\[Add literally everything\]  

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

  
  
  
  
