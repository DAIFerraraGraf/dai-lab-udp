package ch.heig.dai.lab.udp;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Auditor {
    private static final int UDP_PORT = 9904;
    private static final String UDP_IP = "239.255.22.5";
    private static final int TCP_PORT = 2205;
    private static final int TIMEOUT_SECONDS = 5;

    private List<MusicianInfo> activeMusicians = new ArrayList<>();

    public Auditor() {

        // Create a thread pool with two threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Submit tasks for UDP and TCP listeners
        executorService.submit(this::startUdpListener);
        executorService.submit(this::startTcpListener);
    }

    private void startUdpListener() {
        try (MulticastSocket socket = new MulticastSocket(UDP_PORT)) {
            InetAddress group = InetAddress.getByName(UDP_IP);
            socket.joinGroup(new InetSocketAddress(group, UDP_PORT), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String data = new String(packet.getData(), 0, packet.getLength());
                processUdpMessage(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processUdpMessage(String message) {
        Gson gson = new Gson();
        MusicianInfo musicianInfo = gson.fromJson(message, MusicianInfo.class);

        // Update musician's last activity timestamp
        musicianInfo.setLastActivity(System.currentTimeMillis());

        // Update or add musician to the active musicians list
        activeMusicians.removeIf(m -> m.getUuid().equals(musicianInfo.getUuid()));
        activeMusicians.add(musicianInfo);
    }

    private void startTcpListener() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleTcpConnection(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTcpConnection(Socket clientSocket) {
        try {
            // Filter out inactive musicians
            List<MusicianInfo> filteredMusicians = new ArrayList<>(activeMusicians);
            filteredMusicians.removeIf(m -> (System.currentTimeMillis() - m.getLastActivity()) > TIMEOUT_SECONDS * 1000);

            // Send JSON payload to the client
            Gson gson = new Gson();
            String payload = gson.toJson(filteredMusicians);
            clientSocket.getOutputStream().write(payload.getBytes());

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class MusicianInfo {
    private String uuid;
    private String instrument;
    private long lastActivity;

    // Constructors, getters, setters, etc.

    // Example constructor:
    public MusicianInfo(String uuid, String instrument, long lastActivity) {
        this.uuid = uuid;
        this.instrument = instrument;
        this.lastActivity = lastActivity;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
}
