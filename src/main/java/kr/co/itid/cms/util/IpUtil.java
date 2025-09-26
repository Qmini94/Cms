package kr.co.itid.cms.util;

import java.net.Inet4Address;
import java.net.InetAddress;

public class IpUtil {

    /** IPv4 CIDR 포함 여부 검사. 예) ip="49.254.140.140", cidr="49.254.140.0/24" */
    public static boolean isInRange(String ip, String cidr) {
        try {
            if (ip == null || cidr == null) return false;

            int slash = cidr.indexOf('/');
            if (slash < 0) return false;

            String network = cidr.substring(0, slash).trim();
            int prefix = Integer.parseInt(cidr.substring(slash + 1).trim());
            if (prefix < 0 || prefix > 32) return false;

            InetAddress ipAddr = InetAddress.getByName(ip);
            InetAddress netAddr = InetAddress.getByName(network);

            // IPv4만 처리 (IPv6는 별도 정책)
            if (!(ipAddr instanceof Inet4Address) || !(netAddr instanceof Inet4Address)) {
                return false;
            }

            int ipInt  = ipv4ToInt(ipAddr.getAddress());
            int netInt = ipv4ToInt(netAddr.getAddress());

            // 마스크: -1 << (32 - prefix)  (prefix=0이면 0)
            int mask = (prefix == 0) ? 0 : (-1 << (32 - prefix));

            return (ipInt & mask) == (netInt & mask);
        } catch (Exception e) {
            return false;
        }
    }

    private static int ipv4ToInt(byte[] addr) {
        // big-endian, 부호 확장 방지
        return ((addr[0] & 0xFF) << 24)
                | ((addr[1] & 0xFF) << 16)
                | ((addr[2] & 0xFF) << 8)
                |  (addr[3] & 0xFF);
    }

    /** 선택: "49.254.140.*" 같은 패턴을 CIDR로 변환 (원하면 서비스단에서 한번 변환해 저장) */
    public static String wildcardToCidr(String pattern) {
        if (pattern == null || pattern.isEmpty()) return null;
        String[] p = pattern.trim().split("\\.");
        if (p.length != 4) return null;
        int stars = 0;
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if ("*".equals(p[i])) {
                stars++;
                base.append("0");
            } else {
                int n = Integer.parseInt(p[i]);
                if (n < 0 || n > 255) return null;
                base.append(n);
            }
            if (i < 3) base.append('.');
        }
        int prefix = 32 - (stars * 8);
        return base + "/" + prefix;
    }
}