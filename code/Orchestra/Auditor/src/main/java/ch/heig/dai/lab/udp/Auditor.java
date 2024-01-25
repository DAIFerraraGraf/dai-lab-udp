package ch.heig.dai.lab.udp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum InstrumentAuditor{
    piano("ti-ta-ti"),
    trumpet("pouet"),
    flute("trulu"),
    violin("gzi-gzi"),
    drum("boum-boum");

    public String sound;

    private InstrumentAuditor(String sound){
        this.sound = sound;
    }
    public static String getInstrument(String sound){
        for (InstrumentAuditor instrument : InstrumentAuditor.values()) {
            if (instrument.sound.equalsIgnoreCase(sound)) {
                return instrument.name();
            }
        }
        return null;
    }
}

public class Auditor {
    private static final int UDP_PORT = 9904;
    private static final String UDP_IP = "239.255.22.5";
    private static final int TCP_PORT = 2205;
    private static final int TIMEOUT_SECONDS = 5;

//    private ConcurrentLinkedQueue<MusicianInfo> activeMusicians = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String, MusicianInfo> activeMusicians = new ConcurrentHashMap<>();

    public Auditor() {

        // Create a thread pool with two threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Submit tasks for UDP and TCP listeners
        try{
            executorService.submit(this::startUdpListener);
            executorService.submit(this::startTcpListener).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startUdpListener() {
        try (MulticastSocket socket = new MulticastSocket(UDP_PORT)) {
            InetAddress group = InetAddress.getByName(UDP_IP);
            socket.joinGroup(new InetSocketAddress(group, UDP_PORT), NetworkInterface.getByName("eth0"));

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String data = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received UDP message: " + data);
                processUdpMessage(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processUdpMessage(String message) {
        Gson gson = new Gson();
        MusicianInfo musicianInfo = gson.fromJson(message, MusicianInfo.class);

        musicianInfo.setInstrument(InstrumentAuditor.getInstrument(musicianInfo.getSound()));
        // Update musician's last activity timestamp
        musicianInfo.setLastActivity(System.currentTimeMillis());

        // Update or add musician to the active musicians list

        activeMusicians.put(musicianInfo.getUuid(), musicianInfo);
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
//            List<MusicianInfo> filteredMusicians = new ArrayList<>(activeMusicians);

            for(String key : activeMusicians.keySet()) {
                if((System.currentTimeMillis() - activeMusicians.get(key).getLastActivity()) > TIMEOUT_SECONDS * 1000) {
                    activeMusicians.remove(key);
                }
            }

            List<MusicianInfo> filteredMusicians = new ArrayList<>(activeMusicians.values());

            // Send JSON payload to the client
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
            String payload = gson.toJson(filteredMusicians);
            System.out.println("Sending TCP message: " + payload);
            clientSocket.getOutputStream().write(payload.getBytes());

            System.out.println("Data send over TCP");
            clientSocket.getOutputStream().flush();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class MusicianInfo {
    @Expose
    private String uuid;
    private String sound;
    @Expose
    private String instrument;
    @Expose
    private long lastActivity;

    public MusicianInfo(String uuid, String sound) {
        this.uuid = uuid;
        this.sound = sound;
    }
    public String getInstrument() {
        return instrument;
    }
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSound() {

        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
}