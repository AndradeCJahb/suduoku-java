package suduoku;

import org.glassfish.tyrus.server.Server;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SuduokuBackend {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8080, "/", null, WebSocketServer.class);
 
        try {
            server.start();          

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(SuduokuBackend::scraperExecution, 0, 12, TimeUnit.HOURS);

            Thread.currentThread().join(); 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
    
    private static void scraperExecution() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "python", 
                "/app/sudoku_scraper/sudoku_scraper.py"
            );
            pb.inheritIO();
            Process process = pb.start();
            System.out.println("sudoku_scraper.py exit code: " + process.waitFor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}