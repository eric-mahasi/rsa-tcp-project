package com.eric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.Scanner;

public class Bob {
    private String publicKey;

    public static void main(String[] args) {
        Bob bob = new Bob();
        bob.getPublicKey();
        bob.sendCipherText();
    }

    /**
     * Receive the public key from Alice through a TCP connection
     */
    public void getPublicKey() {
        while (true) {
            try {
                ServerSocket serverSocket = new ServerSocket(33333);
                serverSocket.setSoTimeout(10000);
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());
                publicKey = in.readUTF();
                server.close();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out.");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Encrypt a message and send it to Alice over a TCP connection
     */
    public void sendCipherText() {
        Scanner scanner = new Scanner(System.in);
        String[] publicKeyArr = publicKey.split(",");
        BigInteger n = new BigInteger(publicKeyArr[1]);
        BigInteger e = new BigInteger("65537");
        System.out.print("Enter the plaintext to send to Alice(must be an integer): ");
        String plainText = scanner.nextLine();
        BigInteger m = new BigInteger(plainText);
        BigInteger c = m.modPow(e, n);
        String cipherText = c.toString();
        while (true) {
            try {
                InetAddress hostName = InetAddress.getLocalHost();
                Socket clientSocket = new Socket(hostName, 2345);
                DataOutputStream outToAlice = new DataOutputStream(clientSocket.getOutputStream());
                outToAlice.writeUTF(cipherText);
                clientSocket.close();
            } catch (ConnectException s) {
                System.out.println("Address already in use.");
                break;
            } catch (IOException x) {
                x.printStackTrace();
                break;
            }
        }
        System.out.println("Plaintext sent to Alice: " + plainText);
    }
}
