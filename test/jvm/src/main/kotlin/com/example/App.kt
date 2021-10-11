package com.example;

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main(args : Array<String>) {
    println("Sending requests, this might take a while")

    val host = System.getenv("HOST") ?: "localhost:8080"

    val executor = Executors.newFixedThreadPool(16)
    executor.execute {
        time("okhttp") {
            OkHttpClient.Builder().build()
                .newCall(
                    Request.Builder()
                        .url("http://${host}/")
                        .post("".toRequestBody())
                        .build()
                )
                .execute()
        }
    }
    executor.execute {
        time("okhttp with timeout") {
            OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .build()
                .newCall(
                    Request.Builder()
                        .url("http://${host}/")
                        .post("".toRequestBody())
                        .build()
                )
                .execute()
        }
    }
}

fun time(tag: String, func: () -> Unit) {
    val before = Date().time
    try {
        func()
        println("$tag request took ${(Date().time - before) / 1000} seconds")
    } catch (exception: Exception) {
        println("$tag failed after ${(Date().time - before) / 1000} seconds")
    }
}
