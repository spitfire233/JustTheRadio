package com.justtheradio.utils.security;

import static com.justtheradio.utils.constant.NetworkConstants.MAX_URL_LENGTH;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

public final class SecurityUtils {

    private final static int MAX_LENGTH = MAX_URL_LENGTH;

    private final static UrlValidator ONLY_HTTPS = new UrlValidator(new String[]{"https"});
    private final static UrlValidator HTTP_AND_HTTPS =
            new UrlValidator(new String[]{"http", "https"});

    private static final InetAddressValidator IP_VALIDATOR = InetAddressValidator.getInstance();

    private SecurityUtils() { }

    public static boolean isValidUrl(String url, boolean mustBeHttps) {
        if (url == null || url.isBlank() || url.length() > MAX_LENGTH) return false;

        UrlValidator urlValidator = mustBeHttps ? ONLY_HTTPS : HTTP_AND_HTTPS;
        if(!urlValidator.isValid(url)) return false;

        try {
            URI uri = new URI(url);
            if (uri.getUserInfo() != null || isLocalHost(uri.getHost())) return false;
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    public static boolean isLocalHost(String host) {
        if (host == null || host.isEmpty()) return true;

        String h = host.toLowerCase(Locale.ROOT);
        if (h.endsWith(".")) h = h.substring(0, h.length() - 1);

        if (h.startsWith("[") && h.endsWith("]"))
            h = h.substring(1, h.length() -1);

        if (IP_VALIDATOR.isValidInet4Address(h) || IP_VALIDATOR.isValidInet6Address(h)) {
            return isPrivateAddress(h);
        }
        return h.equals("localhost") || !h.contains(".");
    }

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

    public static boolean isPrivateAddress(String literal) {
        try {
            return isPrivateAddress(InetAddress.getByName(literal));
        } catch (UnknownHostException e) {
            return true;
        }
    }

}
