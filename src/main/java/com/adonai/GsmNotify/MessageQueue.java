package com.adonai.GsmNotify;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MessageQueue extends ArrayList<String> {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    ArrayList<String> times = new ArrayList<>();

    @Override
    public boolean add(String object) {
        if (size() < 5) {   // нам нужно всего 5 сообщений макс. в списке
            times.add(sdf.format(Calendar.getInstance().getTime()));
            return super.add(object);
        } else {
            remove(0);
            times.remove(0);

            times.add(sdf.format(Calendar.getInstance().getTime()));
            return super.add(object);
        }
    }

    @Override
    public String toString() { // формат вывода
        String res = "";
        for (int i = 0; i < size(); i++) {
            res = times.get(i) + '\n' + get(i) + "\n\n" + res;
        }

        return res;
    }
}
