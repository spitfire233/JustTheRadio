package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_PRIVATE_ADDRESS_ERROR;

import com.justtheradio.utils.security.SecurityUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;

public class SafeDns {
    private static final Dns DNS  = hostname -> {
        List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
        for (InetAddress address : addresses)
            if (SecurityUtils.isPrivateAddress(address))
                throw new UnknownHostException(BLOCKED_PRIVATE_ADDRESS_ERROR + hostname);
        return addresses;
    };

    public static Dns getDNS() {
        return DNS;
    }
}
