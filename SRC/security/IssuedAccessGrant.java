package security;

public class IssuedAccessGrant {
    private final AccessGrant grant;
    private final String invitationCode;

    public IssuedAccessGrant(AccessGrant grant, String invitationCode) {
        this.grant = grant;
        this.invitationCode = invitationCode;
    }

    public AccessGrant getGrant() { return grant; }
    public String getInvitationCode() { return invitationCode; }
}
