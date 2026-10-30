package com.justtheradio.utils.security;

import static com.justtheradio.utils.constant.NetworkConstants.MAX_URL_LENGTH;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Utility class with the security checks applied to the URLs and addresses that come from
 * external sources, such as the stream and logo URLs of the radio stations.
 */
public final class SecurityUtils {
    /** Maximum length accepted for a URL, in characters. */
    private final static int MAX_LENGTH = MAX_URL_LENGTH;
    /** Validator that accepts only URLs with the {@code https} scheme. */
    private final static UrlValidator ONLY_HTTPS = new UrlValidator(new String[]{"https"});
    /** Validator that accepts URLs with the {@code http} or {@code https} scheme. */
    private final static UrlValidator HTTP_AND_HTTPS =
            new UrlValidator(new String[]{"http", "https"});
    /** Validator used to recognize IPv4 and IPv6 address literals. */
    private static final InetAddressValidator IP_VALIDATOR = InetAddressValidator.getInstance();

    /**
     * Private constructor for {@link SecurityUtils}.
     * This is a utility class and must not be instantiated.
     */
    private SecurityUtils() { }
    /**
     * Checks whether a string is a URL that is safe to use. A URL is accepted only if all
     * the following hold:
     * <ul>
     *   <li>It is not {@code null} or blank and is not longer than the maximum length;</li>
     *   <li>It is well-formed and its scheme is allowed ({@code https} only, or {@code http}
     *       and {@code https});
     *   </li>
     *   <li>It does not contain user information (a username or password before the host);</li>
     *   <li>Its host is not a local or private one (see {@link #isLocalHost(String)}).</li>
     * </ul>
     *
     * @param url         The URL to check
     * @param mustBeHttps {@code true} to accept only {@code https} URLs, {@code false} to
     *                    also accept {@code http} ones
     * @return {@code true} If the URL is valid, {@code false} otherwise
     */
    public static boolean isValidUrl(String url, boolean mustBeHttps) {
        if (url == null || url.isBlank() || url.length() > MAX_LENGTH) return false;

        UrlValidator urlValidator = mustBeHttps ? ONLY_HTTPS : HTTP_AND_HTTPS;
        if(!urlValidator.isValid(url)) return false;

        try {
            URI uri = new URI(url);
            // Reject URLs with credentials in them and URLs pointing to local hosts
            if (uri.getUserInfo() != null || isLocalHost(uri.getHost())) return false;
        } catch (URISyntaxException e) {
            return false; // In case of error, assume is not valid
        }
        return true;
    }

    /**
     * Checks whether a host name or address refers to a local device or network, and so
     * must not be contacted. The following are considered local:
     * <ul>
     *   <li>A {@code null} or empty host;</li>
     *   <li>An IPv4 or IPv6 address that is private (see {@link #isPrivateAddress(String)});</li>
     *   <li>The name {@code localhost};</li>
     *   <li>A name without any dot, such as the name of a machine in a local network.</li>
     * </ul>
     *
     * @param host The host name or IP address, without scheme or port
     * @return {@code true} if the host is local or private, {@code false} otherwise
     */
    public static boolean isLocalHost(String host) {
        if (host == null || host.isEmpty()) return true;

        String h = host.toLowerCase(Locale.ROOT);
        // Remove the trailing dot of a fully qualified name ("example.com.")
        if (h.endsWith(".")) h = h.substring(0, h.length() - 1);

        // Remove the brackets around an IPv6 address ("[::1]")
        if (h.startsWith("[") && h.endsWith("]"))
            h = h.substring(1, h.length() -1);

        if (IP_VALIDATOR.isValidInet4Address(h) || IP_VALIDATOR.isValidInet6Address(h)) {
            return isPrivateAddress(h);
        }
        return h.equals("localhost") || !h.contains(".");
    }

    /**
     * Checks whether an address is private, that is not reachable from the public internet.
     * The following are considered private:
     * <ul>
     *     <li>Any local address and the loopback addresses</li>
     *     <li>The link-local address</li>
     *     <li>The site-local address (the private ranges of IPv4)</li>
     *     <li>The multicast address</li>
     *     <li>The carrier-grade NAT range of IPv4 ({@code 100.64.0.0/10})</li>
     *     <li>The unique local addresses of IPv6 ({@code fc00::/7})</li>
     * </ul>
     *
     * @param address The address to check
     * @return {@code true} if the address is private, {@code false} if it is public
     */
    public static boolean isPrivateAddress(InetAddress address) {
        if (address.isAnyLocalAddress() // 0.0.0.0, ::
                || address.isLoopbackAddress() // 127.0.0.0/8, ::1
                || address.isLinkLocalAddress() // 169.254.0.0/16, fe80::/10
                || address.isSiteLocalAddress() // 10/8, 172.16/12, 192.168/16
                || address.isMulticastAddress()) {
            return true;
        }
        byte[] b = address.getAddress();
        if (b.length == 4) { // Carrier-grade NAT
            return (b[0] & 0xFF) == 100 && (b[1] & 0xC0) == 0x40;
        }
        // fc00::/7 (IPv6 unique local addresses)
        return (b[0] & 0xFE) == 0xFC;
    }
    /**
     * Checks whether an IP address, given as a string, is private. See
     * {@link #isPrivateAddress(InetAddress)}.
     *
     * @param literal The IP address as a string
     * @return {@code true} if the address is private or cannot be parsed, {@code false} if
     * it is public
     */
    public static boolean isPrivateAddress(String literal) {
        try {
            return isPrivateAddress(InetAddress.getByName(literal));
        } catch (UnknownHostException e) {
            return true;
        }
    }
}
