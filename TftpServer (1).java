import java.net.*;
import java.io.*;
import java.util.*;

class TftpServerWorker extends Thread {
    private DatagramPacket req;
    private static final byte RRQ = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;

    private void sendfile(String filename, DatagramSocket ds, InetAddress address, int serverPort) {
        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */
        String fileName = filename;

        try {
            File file = new File(fileName);
            int serverBlock = 1;
            int clientBlock = 1;
            if ((file.exists() && file.isFile())) {
                System.out.println(file + " exists");
                FileInputStream fis = new FileInputStream(file);
                int Timeout = 1000;
                byte[] buffer = new byte[512];
                int bytesRead;
                ds.setSoTimeout(Timeout);
                while ((bytesRead = fis.read(buffer)) != -1) {
                    while (true) {
                        try {
                            // Create a new 514-byte array with two blank bytes at the start
                            byte[] extendedBuffer = new byte[514];
                            extendedBuffer[0] = 0; // First byte is zero-initialized
                            extendedBuffer[1] = 0; // Second byte is zero-initialized
                            System.arraycopy(buffer, 0, extendedBuffer, 2, bytesRead); // Copy data from buffer
                            extendedBuffer[0] = 2; // DATA
                            extendedBuffer[1] = (byte) clientBlock;
                            // Check to see if blocks are same
                            if (serverBlock == clientBlock) {
                                DatagramPacket sendPacket = new DatagramPacket(extendedBuffer, bytesRead,
                                        address, serverPort);
                                ds.send(sendPacket);
                                System.out.println(
                                        "sent Block " + serverBlock + "  =   " + clientBlock + " "
                                                + extendedBuffer.length);
                                serverBlock++;
                                // // Wait for ack
                            }
                            Timeout += 1000;
                            ds.receive(req);
                            byte[] packetData = req.getData();
                            byte sByte = (byte) (packetData[1]);
                            clientBlock = sByte + 1;
                            break;

                        } catch (SocketTimeoutException e) {
                            // TODO: handle exception
                        }
                    }
                }
                fis.close();
            } else {
                byte[] responseErr = "ACK".getBytes();
                byte[] ACK = new byte[responseErr.length + 1];
                ACK[0] = 4;
                for (int i = 2; i < ACK.length; i++) {
                    ACK[i] = responseErr[i - 1];
                }

                DatagramPacket dpe = new DatagramPacket(ACK, ACK.length,
                        address, serverPort);

                // Send to server
                ds.send(dpe);
                System.out.println("SENT Err PACKET");
            }
        } catch (

        Exception e) {
            // TODO: handle exception
            System.err.println("Exception: " + e);
        }

        return;
    }

    public void run() {
        /*
         * parse the request packet, ensuring that it is a RRQ
         * and then call sendfile
         */
        DatagramPacket p = req;
        try {
            DatagramSocket ds = new DatagramSocket();
            int serverPort = req.getPort();
            String serverAddress = req.getAddress().getHostAddress();
            InetAddress address = InetAddress.getByName(serverAddress);

            byte[] requestData = p.getData();
            int requestLength = p.getLength();

            // Extract the request type (RRQ or ACK)
            String requestFile = new String(requestData, 1, requestLength - 1);

            if (requestData[0] == RRQ) {
                System.out.println("Request File" + " " + requestData[0] + " : RRQ");
            }
            if (requestData[0] == ACK)
                System.out.println("RECIEVED ACK");

            sendfile(requestFile, ds, address, serverPort);

        } catch (SocketException | UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }

    public TftpServerWorker(DatagramPacket req) {
        this.req = req;
    }
}

class TftpServer {
    public void start_server() {
        try {
            DatagramSocket ds = new DatagramSocket(45453);
            System.out.println("TftpServer on port " + ds.getLocalPort());

            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.receive(p);
                System.out.println("Received request.");
                TftpServerWorker worker = new TftpServerWorker(p);
                worker.start();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        return;
    }

    public static void main(String args[]) {
        TftpServer d = new TftpServer();
        d.start_server();
    }
}