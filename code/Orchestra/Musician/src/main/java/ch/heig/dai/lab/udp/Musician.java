package ch.heig.dai.lab.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

enum Instrument{
    piano("ti-ta-ti"),
    trumpet("pouet"),
    flute("trulu"),
    violin("gzi-gzi"),
    drum("boum-boum");

    public String sound;

    private Instrument(String sound){
        this.sound = sound;
    }
}

public class Musician {
    private int port;
    private String ipAddress;
    private Instrument instrument;
    private final DatagramSocket socket;
    private final UUID uuid;

    public Musician(String ip, int port, String instrument) throws Exception {
        this.uuid = UUID.randomUUID();

        try {
            this.instrument = Instrument.valueOf(instrument);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid instrument: " + instrument);
        }

        this.socket = new DatagramSocket();
        this.ipAddress = ip;
        this.port = port;
    }

    public void run() {
        while (true) {
            try {
                // Création du payload JSON
                String payload = String.format(
                        "{\"uuid\":\"%s\",\"sound\":\"%s\"}",
                        uuid.toString(), instrument.sound
                );

                byte[] buffer = payload.getBytes();

                InetAddress group = InetAddress.getByName(ipAddress);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(packet);

                System.out.println("Sent: " + payload);
                Thread.sleep(1000); // Pause de 1 seconde
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}