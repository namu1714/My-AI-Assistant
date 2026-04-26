package com.acme.assistant.tool.validator;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class UrlValidator {

    public URI validate(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("URL이 비어 있습니다");
        }

        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("잘못된 URL: " + e.getMessage(), e);
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || (!scheme.equals("http") && !scheme.equals("https"))
        ) {
            throw new SecurityException("허용되지 않는 프로토콜: " + scheme + " (http 또는 https만 허용)");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(" 호스트가 없습니다");
        }

        if (isPrivateHost(host)) {
            throw new SecurityException("사설 네트워크 접근 차단: " + host);
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            if (isPrivateAddress(address)) {
                throw new SecurityException("사설 IP 접근 차단: " + host + " -> " + address.getHostAddress());
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("호스트를 찾을 수 없습니다: " + host, e);
        }

        return uri;
    }

    private boolean isPrivateHost(String host) {
        return host.equals("localhost")
                || host.equals("127.0.0.1")
                || host.equals("[::1]")
                || host.endsWith(".local")
                || host.endsWith(".internal");
    }

    private boolean isPrivateAddress(InetAddress address) {
        return address.isLoopbackAddress()
                || address.isSiteLocalAddress()
                || address.isLinkLocalAddress()
                || address.isAnyLocalAddress();
    }
}
