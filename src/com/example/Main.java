package com.example;

import com.example.http.HttpServer;
import com.example.http.SlowHttpServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //a relatively sane HttpServer that tries to service each request as far as possible
//        var server = new HttpServer();

        //a deliberately slow HttpServer that tries to service each request as slow as possible
        var server = new SlowHttpServer(2000, 100, 8080);

        server.start();
    }
}
