package ch.heig.dai.lab.udp;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class MainAuditor {

    private static final int numThreads = 2;
    public static void main(String[] args) {
        System.out.println("Auditor is running");

        Thread worker = new Thread(new Auditor());

        try (var serverSocket = new ServerSocket(port);
             ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    var handler = new RunnableClientHandler(socket, worker);
                    executor.execute(handler);
                } catch (IOException e) {
                    System.err.println("Error client socket: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error server socket: " + e.getMessage());
        }
    }
}