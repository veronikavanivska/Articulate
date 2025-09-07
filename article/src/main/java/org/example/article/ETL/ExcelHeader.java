package org.example.article.ETL;

import org.apache.poi.ss.usermodel.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public final class ExcelHeader {
    private ExcelHeader() {}

    private static final Pattern YEAR_COL = Pattern.compile("^\\d{4}$");
    private static final Pattern CODE_COL = Pattern.compile("^\\d{3,4}$");
    private static final DecimalFormat NUM_FMT = new DecimalFormat("#");

    public static int findHeaderRow(Sheet sh, Set<String> markers) {
        for (int r = sh.getFirstRowNum(); r <= sh.getLastRowNum(); r++) {
            Row row = sh.getRow(r);
            if (row == null) continue;
            for (Cell c : row) {
                String s = val(c);
                if (s != null && markers.contains(s.trim())) return r;
            }
        }
        return -1;
    }

    public static Map<Integer,String> rowToMap(Row r) {
        Map<Integer,String> out = new LinkedHashMap<>();
        if (r == null) return out;
        short last = r.getLastCellNum();
        for (int i = 0; i < last; i++) out.put(i, val(r.getCell(i)));
        return out;
    }

    public static Map<Integer,String> detectCodeColumns(Map<Integer,String> labels) {
        Map<Integer,String> codes = new LinkedHashMap<>();
        for (var e : labels.entrySet()) {
            String lab = safe(e.getValue());
            if (lab != null && CODE_COL.matcher(lab).matches() && !YEAR_COL.matcher(lab).matches()) {
                codes.put(e.getKey(), lab);
            }
        }
        return codes;
    }

    public static Integer find(Map<Integer,String> labels, String... wanted) {
        for (var e : labels.entrySet()) {
            String v = safe(e.getValue());
            if (v == null) continue;
            for (String w : wanted) if (v.equalsIgnoreCase(w)) return e.getKey();
        }
        return null;
    }

    public static Integer nextAfter(Map<Integer,String> labels, Integer afterIdx, String... names) {
        if (afterIdx == null) return null;
        int max = labels.keySet().stream().mapToInt(Integer::intValue).max().orElse(afterIdx);
        for (int i = afterIdx + 1; i <= max; i++) {
            String v = safe(labels.get(i));
            if (v == null) continue;
            for (String n : names) if (v.equalsIgnoreCase(n)) return i;
        }
        return null;
    }

    public static String getCell(Row r, Integer idx) {
        if (r == null || idx == null) return null;
        return val(r.getCell(idx));
    }

    public static boolean isChecked(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.equals("x") || t.equals("1") || t.equals("true");
    }

    public static Integer toInt(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    public static String nullIfBlank(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private static String val(Cell c) {
        if (c == null) return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> (DateUtil.isCellDateFormatted(c) ? c.getDateCellValue().toString()
                    : NUM_FMT.format(c.getNumericCellValue()));
            case BOOLEAN -> Boolean.toString(c.getBooleanCellValue());
            case FORMULA -> {
                try { yield c.getStringCellValue(); }
                catch (Exception e) { yield Double.toString(c.getNumericCellValue()); }
            }
            default -> null;
        };
    }

    private static String safe(String s) { return (s == null) ? null : s.trim(); }
}
