package com.example.bookstore.security.keycloak;

import java.util.List;
import java.util.UUID;

import com.example.bookstore.exception.KeycloakUserCreationException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

@Service
public class KeycloakAdminService {

    private final Keycloak keycloakAdminClient;
    private final String realm;

    public KeycloakAdminService(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.realm}") String realm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.client-secret}") String clientSecret) {

        this.realm = realm;
        this.keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    public UUID createUser(String username, String email, String password, String realmRole) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(username);
        kcUser.setFirstName(username);
        kcUser.setLastName(username);
        kcUser.setEmail(email);
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        kcUser.setCredentials(List.of(credential));

        RealmResource realmResource = keycloakAdminClient.realm(realm);
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(kcUser);
        try {
            if (response.getStatus() != 201) {
                String body = response.readEntity(String.class);
                throw new KeycloakUserCreationException(
                        "Keycloak rejected user creation (HTTP " + response.getStatus() + "): " + body);
            }

            String location = response.getLocation().getPath();
            String keycloakUserId = location.substring(location.lastIndexOf('/') + 1);

            try {
                assignRealmRole(realmResource, keycloakUserId, realmRole);
            } catch (RuntimeException roleAssignmentFailure) {
                try {
                    realmResource.users().get(keycloakUserId).remove();
                } catch (RuntimeException cleanupFailure) {
                    roleAssignmentFailure.addSuppressed(cleanupFailure);
                }
                throw new KeycloakUserCreationException(
                        "Failed to assign role '" + realmRole + "' after creating Keycloak user - "
                                + "rolled back user creation. Check the admin client's service account has "
                                + "both 'manage-users' AND 'view-realm' under realm-management.",
                        roleAssignmentFailure);
            }

            return UUID.fromString(keycloakUserId);
        } finally {
            response.close();
        }
    }

    private void assignRealmRole(RealmResource realmResource, String keycloakUserId, String roleName) {
        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
        realmResource.users().get(keycloakUserId).roles().realmLevel().add(List.of(role));
    }

    public void deleteUser(UUID keycloakUserId) {
        keycloakAdminClient.realm(realm).users().get(keycloakUserId.toString()).remove();
    }
    public void updateUser(UUID userId, String newUsername, String newEmail) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(newUsername);
        user.setEmail(newEmail);

        keycloakAdminClient.realm(realm).users().get(userId.toString()).update(user);
    }

    public void updatePassword(UUID userId, String newPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);

        keycloakAdminClient.realm(realm).users().get(userId.toString()).resetPassword(credential);
    }
}