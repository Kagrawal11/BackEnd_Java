package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

// Temporary diagnostic endpoint to check outbound egress from Render - remove after use.
@RestController
@RequestMapping("/api/netdiag")
public class NetDiagController {

    @GetMapping
    public Map<String, String> check() {
        Map<String, String> results = new LinkedHashMap<>();
        String[] hosts = {
                "https://api.razorpay.com/v1/orders",
                "https://www.google.com",
                "https://smtp.gmail.com",
                "https://httpbin.org/get"
        };
        for (String host : hosts) {
            long start = System.currentTimeMillis();
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(host).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                long ms = System.currentTimeMillis() - start;
                results.put(host, "OK status=" + code + " in " + ms + "ms");
            } catch (Exception e) {
                long ms = System.currentTimeMillis() - start;
                results.put(host, "FAILED after " + ms + "ms: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        return results;
    }
}
