package ch.heig.dai.lab.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

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
    private  Instrument instrument;
    private final DatagramSocket socket;

    public Musician() throws Exception {
        Random random = new Random();
        this.instrument = Instrument.values()[random.nextInt(Instrument.values().length)];
        this.socket = new DatagramSocket();
    }

    public void run() {
        while (true) {
            try {
                String message = instrument.sound;
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName("239.255.22.5");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 9904);
                socket.send(packet);

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}