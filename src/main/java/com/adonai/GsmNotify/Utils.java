package com.adonai.GsmNotify;

import java.util.Collection;
import java.util.Iterator;

public class Utils {
    static String join(Collection<String> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
            String current = iter.next();
            if (current.equals("")) // skip empty parts
            {
                continue;
            }

            builder.append(current);

            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }
}
