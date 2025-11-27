package net.atos.dms.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextAlgo {
    public static List<String> ngrams(String s, int n) {
        s = s.toLowerCase(Locale.ROOT);
        List<String> g = new ArrayList<>();
        if (s.length() < n) {
            g.add(s);
            return g;
        }
        for (int i = 0; i <= s.length() - n; i++) g.add(s.substring(i, i + n));
        return g;
    }

    public static boolean kmpContains(String text, String pat) {
        text = text.toLowerCase(Locale.ROOT);
        pat = pat.toLowerCase(Locale.ROOT);
        if (pat.isEmpty()) return true;
        int[] lps = lps(pat);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pat.charAt(j)) {
                i++;
                j++;
                if (j == pat.length()) return true;
            } else if (j > 0) j = lps[j - 1];
            else i++;
        }
        return false;
    }

    private static int[] lps(String p) {
        int[] l = new int[p.length()];
        int len = 0;
        for (int i = 1; i < p.length(); ) {
            if (p.charAt(i) == p.charAt(len)) {
                l[i++] = ++len;
            } else if (len > 0) len = l[len - 1];
            else l[i++] = 0;
        }
        return l;
    }

    public static boolean isSubsequence(String text, String pat) {
        text = text.toLowerCase(Locale.ROOT);
        pat = pat.toLowerCase(Locale.ROOT);
        int i = 0, j = 0;
        while (i < text.length() && j < pat.length()) {
            if (text.charAt(i) == pat.charAt(j)) j++;
            i++;
        }
        return j == pat.length();
    }

    public static int editDistance(String a, String b) {
        a = a.toLowerCase(Locale.ROOT);
        b = b.toLowerCase(Locale.ROOT);
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++)
            for (int j = 1; j <= b.length(); j++) {
                int c = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + c);
            }
        return dp[a.length()][b.length()];
    }

    public static int score(String name, String q) {
        int s = 0;
        if (kmpContains(name, q)) s += 50;
        if (isSubsequence(name, q)) s += 20;
        int ed = editDistance(name, q);
        s += Math.max(0, 20 - ed * 5);
        s += Math.min(q.length(), name.length());
        return s;
    }
}
