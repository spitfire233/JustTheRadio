package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_PRIVATE_ADDRESS_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.justtheradio.utils.security.SecurityUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;


/**
 * Unit tests for {@link SafeDns}.
 * The hosts used are IP literals, which the system resolver
 * returns as they are without asking any DNS server. The check on private addresses is mocked.
 */
public class SafeDnsUnitTest {

    /** Address of a host that the tests treat as public. */
    private static final String PUBLIC_HOST = "8.8.8.8";
    /** Address of a host that the tests treat as private. */
    private static final String PRIVATE_HOST = "10.0.0.1";

    /** Static mock replacing the private address check, closed after each test. */
    private MockedStatic<SecurityUtils> securityStatic;
    /** The DNS under test. */
    private Dns dns;

    /**
     * Gets the DNS under test and replaces the private address check with a mock that answers
     * that no address is private, unless a test says otherwise
     */
    @Before
    public void setUp() {
        securityStatic = Mockito.mockStatic(SecurityUtils.class);
        dns = SafeDns.getDNS();
    }

    /**
     * The static mock is bound to the thread that opened it: it must be closed after each
     * test, or it leaks into the following ones
     */
    @After
    public void tearDown() {
        securityStatic.close();
    }

    /**
     * A host whose address is not private must be resolved and its addresses returned as the
     * system resolver gives them
     */
    @Test
    public void lookup_publicAddress_returnsResolvedAddresses() throws Exception {
        List<InetAddress> result = dns.lookup(PUBLIC_HOST);

        assertEquals(1, result.size());
        assertEquals(PUBLIC_HOST, result.get(0).getHostAddress());
    }
    /**
     * Every resolved address must be passed to the private address check, since a host is
     * judged by the addresses it actually resolves to and not by its name
     */
    @Test
    public void lookup_checksEveryResolvedAddress() throws Exception {
        InetAddress address = InetAddress.getByName(PUBLIC_HOST);

        dns.lookup(PUBLIC_HOST);

        securityStatic.verify(() -> SecurityUtils.isPrivateAddress(address));
    }
    /**
     * A host that resolves to a private address must be rejected with an UnknownHostException
     * that names the host
     */
    @Test
    public void lookup_privateAddress_throwsUnknownHostException() {
        securityStatic.when(() -> SecurityUtils.isPrivateAddress(any(InetAddress.class)))
                .thenReturn(true);

        UnknownHostException error = assertThrows(UnknownHostException.class,
                () -> dns.lookup(PRIVATE_HOST));

        assertEquals(BLOCKED_PRIVATE_ADDRESS_ERROR + PRIVATE_HOST, error.getMessage());
    }
    /**
     * The decision must come from the private address check and not from the address itself:
     * the same host must be accepted when the check says it is not private
     */
    @Test
    public void lookup_sameHost_followsPrivateAddressCheck() throws Exception {
        List<InetAddress> result = dns.lookup(PRIVATE_HOST);

        assertEquals(PRIVATE_HOST, result.get(0).getHostAddress());
    }
}
