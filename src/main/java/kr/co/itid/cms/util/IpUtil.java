package kr.co.itid.cms.util;

import java.net.InetAddress;

public class IpUtil {
    public static boolean isInRange(String ip, String cidr) {
        try {
            int index = cidr.indexOf("/");
            if (index < 0) return false;

            String network = cidr.substring(0, index);
            int netmask = Integer.parseInt(cidr.substring(index + 1));

            byte[] address = InetAddress.getByName(ip).getAddress();
            byte[] networkAddress = InetAddress.getByName(network).getAddress();

            int mask = ~((1 << (32 - netmask)) - 1);

            int ipAddr = byteArrayToInt(address);
            int networkAddr = byteArrayToInt(networkAddress);

            return (ipAddr & mask) == (networkAddr & mask);

        } catch (Exception e) {
            return false;
        }
    }

    private static int byteArrayToInt(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}
