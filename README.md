# Java TFTP Client & Server

## Overview

This project implements a simplified Trivial File Transfer Protocol (TFTP) in Java using UDP sockets. It consists of:

- A TFTP server that listens for read requests (RRQ) and sends file data reliably with retransmissions.
- A TFTP client that requests files, handles data and error packets, and writes received files to disk.

## Requirements

- Java Development Kit (JDK)
- Linux or equivalent command-line environment
- Terminal tools: `javac`, `java`
- Understanding of UDP sockets (`DatagramSocket`, `DatagramPacket`) and basic threading

## Files

- `TftpServer.java`  
  - Listens on a specified port for RRQ packets.  
  - Spawns a handler thread per request to send DATA packets and handle retransmissions with timeouts.
- `TftpClient.java`  
  - Sends RRQ to the server.  
  - Receives DATA, sends ACKs, and writes file blocks to disk.  
  - Handles ERROR packets and retransmissions.

## Compilation

```bash
javac TftpServer.java TftpClient.java
```

Include any helper `.java` files in the command if present.

## Usage

### Starting the Server

```bash
java TftpServer <port>
```

- `<port>`: UDP port to listen on (choose above 50000 if on shared machines).  
- Example:
  ```
  java TftpServer 6969
  Listening on port 6969
  ```

### Running the Client

```bash
java TftpClient <server-hostname> <port> <filename>
```

- `<server-hostname>`: DNS name or IP of the TFTP server.  
- `<port>`: The server’s listening port.  
- `<filename>`: Name of the file to request.  
- Example:
  ```bash
  java TftpClient localhost 6969 largefile.bin
  Received 'largefile.bin' (size: 131072 bytes)
  ```

## Notes

- Protocol supports RRQ, DATA (512-byte blocks), ACK, and ERROR packets as per RFC 1350 (simplified).
- Retransmits DATA up to 5 times on timeout (1 second) before aborting.
- EOF signaled by DATA packet of <512 bytes, or zero-length block if exact multiple of 512.
- Block numbers wrap at 255 to 0.
- Test interoperability by exchanging files with peers’ implementations.
- Use `md5sum` to verify file integrity:
  ```bash
  md5sum original.bin received.bin
  ```
