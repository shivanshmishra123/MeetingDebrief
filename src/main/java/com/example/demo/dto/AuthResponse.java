package com.example.demo.dto;

/**
 * Response body returned by /register and /login.
 * The frontend stores `token` in localStorage and sends it
 * in the Authorization header on every subsequent request.
 */
public class AuthResponse {
    public String token;
    public String name;
    public String email;
    /** Company designation returned so the frontend can display it in the profile. */
    public String designation;

    public AuthResponse(String token, String name, String email, String designation) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.designation = designation;
    }
}
