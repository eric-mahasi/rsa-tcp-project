package com.eric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;

public class Alice {
    public String publicKey;
    public BigInteger d;
    public BigInteger n;

    public static void main(String[] args) throws IOException {
        Alice alice = new Alice();
        alice.generateKey();
        alice.sendPublicKey();
        alice.getCipherText();
    }

    /**
     * Calculate the public key
     */
    public void generateKey() {
        int bitLength = 512;
        int certainty = 1 - ((1 / 2) ^ 50);
        SecureRandom rnd = new SecureRandom();
        BigInteger p = new BigInteger(bitLength, certainty, rnd);
        BigInteger q = new BigInteger(bitLength, certainty, rnd);
        n = p.multiply(q);
        BigInteger one = new BigInteger("1");
        BigInteger phi = p.subtract(one).multiply(q.subtract(one));
        BigInteger e = new BigInteger("65537");
        d = e.modInverse(phi);
        publicKey = e + "," + n;
    }

    /**
     * Send the public key to Bob through a TCP connection
     */
    public void sendPublicKey() {
        try {
            InetAddress hostName = InetAddress.getLocalHost();
            Socket clientSocket = new Socket(hostName, 33333);
            System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());
            DataOutputStream outToBob = new DataOutputStream(clientSocket.getOutputStream());
            outToBob.writeUTF(publicKey);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive the ciphertext from Bob and decrypt it
     */
    public void getCipherText() throws IOException {
        ServerSocket welcomeSocket = new ServerSocket(2345);
        String plainText = "";
        while (true) {
            try {
                Socket connectionSocket = welcomeSocket.accept();
                DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
                String cipher = in.readUTF();
                BigInteger c = new BigInteger(cipher);
                BigInteger m = c.modPow(d, n);
                plainText = String.valueOf(m);
                System.out.println("Plaintext received from Bob: " + plainText);
                break;
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
