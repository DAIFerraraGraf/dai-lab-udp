package ch.heig.dai.lab.udp;


public class MainMusician {
    public static void main(String[] args) {
        try {
            Musician musician = new Musician("239.255.22.5", 9904, args[0]);
            musician.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}