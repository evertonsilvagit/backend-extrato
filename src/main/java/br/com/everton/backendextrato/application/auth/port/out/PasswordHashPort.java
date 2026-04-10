package br.com.everton.backendextrato.application.auth.port.out;

public interface PasswordHashPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
