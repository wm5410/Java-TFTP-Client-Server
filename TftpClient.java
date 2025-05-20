import java.net.*;
import java.io.*;
import java.util.*;

public class TftpClient {

    // From args get address (localhost) port and filename
    // to run do java client with parameters local host 45453 and cat.jpg

    // Client will deal with RRQ and ACK packets so that it knows when to send next
    public static void main(String[] args) {
        try {
            String serverAddress = args[0];
            InetAddress address = InetAddress.getByName(serverAddress);
            int serverPort = Integer.parseInt(args[1]);
            String filename = args[2];

            // RRQ
            byte[] file = filename.getBytes(); // byte array
            byte[] RRQ = new byte[file.length + 1];

            RRQ[0] = 1;
            for (int i = 1; i < RRQ.length; i++) {
                RRQ[i] = file[i - 1];
            }

            DatagramSocket ds = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(RRQ, RRQ.length, address, serverPort); // array length addy port

            // Send to server
            ds.send(dp);
            System.out.println("Packet Sent to server: " + RRQ + " " + RRQ.length + " " + address + " " + serverPort
                    + " " + RRQ[0]);

            /////////////////////////////////////////////////////////////////////////////////////////////////

            // Wait for response
            // Recieve DATA or ERROR

            // Create a buffer to receive data from the server
            byte[] receiveData = new byte[514]; // Two extra bytes for the leading zeros
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            int countBlock = 1;

            // Create a FileOutputStream to write the received file
            FileOutputStream fos = new FileOutputStream("received_" + filename);

            // Receive and process data from the server
            while (true) {
                ds.receive(receivePacket);
                // InetAddress clientAddress = receivePacket.getAddress();
                // String clientHost = clientAddress.getHostAddress();
                int clientPort = receivePacket.getPort();

                System.out.println(
                        "packet recieved Block number: "
                                + receivePacket.getData()[1] + "  Length:"
                                + receivePacket.getLength());

                if (receivePacket.getData()[0] == 2) {

                    // File output stream
                    byte[] picture = new byte[512];
                    System.arraycopy(receiveData, 2, picture, 0, picture.length);
                    fos.write(picture);

                    // ACK
                    byte[] responseAck = "ACK".getBytes();
                    byte[] ACK = new byte[responseAck.length + 2];
                    ACK[0] = 3;
                    ACK[1] = (byte) countBlock;
                    for (int i = 2; i < ACK.length; i++) {
                        ACK[i] = responseAck[i - 2];
                    }

                    DatagramPacket dpa = new DatagramPacket(ACK, ACK.length,
                            address, clientPort);

                    // Send to server
                    ds.send(dpa);
                    System.out.println("SENT ACK PACKET block number:" + receivePacket.getData()[1]);
                    countBlock++;

                } // ERROR PACKET
                if (receivePacket.getData()[0] == 4) {
                    System.out.println("Error with file you sent");
                    return;
                } // END OF FILE
                if (receivePacket.getLength() < 512) {
                    System.out.println("done");
                    return;
                }
                ds.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Error " + e);
        }

    }
}
