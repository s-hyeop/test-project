package com.example.test_project.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.*;

public class OsDetectorUtil {
    private OsDetectorUtil() {}

    /** 예: "Windows 11 13.0", "Android 14", "macOS 14.5", "Linux", "Unknown" */
    public static String detect(HttpServletRequest req) {
        // 1) UA-CH 우선
        String plat = h(req, "sec-ch-ua-platform");
        String ver  = h(req, "sec-ch-ua-platform-version");
        if (notEmpty(plat)) {
            String p = unq(plat), v = unq(ver);
            if ("Windows".equalsIgnoreCase(p)) {
                int major = parseInt(first(v));
                String name = major >= 13 ? "Windows 11" : (major > 0 ? "Windows 10" : "Windows");
                return v.isBlank() ? name : name + " " + v;
            }
            return v.isBlank() ? p : p + " " + v;
        }

        // 2) UA 정규식 fallback
        String ua = h(req, "user-agent");
        if (ua == null) return "Unknown";

        if (ua.contains("Macintosh") && ua.contains("Mobile")) return "iPadOS";
        Matcher m = Pattern.compile("(iPhone|iPad|iPod).*OS\\s([\\d_]+)").matcher(ua);
        if (m.find()) return "iOS " + m.group(2).replace('_','.');
        m = Pattern.compile("Android\\s([\\d.]+)").matcher(ua);
        if (m.find()) return "Android " + m.group(1);
        m = Pattern.compile("Windows NT ([\\d.]+)").matcher(ua);
        if (m.find()) return mapWin(m.group(1));
        m = Pattern.compile("Mac OS X ([\\d_\\.]+)").matcher(ua);
        if (m.find()) return "macOS " + m.group(1).replace('_','.');
        if (ua.contains("CrOS ")) return "ChromeOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }

    // --- helpers ---
    private static String h(HttpServletRequest r, String n){ String v=r.getHeader(n); return v!=null?v:r.getHeader(n.toUpperCase());}
    private static String unq(String s){ if(s==null) return ""; s=s.trim(); return s.startsWith("\"")&&s.endsWith("\"")?s.substring(1,s.length()-1):s; }
    private static String first(String s){ if(s==null||s.isBlank()) return ""; int p=s.indexOf('.'); return p>0?s.substring(0,p):s; }
    private static int parseInt(String n){ try { return Integer.parseInt(n); } catch(Exception e){ return -1; } }
    private static boolean notEmpty(String s){ return s!=null && !s.isBlank(); }
    private static String mapWin(String nt){
        return switch (nt) {
            case "10.0" -> "Windows 10/11"; // UA만으론 구분 불가(UA-CH로 보완)
            case "6.3" -> "Windows 8.1";
            case "6.2" -> "Windows 8";
            case "6.1" -> "Windows 7";
            case "6.0" -> "Windows Vista";
            case "5.1" -> "Windows XP";
            default -> "Windows";
        };
    }
}
