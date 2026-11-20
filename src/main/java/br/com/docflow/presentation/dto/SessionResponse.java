package br.com.docflow.presentation.dto;

/** Estado da sessão para o frontend: se há login e com qual usuário. */
public record SessionResponse(boolean authenticated, String username) {

    public static SessionResponse authenticated(String username) {
        return new SessionResponse(true, username);
    }

    public static SessionResponse anonymous() {
        return new SessionResponse(false, null);
    }
}
