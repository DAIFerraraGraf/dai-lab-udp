package ch.heig.dai.lab.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    private  int port;
    private  String ipAddress;
    private  Instrument instrument;
    private final DatagramSocket socket;

    public Musician(String ip, int port, String instrument) throws Exception {
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
                String message = instrument.sound;
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName(ipAddress);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(packet);
                System.out.println("Sent: " + message);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}