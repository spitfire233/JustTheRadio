package com.justtheradio.utils.security;

import static com.justtheradio.utils.constant.NetworkConstants.MAX_URL_LENGTH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.justtheradio.utils.security.SecurityUtils;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Unit tests for {@link SecurityUtils SecurityUtils}.
 * The hosts used are IP literals or names that
 * are rejected by the class itself, so no DNS lookup is ever made. The checks run against the
 * real URL and address validators.
 */
public class SecurityUtilsUnitTest {

    /**
     * Builds an address from a literal.
     *
     * @param literal The IP address as a string
     * @return The address
     */
    private static InetAddress address(String literal) {
        try {
            return InetAddress.getByName(literal);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(literal, e);
        }
    }

    /**
     * Asserts that every URL is accepted, naming the URL that fails.
     *
     * @param mustBeHttps The flag passed to the validation
     * @param urls        The URLs that must be valid
     */
    private static void assertAllValid(boolean mustBeHttps, String... urls) {
        for (String url : urls)
            assertTrue("expected valid: " + url, SecurityUtils.isValidUrl(url, mustBeHttps));
    }

    /**
     * Asserts that every URL is rejected, naming the URL that fails.
     *
     * @param mustBeHttps The flag passed to the validation
     * @param urls        The URLs that must be invalid
     */
    private static void assertAllInvalid(boolean mustBeHttps, String... urls) {
        for (String url : urls)
            assertFalse("expected invalid: " + url, SecurityUtils.isValidUrl(url, mustBeHttps));
    }

    /**
     * Asserts that every host is considered local, naming the host that fails.
     *
     * @param hosts The hosts that must be local
     */
    private static void assertAllLocal(String... hosts) {
        for (String host : hosts)
            assertTrue("expected local: " + host, SecurityUtils.isLocalHost(host));
    }

    /**
     * Asserts that no host is considered local, naming the host that fails.
     *
     * @param hosts The hosts that must not be local
     */
    private static void assertNoneLocal(String... hosts) {
        for (String host : hosts)
            assertFalse("expected not local: " + host, SecurityUtils.isLocalHost(host));
    }

    /**
     * Asserts that every address is private, naming the address that fails.
     *
     * @param literals The addresses that must be private
     */
    private static void assertAllPrivate(String... literals) {
        for (String literal : literals)
            assertTrue("expected private: " + literal,
                    SecurityUtils.isPrivateAddress(address(literal)));
    }

    /**
     * Asserts that every address is public, naming the address that fails.
     *
     * @param literals The addresses that must be public
     */
    private static void assertAllPublic(String... literals) {
        for (String literal : literals)
            assertFalse("expected public: " + literal,
                    SecurityUtils.isPrivateAddress(address(literal)));
    }

    /**
     * A null URL must be rejected
     */
    @Test
    public void isValidUrl_null_returnsFalse() {
        assertFalse(SecurityUtils.isValidUrl(null, false));
        assertFalse(SecurityUtils.isValidUrl(null, true));
    }
    /**
     * An empty or blank URL must be rejected
     */
    @Test
    public void isValidUrl_emptyOrBlank_returnsFalse() {
        assertAllInvalid(false, "", "   ", "\t\n");
    }
    /**
     * A URL longer than the maximum length must be rejected, even when it is otherwise
     * well-formed
     */
    @Test
    public void isValidUrl_overMaxLength_returnsFalse() {
        String tooLong = "https://example.com/" + "a".repeat(MAX_URL_LENGTH);

        assertFalse(SecurityUtils.isValidUrl(tooLong, false));
    }
    /**
     * A URL exactly as long as the maximum length must still be accepted, since only a longer
     * one is rejected
     */
    @Test
    public void isValidUrl_exactlyMaxLength_returnsTrue() {
        String prefix = "https://example.com/";
        String atLimit = prefix + "a".repeat(MAX_URL_LENGTH - prefix.length());

        assertTrue(SecurityUtils.isValidUrl(atLimit, false));
    }
    /**
     * A well-formed HTTPS URL on a public host must be accepted whatever the flag
     */
    @Test
    public void isValidUrl_httpsPublicHost_returnsTrue() {
        assertAllValid(false, "https://example.com/stream", "https://stream.example.com/live");
        assertAllValid(true, "https://example.com/stream", "https://stream.example.com/live");
    }
    /**
     * An HTTP URL must be accepted only when HTTPS is not required
     */
    @Test
    public void isValidUrl_http_dependsOnFlag() {
        assertAllValid(false, "http://example.com/stream");
        assertAllInvalid(true, "http://example.com/stream");
    }
    /**
     * A scheme that is neither HTTP nor HTTPS must be rejected whatever the flag
     */
    @Test
    public void isValidUrl_otherScheme_returnsFalse() {
        assertAllInvalid(false, "ftp://example.com/stream", "file:///etc/passwd");
        assertAllInvalid(true, "ftp://example.com/stream", "file:///etc/passwd");
    }
    /**
     * Text that is not a URL, or a URL missing its scheme or its host, must be rejected
     */
    @Test
    public void isValidUrl_malformed_returnsFalse() {
        assertAllInvalid(false, "not a url", "example.com/stream", "https://", "://example.com");
    }
    /**
     * A URL with a port and a query must be accepted, since streams often use both
     */
    @Test
    public void isValidUrl_portAndQuery_returnsTrue() {
        assertAllValid(false, "https://example.com:8443/stream?quality=high");
    }
    /**
     * A URL with user information must be rejected, so that credentials cannot be smuggled
     * into the address, with or without a password
     */
    @Test
    public void isValidUrl_userInfo_returnsFalse() {
        assertAllInvalid(false,
                "https://user:password@example.com/stream",
                "https://user@example.com/stream");
    }
    /**
     * A URL that points to a local or private host must be rejected: private and loopback
     * addresses, and names that are not on the public internet
     */
    @Test
    public void isValidUrl_localOrPrivateHost_returnsFalse() {
        assertAllInvalid(false,
                "http://192.168.1.10/stream",
                "http://10.0.0.5:8000/live",
                "http://127.0.0.1/stream",
                "http://169.254.169.254/latest",
                "http://100.64.0.1/stream",
                "http://localhost/stream",
                "http://intranet/stream");
    }
    /**
     * A URL whose host is a public IP address must be accepted
     */
    @Test
    public void isValidUrl_publicIpHost_returnsTrue() {
        assertAllValid(false, "http://8.8.8.8/stream");
    }
    /**
     * A URL that the validator lets through but that cannot be parsed as a URI must be
     * rejected and not make the check fail with an exception
     */
    @Test
    public void isValidUrl_illegalCharacters_returnsFalse() {
        assertFalse(SecurityUtils.isValidUrl("https://example.com/stream?q=a|b", false));
    }
    /**
     * A null or empty host must be treated as local, so that a missing host is never
     * contacted
     */
    @Test
    public void isLocalHost_nullOrEmpty_returnsTrue() {
        assertTrue(SecurityUtils.isLocalHost(null));
        assertTrue(SecurityUtils.isLocalHost(""));
    }
    /**
     * The name localhost must be local in any letter case and with a trailing dot
     */
    @Test
    public void isLocalHost_localhost_returnsTrue() {
        assertAllLocal("localhost", "LOCALHOST", "LocalHost", "localhost.");
    }
    /**
     * A name without any dot is the name of a machine in a local network, so it must be local
     */
    @Test
    public void isLocalHost_nameWithoutDot_returnsTrue() {
        assertAllLocal("intranet", "nas", "printer01");
    }
    /**
     * A public domain name must not be local, in any letter case and with a trailing dot
     */
    @Test
    public void isLocalHost_publicDomainName_returnsFalse() {
        assertNoneLocal("example.com", "stream.example.com", "EXAMPLE.COM", "example.com.");
    }
    /**
     * Private IPv4 addresses must be local
     */
    @Test
    public void isLocalHost_privateIpv4_returnsTrue() {
        assertAllLocal("10.0.0.1", "172.16.0.1", "192.168.1.1", "127.0.0.1", "169.254.1.1",
                "0.0.0.0", "100.64.0.1");
    }
    /**
     * Public IPv4 addresses must not be local
     */
    @Test
    public void isLocalHost_publicIpv4_returnsFalse() {
        assertNoneLocal("8.8.8.8", "1.1.1.1", "93.184.216.34");
    }
    /**
     * Private IPv6 addresses must be local, with or without the brackets used in URLs
     */
    @Test
    public void isLocalHost_privateIpv6_returnsTrue() {
        assertAllLocal("::1", "[::1]", "::", "fe80::1", "FE80::1", "fc00::1",
                "fd12:3456:789a::1", "ff02::1");
    }
    /**
     * Public IPv6 addresses must not be local, with or without the brackets used in URLs
     */
    @Test
    public void isLocalHost_publicIpv6_returnsFalse() {
        assertNoneLocal("2001:4860:4860::8888", "[2001:4860:4860::8888]");
    }
    /**
     * The unspecified, loopback, link-local and multicast addresses must be private
     */
    @Test
    public void isPrivateAddress_specialIpv4_returnsTrue() {
        assertAllPrivate("0.0.0.0", "127.0.0.1", "127.255.255.254", "169.254.0.1",
                "224.0.0.1", "239.255.255.255");
    }
    /**
     * The private ranges of IPv4 must be private up to their first and last address
     */
    @Test
    public void isPrivateAddress_siteLocalRanges_returnsTrueAtTheEdges() {
        assertAllPrivate("10.0.0.0", "10.255.255.255", "172.16.0.0", "172.31.255.255",
                "192.168.0.0", "192.168.255.255");
    }
    /**
     * The addresses right outside the private ranges of IPv4 must be public, so that the
     * ranges are not wider than they should be
     */
    @Test
    public void isPrivateAddress_justOutsideSiteLocalRanges_returnsFalse() {
        assertAllPublic("9.255.255.255", "11.0.0.0", "172.15.255.255", "172.32.0.0",
                "192.167.255.255", "192.169.0.0", "169.253.255.255", "169.255.0.0");
    }
    /**
     * The carrier-grade NAT range must be private up to its first and last address, and the
     * addresses right outside it must be public
     */
    @Test
    public void isPrivateAddress_carrierGradeNat_coversExactlyItsRange() {
        assertAllPrivate("100.64.0.0", "100.100.100.100", "100.127.255.255");
        assertAllPublic("100.63.255.255", "100.128.0.0", "99.255.255.255", "101.0.0.0");
    }
    /**
     * Public IPv4 addresses must not be private
     */
    @Test
    public void isPrivateAddress_publicIpv4_returnsFalse() {
        assertAllPublic("8.8.8.8", "1.1.1.1", "93.184.216.34");
    }
    /**
     * The loopback, link-local, site-local and multicast addresses of IPv6 must be private
     */
    @Test
    public void isPrivateAddress_specialIpv6_returnsTrue() {
        assertAllPrivate("::", "::1", "fe80::1", "fec0::1", "ff02::1");
    }
    /**
     * The unique local range of IPv6, fc00::/7, must be private at both of its halves
     */
    @Test
    public void isPrivateAddress_uniqueLocalIpv6_returnsTrue() {
        assertAllPrivate("fc00::1", "fd00::1", "fdff:ffff:ffff:ffff::1");
    }
    /**
     * The IPv6 addresses right outside the unique local range, and ordinary global addresses,
     * must be public
     */
    @Test
    public void isPrivateAddress_publicIpv6_returnsFalse() {
        assertAllPublic("fbff::1", "fe00::1", "2001:4860:4860::8888", "2001:db8::1");
    }
    /**
     * A public address given as a string must not be private, and a private one must be
     */
    @Test
    public void isPrivateAddressString_parsesTheLiteral() {
        assertFalse(SecurityUtils.isPrivateAddress("8.8.8.8"));
        assertTrue(SecurityUtils.isPrivateAddress("10.0.0.1"));
        assertTrue(SecurityUtils.isPrivateAddress("::1"));
    }
    /**
     * A string that cannot be parsed as an address must be treated as private, so that
     * anything unknown is never contacted
     */
    @Test
    public void isPrivateAddressString_unparsable_returnsTrue() {
        assertTrue(SecurityUtils.isPrivateAddress("[not-an-address"));
    }
    /**
     * A null string must be treated as private, since the JDK resolves it to the loopback
     * address
     */
    @Test
    public void isPrivateAddressString_null_returnsTrue() {
        assertTrue(SecurityUtils.isPrivateAddress((String) null));
    }
}
