package ch.heig.dai.lab.udp;

import java.net.*;
import java.io.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Auditor implements Runnable {
    final private Socket socket;

    final private Worker worker;

    public Auditor(Socket socket, Worker worker) {
        this.socket = socket;
        this.worker = worker;
    }

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {

            var group_address = new InetSocketAddress(IPADDRESS, PORT);
            NetworkInterface netif = NetworkInterface.getByName("eth0");
            socket.joinGroup(group_address, netif);

            while (true) {
                byte[] buffer = new byte[1024];
                var packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), UTF_8);
                System.out.println("Received message: " + message + " from " + packet.getAddress() + ", port " + packet.getPort());
            }


            socket.leaveGroup(group_address, netif);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}