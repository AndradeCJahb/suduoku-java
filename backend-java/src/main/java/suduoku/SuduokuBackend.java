package suduoku;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.tyrus.server.Server;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SuduokuBackend {
    private static final Logger logger = LogManager.getLogger(SuduokuBackend.class);

    public static void main(String[] args) {
        String port = System.getenv("PORT");
        int serverPort = (port != null) ? Integer.parseInt(port) : 8080; // Default to 8080 if PORT is not set
        Server server = new Server("0.0.0.0", serverPort, "/", null, WebSocketServer.class);
        logger.info("Starting Suduoku server on port {}", serverPort);
        try {
            server.start();          

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(SuduokuBackend::scraperExecution, 0, 12, TimeUnit.HOURS);

            Thread.currentThread().join(); 
        } catch (Exception e) {
            logger.error("Error starting server: {}", e.getMessage(), e);
        } finally {
            logger.info("Stopping Suduoku server");
            server.stop();
        }
    }
    
    private static void scraperExecution() {
        logger.info("Starting sudoku scraper execution");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "python", 
                "/app/sudoku_scraper/sudoku_scraper.py"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            logger.info("Sudoku scraper completed with exit code: {}", exitCode);
        } catch (Exception e) {
            logger.error("Error executing sudoku scraper: {}", e.getMessage(), e);
        }
    }
}