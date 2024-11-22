package fr.iban.warps.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<>();
        Pattern p = Pattern.compile("\\b.{1," + (lineSize - 1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while(m.find()) {
            res.add("§a" + m.group());
        }

        return res;
    }

}
