package com.auth.common.mail;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class ResendMailService implements MailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "resend.api.key")
    String apiKey;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> payload = new HashMap<>();
            payload.put("from", "Auth System <onboarding@resend.dev>");
            payload.put("to", List.of(to));
            payload.put("subject", subject);
            payload.put("text", body);

            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException(
                        "Resend mail failed: " + response.statusCode() + " " + response.body()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Resend", e);
        }
    }

}
