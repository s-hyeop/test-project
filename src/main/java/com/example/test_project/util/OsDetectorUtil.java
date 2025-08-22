package com.example.test_project.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.*;
import java.util.Optional;

/**
 * 운영체제 감지 유틸리티 클래스
 * 
 * <p>HTTP 요청 헤더를 분석하여 클라이언트의 운영체제를 감지합니다.
 * User-Agent Client Hints와 User-Agent 문자열을 모두 활용합니다.</p>
 */
public final class OsDetectorUtil {
    
    private static final String UNKNOWN_OS = "Unknown";
    private static final String HEADER_UA_PLATFORM = "sec-ch-ua-platform";
    private static final String HEADER_UA_PLATFORM_VERSION = "sec-ch-ua-platform-version";
    private static final String HEADER_USER_AGENT = "user-agent";
    
    /**
     * 유틸리티 클래스의 인스턴스화 방지
     */
    private OsDetectorUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * HTTP 요청에서 클라이언트 운영체제를 감지합니다.
     * 
     * <p>User-Agent Client Hints를 우선적으로 확인하고, 없을 경우 
     * User-Agent 문자열을 파싱하여 운영체제를 감지합니다.</p>
     * 
     * @param request HTTP 요청 객체
     * @return 감지된 운영체제 정보 (예: "Windows 11 13.0", "Android 14", "macOS 14.5")
     *         감지 실패 시 "Unknown" 반환
     */
    public static String detect(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_OS;
        }
        
        // 1. User-Agent Client Hints 우선 확인
        Optional<String> osFromClientHints = detectFromClientHints(request);
        if (osFromClientHints.isPresent()) {
            return osFromClientHints.get();
        }
        
        // 2. User-Agent 문자열 파싱
        return detectFromUserAgent(request);
    }
    
    /**
     * User-Agent Client Hints에서 OS 정보를 추출합니다.
     */
    private static Optional<String> detectFromClientHints(HttpServletRequest request) {
        String platform = getHeader(request, HEADER_UA_PLATFORM);
        if (isBlank(platform)) {
            return Optional.empty();
        }
        
        String platformClean = unquote(platform);
        String version = unquote(getHeader(request, HEADER_UA_PLATFORM_VERSION));
        
        if ("Windows".equalsIgnoreCase(platformClean)) {
            return Optional.of(formatWindowsVersion(version));
        }
        
        return Optional.of(formatOsWithVersion(platformClean, version));
    }
    
    /**
     * User-Agent 문자열에서 OS 정보를 추출합니다.
     */
    private static String detectFromUserAgent(HttpServletRequest request) {
        String userAgent = getHeader(request, HEADER_USER_AGENT);
        if (userAgent == null) {
            return UNKNOWN_OS;
        }
        
        // iOS 디바이스 체크
        if (userAgent.contains("Macintosh") && userAgent.contains("Mobile")) {
            return "iPadOS";
        }
        
        // 각 OS별 패턴 매칭
        Optional<String> os = detectIOS(userAgent)
            .or(() -> detectAndroid(userAgent))
            .or(() -> detectWindows(userAgent))
            .or(() -> detectMacOS(userAgent))
            .or(() -> detectOtherOS(userAgent));
        
        return os.orElse(UNKNOWN_OS);
    }
    
    private static Optional<String> detectIOS(String userAgent) {
        Pattern pattern = Pattern.compile("(iPhone|iPad|iPod).*OS\\s([\\d_]+)");
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            String version = matcher.group(2).replace('_', '.');
            return Optional.of("iOS " + version);
        }
        return Optional.empty();
    }
    
    private static Optional<String> detectAndroid(String userAgent) {
        Pattern pattern = Pattern.compile("Android\\s([\\d.]+)");
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            return Optional.of("Android " + matcher.group(1));
        }
        return Optional.empty();
    }
    
    private static Optional<String> detectWindows(String userAgent) {
        Pattern pattern = Pattern.compile("Windows NT ([\\d.]+)");
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            return Optional.of(mapWindowsNTVersion(matcher.group(1)));
        }
        return Optional.empty();
    }
    
    private static Optional<String> detectMacOS(String userAgent) {
        Pattern pattern = Pattern.compile("Mac OS X ([\\d_\\.]+)");
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            String version = matcher.group(1).replace('_', '.');
            return Optional.of("macOS " + version);
        }
        return Optional.empty();
    }
    
    private static Optional<String> detectOtherOS(String userAgent) {
        if (userAgent.contains("CrOS ")) {
            return Optional.of("ChromeOS");
        }
        if (userAgent.contains("Linux")) {
            return Optional.of("Linux");
        }
        return Optional.empty();
    }
    
    private static String formatWindowsVersion(String version) {
        if (isBlank(version)) {
            return "Windows";
        }
        
        int majorVersion = parseFirstNumber(version);
        String windowsName = majorVersion >= 13 ? "Windows 11" : majorVersion > 0 ? "Windows 10" : "Windows";
        return windowsName + " " + version;
    }
    
    private static String formatOsWithVersion(String os, String version) {
        return isBlank(version) ? os : os + " " + version;
    }
    
    /**
     * Windows NT 버전을 사용자 친화적인 이름으로 매핑합니다.
     */
    private static String mapWindowsNTVersion(String ntVersion) {
        return switch (ntVersion) {
            case "10.0" -> "Windows 10/11";
            case "6.3" -> "Windows 8.1";
            case "6.2" -> "Windows 8";
            case "6.1" -> "Windows 7";
            case "6.0" -> "Windows Vista";
            case "5.1" -> "Windows XP";
            default -> "Windows";
        };
    }
    
    private static String getHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value != null ? value : request.getHeader(headerName.toUpperCase());
    }
    
    private static String unquote(String str) {
        if (str == null) {
            return "";
        }
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
    
    private static int parseFirstNumber(String str) {
        if (isBlank(str)) {
            return -1;
        }
        
        String firstPart = str.contains(".") ? 
            str.substring(0, str.indexOf('.')) : str;
        
        try {
            return Integer.parseInt(firstPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}