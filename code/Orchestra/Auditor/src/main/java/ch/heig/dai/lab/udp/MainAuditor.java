package ch.heig.dai.lab.udp;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class MainAuditor {

    public static void main(String[] args) {
        System.out.println("Auditor is running");
        Auditor auditor = new Auditor("239.255.22.5", 9904, 2205, 5);

        // Attend indéfiniment pour éviter que le programme se termine
        try {
            while (true)
                Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}