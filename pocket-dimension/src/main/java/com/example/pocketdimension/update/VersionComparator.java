package com.example.pocketdimension.update;

import java.util.ArrayList;
import java.util.List;

public final class VersionComparator {
    private VersionComparator() {}

    public static boolean isNewer(String latest, String current) {
        if (latest == null || current == null) return false;

        List<Integer> a = parse(latest);
        List<Integer> b = parse(current);

        int max = Math.max(a.size(), b.size());
        for (int i = 0; i < max; i++) {
            int av = i < a.size() ? a.get(i) : 0;
            int bv = i < b.size() ? b.get(i) : 0;
            if (av > bv) return true;
            if (av < bv) return false;
        }
        return false;
    }

    private static List<Integer> parse(String raw) {
        String v = raw.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);

        String[] parts = v.split("[^0-9]+");
        List<Integer> out = new ArrayList<>();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            try {
                out.add(Integer.parseInt(p));
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return out;
    }
}
