package com.example.http;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class SlowHttpServer extends HttpServer {

    private int byteFlushSleepMs;

    public SlowHttpServer() {
        this(2000);
    }

    public SlowHttpServer(int byteFlushSleepMs) {
        super();
        this.byteFlushSleepMs = byteFlushSleepMs;
    }

    /**
     * A basic HTTP server (HTTP/1.1) that tries to service each request (sequentially) as slow as it can
     * without causing a disconnect. Specifically, we want to send data as slowly as possible while avoiding read
     * timeouts that close the connection because a client didn't receive any data for X seconds
     * @param byteFlushSleepMs how long to sleep for after flushing a single byte of the response
     * @param threadCount how many threads to use to service requests
     * @param port the port to listen for http requests on
     */
    public SlowHttpServer(int byteFlushSleepMs, int threadCount, int port) {
        super(threadCount, port);
        this.byteFlushSleepMs = byteFlushSleepMs;
    }

    @Override
    public void handleRequest(Socket clientSocket) throws IOException {
        var started = new Date();
        var userAgent  = "unknown";
        HttpRequest httpRequest = null;
        try {
            var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            httpRequest = determineHttpRequest(in);
            if (!httpRequest.getVersion().equalsIgnoreCase("HTTP/1.1")) {
                throw new IOException("Expected HTTP/1.1 request");
            }

            var realUserAgent = determineUserAgent(in);
            if (realUserAgent != null) {
                userAgent = realUserAgent;
            }

            var out = clientSocket.getOutputStream();
            writeStatus(out);
            writeHeader(out);
            writeBody(out);
            out.close();
        } finally {
            System.out.println(
                "Held request from " + userAgent +
                " to " + (httpRequest == null ? "(null)" : httpRequest.getPath()) +
                " for " + (new Date().getTime() - started.getTime()) / 1000 + " seconds"
            );
        }
    }

    @Override
    public void writeStatus(OutputStream outputStream) throws IOException {
        writeSlowly(outputStream, "HTTP/1.1 200 OK\n");
    }

    private void writeSlowly(OutputStream outputStream, String content) throws IOException {
        this.writeSlowly(outputStream, content, StandardCharsets.ISO_8859_1);
    }

    private void writeSlowly(OutputStream outputStream, String content, Charset charset) throws IOException {
        var byteContent = content.getBytes(charset);
        for(var index = 0; index < byteContent.length; index++) {
            outputStream.write(byteContent[index]);
            outputStream.flush();

            try{
                Thread.sleep(byteFlushSleepMs);
            } catch (InterruptedException exception) {
                //ignored
            }
        }
    }

    @Override
    public void writeHeader(OutputStream outputStream) throws IOException {
        writeSlowly(outputStream, "Server: Boeing AH-64 Apache rDOS/0.1\n");
        writeSlowly(outputStream, "Content-Type: application/json; charset=utf-8\n");
        writeSlowly(outputStream, "Date: Sun, 1 Jan 2000 00:00:00 GMT\n");
        writeSlowly(outputStream, "Connection: close\n");
        writeSlowly(outputStream, "\n");
    }

    @Override
    public void writeBody(OutputStream outputStream) throws IOException {
        writeSlowly(outputStream, "{\"done\": true}\n");
    }

    private String determineUserAgent(BufferedReader in) throws IOException {
        var line = in.readLine();
        while (line != null) {
            var split = line.toLowerCase().split("user-agent:", 2);
            if (split.length > 1) {
                return split[1].trim();
            } else {
                line = in.readLine();
            }
        }
        return null;
    }
}
