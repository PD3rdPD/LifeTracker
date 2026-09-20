package security;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

public class AccessGrant {
    private final String grantId;
    private final String ownerUserId;
    private final String ownerUsername;
    private final String recipientIdentity;
    private final UserRole recipientRole;
    private final EnumSet<ShareScope> scopes;
    private final long createdAtEpochMillis;
    private final long expiresAtEpochMillis;
    private final String invitationTokenHash;
    private boolean revoked;

    public AccessGrant(String grantId, String ownerUserId, String ownerUsername,
                       String recipientIdentity, UserRole recipientRole,
                       Set<ShareScope> scopes, long createdAtEpochMillis,
                       long expiresAtEpochMillis, String invitationTokenHash,
                       boolean revoked) {
        this.grantId = grantId;
        this.ownerUserId = ownerUserId;
        this.ownerUsername = ownerUsername;
        this.recipientIdentity = recipientIdentity;
        this.recipientRole = recipientRole;
        this.scopes = scopes == null || scopes.isEmpty()
                ? EnumSet.noneOf(ShareScope.class)
                : EnumSet.copyOf(scopes);
        this.createdAtEpochMillis = createdAtEpochMillis;
        this.expiresAtEpochMillis = expiresAtEpochMillis;
        this.invitationTokenHash = invitationTokenHash;
        this.revoked = revoked;
    }

    public String getGrantId() { return grantId; }
    public String getOwnerUserId() { return ownerUserId; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getRecipientIdentity() { return recipientIdentity; }
    public UserRole getRecipientRole() { return recipientRole; }
    public EnumSet<ShareScope> getScopes() { return EnumSet.copyOf(scopes); }
    public long getCreatedAtEpochMillis() { return createdAtEpochMillis; }
    public long getExpiresAtEpochMillis() { return expiresAtEpochMillis; }
    public String getInvitationTokenHash() { return invitationTokenHash; }
    public boolean isRevoked() { return revoked; }
    public void revoke() { revoked = true; }

    public boolean isExpired() {
        return expiresAtEpochMillis > 0 && Instant.now().toEpochMilli() > expiresAtEpochMillis;
    }

    public boolean allows(ShareScope scope) {
        return !revoked && !isExpired() && scopes.contains(scope);
    }
}
