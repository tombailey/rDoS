package com.example.http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.http.ListeningState.STARTED;
import static com.example.http.ListeningState.STOPPED;

enum ListeningState {
    INITIAL, STARTED, STOPPED,
}

record HttpRequest(String method, String path, String version) {
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }
}

public class HttpServer {

    private ServerSocket serverSocket;
    private ListeningState listeningState;

    private int threadCount;
    private int port;

    public HttpServer() {
        this(32, 8080);
    }

    /**
     * A basic HTTP server (HTTP/1.1) that tries to service each request (sequentially) as fast as it can
     * @param port the port to listen for http requests on
     */
    public HttpServer(int threadCount, int port) {
        this.threadCount = threadCount;
        this.port = port;
        this.listeningState = ListeningState.INITIAL;
    }

    public synchronized void start() throws IOException {
        if (listeningState == STARTED) {
            throw new IllegalStateException("HttpServer was already started");
        } else {
            serverSocket = new ServerSocket(port);

            //refactor this to increase capacity since threads are doing nothing most of the time
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            while (listeningState != STOPPED) {
                var clientSocket = serverSocket.accept();
                executor.submit(() -> {
                    try {
                        handleRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            listeningState = STARTED;
        }
    }

    public void stop() throws IOException {
        listeningState = STOPPED;
        serverSocket.close();
    }

    public void handleRequest(Socket clientSocket) throws IOException {
        var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        var httpRequest = determineHttpRequest(in);
        if (!httpRequest.getVersion().equalsIgnoreCase("HTTP/1.1")) {
            throw new IOException("Expected HTTP/1.1 request");
        }

        var out = clientSocket.getOutputStream();
        writeStatus(out);
        writeHeader(out);
        writeBody(out);
        out.close();
    }

    public void writeStatus(OutputStream outputStream) throws IOException {
        //ISO_8859_1 according to https://www.w3.org/International/articles/http-charset/index
        outputStream.write("HTTP/1.1 200 OK\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    public void writeHeader(OutputStream outputStream) throws IOException {
        outputStream.write("Server: Boeing AH-64 Apache rDOS/0.1\n".getBytes(StandardCharsets.ISO_8859_1));
        outputStream.write("Content-Type: application/json; charset=utf-8\n".getBytes(StandardCharsets.ISO_8859_1));
        //1 Jan 2000 was a Saturday not a Sunday but intention Easter eggs are fun right?
        outputStream.write("Date: Sun, 1 Jan 2000 00:00:00 GMT\n".getBytes(StandardCharsets.ISO_8859_1));
        outputStream.write("Connection: close\n".getBytes(StandardCharsets.ISO_8859_1));
        outputStream.write("\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    public void writeBody(OutputStream outputStream) throws IOException {
        outputStream.write("{\"done\": true}\n".getBytes(StandardCharsets.UTF_8));
    }


    protected HttpRequest determineHttpRequest(BufferedReader in) throws IOException {
        var methodPathVersion = in.readLine().split(" ");
        if (methodPathVersion.length != 3) {
            throw new IOException("Expected HTTP/1.1 request with method, path and http version");
        }
        return new HttpRequest(methodPathVersion[0], methodPathVersion[1], methodPathVersion[2]);
    }
}
