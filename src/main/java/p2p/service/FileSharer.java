package p2p.service;

import p2p.utils.UploadUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap; // CHANGED: Import ConcurrentHashMap for thread safety

public class FileSharer {

    // CHANGED: Use ConcurrentHashMap instead of HashMap for better thread safety
    private final ConcurrentHashMap<Integer, String> availableFiles;

    public FileSharer() {
        // CHANGED: Instantiate the ConcurrentHashMap
        availableFiles = new ConcurrentHashMap<>();
    }

    public int offerFile(String filePath) {
        int port;
        while (true) {
            port = UploadUtils.generateCode();
            // The 'putIfAbsent' method is a thread-safe way to ensure uniqueness
            if (availableFiles.putIfAbsent(port, filePath) == null) {
                return port;
            }
        }
    }

    public void startFileServer(int port) {
        String filePath = availableFiles.get(port);
        if (filePath == null) {
            System.err.println("No file associated with port: " + port);
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serving file '" + new File(filePath).getName() + "' on port " + port + ". Ready for connections...");

            // CHANGED: Added a loop to allow multiple clients to download the file
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                // Start a new thread for each client to handle the file transfer
                new Thread(new FileSenderHandler(clientSocket, filePath)).start();
            }

        } catch (IOException e) {
            System.err.println("Error running file server on port " + port + ": " + e.getMessage());
            // Optional: Remove the file from the map if the server fails to start
            availableFiles.remove(port);
        }
    }

    private static class FileSenderHandler implements Runnable {
        private final Socket clientSocket;
        private final String filePath;

        public FileSenderHandler(Socket clientSocket, String filePath) {
            this.clientSocket = clientSocket;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try (FileInputStream fis = new FileInputStream(filePath);
                 OutputStream oss = clientSocket.getOutputStream()) {

                String filename = new File(filePath).getName();
                System.out.println("Sending file '" + filename + "' to " + clientSocket.getInetAddress());

                // A simple protocol: send filename length, then filename, then file content
                DataOutputStream dos = new DataOutputStream(oss);
                dos.writeUTF(filename); // Send filename

                byte[] buffer = new byte[8192]; // Using a slightly larger buffer
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.flush();
                System.out.println("File '" + filename + "' sent successfully to " + clientSocket.getInetAddress());

            } catch (IOException e) {
                System.err.println("Error sending file to client " + clientSocket.getInetAddress() + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}