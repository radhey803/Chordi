package p2p;

import p2p.controller.FileController;
import java.io.IOException;

/**
 * PeerLink - P2P File Sharing Application
 */
public class App {
    public static void main(String[] args) {
        try {
            // Get port from environment variable for Render, default to 8080 for local dev
            String portStr = System.getenv("PORT");
            int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 8080;

            // Start the API server on the configured port
            FileController fileController = new FileController(port);
            fileController.start();
            
            System.out.println("PeerLink server started on port " + port);
            System.out.println("UI available at http://localhost:3000");

            // Add a shutdown hook to gracefully stop the server
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                fileController.stop();
            }));

            // Keep the main thread alive to let the server run
            Thread.currentThread().join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
