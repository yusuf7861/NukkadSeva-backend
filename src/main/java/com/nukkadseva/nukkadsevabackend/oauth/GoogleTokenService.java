package com.nukkadseva.nukkadsevabackend.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class GoogleTokenService {

    private final String clientId;

    private final NetHttpTransport netHttpTransport;
    private final GsonFactory jsonFactory;


    public GoogleTokenService(@Value("${google.client.id}") String clientId, NetHttpTransport netHttpTransport, GsonFactory gsonFactory) {
        this.clientId = clientId;
        this.netHttpTransport = netHttpTransport;
        this.jsonFactory = gsonFactory;
    }

    public OAuthUserInfo verifyAndExtract(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(netHttpTransport, jsonFactory)
                    .setAudience(java.util.Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new RuntimeException("Email not verified by Google");
            }

            return new OAuthUserInfo(
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (Exception e) {
            throw new RuntimeException("Google token verification failed" + e.getMessage(), e);
        }
    }
}
