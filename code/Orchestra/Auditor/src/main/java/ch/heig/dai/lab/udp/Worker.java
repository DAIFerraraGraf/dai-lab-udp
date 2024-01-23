import java.net.DatagramPacket;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Worker implements Runnable {
    private DatagramPacket packet;

    public Worker(DatagramPacket packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        // Process the packet here
        String message = new String(packet.getData(), 0, packet.getLength(), UTF_8);
        System.out.println("Processed message: " + message + " from " + packet.getAddress() + ", port " + packet.getPort());
    }
}