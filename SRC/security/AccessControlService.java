package security;

import account.User;
import storage.StorageManager;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class AccessControlService {
    private final StorageManager storage;

    public AccessControlService(StorageManager storage) {
        this.storage = storage;
    }

    public IssuedAccessGrant createReadOnlyGrant(User owner, String recipientIdentity,
                                                  UserRole recipientRole,
                                                  EnumSet<ShareScope> scopes,
                                                  Duration validFor) {
        if (owner == null) throw new IllegalArgumentException("Owner is required.");
        if (recipientIdentity == null || recipientIdentity.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient username or email is required.");
        }
        if (recipientRole == null || recipientRole == UserRole.STUDENT || recipientRole == UserRole.ADMIN) {
            throw new IllegalArgumentException("Choose Teacher, Counselor, or College Reviewer.");
        }
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one area to share.");
        }

        long now = Instant.now().toEpochMilli();
        long expires = validFor == null ? 0 : Instant.now().plus(validFor).toEpochMilli();
        String code = ShareTokenUtil.newInvitationCode();
        AccessGrant grant = new AccessGrant(
                UUID.randomUUID().toString(), owner.getUserId(), owner.getUsername(),
                recipientIdentity.trim(), recipientRole, scopes, now, expires,
                ShareTokenUtil.hash(code), false);

        List<AccessGrant> grants = storage.loadAccessGrants(owner.getUsername());
        grants.add(grant);
        storage.saveAccessGrants(owner.getUsername(), grants);
        storage.appendAuditEvent(owner.getUsername(), "SHARE_CREATED", grant.getGrantId(),
                recipientRole.name() + ":" + recipientIdentity.trim());
        return new IssuedAccessGrant(grant, code);
    }

    public void revoke(User owner, String grantId) {
        List<AccessGrant> grants = storage.loadAccessGrants(owner.getUsername());
        for (AccessGrant grant : grants) {
            if (grant.getGrantId().equals(grantId)) {
                grant.revoke();
                storage.saveAccessGrants(owner.getUsername(), grants);
                storage.appendAuditEvent(owner.getUsername(), "SHARE_REVOKED", grantId,
                        grant.getRecipientRole().name() + ":" + grant.getRecipientIdentity());
                return;
            }
        }
    }
}
