package com.example.demo.dto;

/**
 * Request body for both /register and /login.
 *
 * For LOGIN  → only email + password are required.
 * For REGISTER → name, email, password are required; designation is optional.
 */
public class AuthRequest {
    public String name;
    public String email;
    public String password;
    /** Company designation / job title, e.g. "Engineering Lead" */
    public String designation;
}
