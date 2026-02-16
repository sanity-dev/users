package com.sanity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private static final String GOOGLE_TOKEN_VERIFICATION_URL =
            "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";

    /**
     * Valida el token de Google y extrae el correo
     */
    public String validarTokenGoogle(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GOOGLE_TOKEN_VERIFICATION_URL + accessToken;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                String correo = jsonNode.get("email").asText();
                String clientIdFromToken = jsonNode.get("audience").asText();

                // Verifica que el token es para tu aplicación
                if (!clientIdFromToken.equals(googleClientId)) {
                    throw new RuntimeException("Token no válido para esta aplicación");
                }

                log.info("Token de Google validado. Correo: {}", correo);
                return correo;
            }
        } catch (Exception e) {
            log.error("Error validando token de Google: {}", e.getMessage());
            throw new RuntimeException("Token de Google inválido o expirado: " + e.getMessage());
        }

        throw new RuntimeException("No se pudo validar el token de Google");
    }
}