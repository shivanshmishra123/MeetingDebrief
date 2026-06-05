package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint — publicly accessible (no JWT required).
 *
 * Used by UptimeRobot / cron-job.org to ping this service every 14 minutes
 * so Render's free tier never spins it down.
 *
 * SecurityConfig permits GET /healthz without authentication.
 */
@RestController
public class HealthController {

    @GetMapping("/healthz")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
