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
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                fileController.stop();
            }));
            
            System.out.println("Press Enter to stop the server");
            System.in.read();
            
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
