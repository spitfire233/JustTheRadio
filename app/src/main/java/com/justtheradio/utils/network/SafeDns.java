package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_PRIVATE_ADDRESS_ERROR;

import com.justtheradio.utils.security.SecurityUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;
/**
 * Provides a DNS resolver for the OkHttp clients that refuses to connect to private
 * addresses.
 */
public class SafeDns {
    /** Resolver that uses the system one and rejects the hosts that resolve to private addresses. */
    private static final Dns DNS  = hostname -> {
        List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
        // Reject the host if any of its addresses is private
        for (InetAddress address : addresses)
            if (SecurityUtils.isPrivateAddress(address))
                throw new UnknownHostException(BLOCKED_PRIVATE_ADDRESS_ERROR + hostname);
        return addresses;
    };
    /**
     * Getter for the custom DNS.
     *
     * @return The {@link Dns} that rejects the hosts resolving to private address
     */
    public static Dns getDNS() {
        return DNS;
    }
}
