package it.ex.routex;

import android.text.format.DateFormat;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

/**
 * Created by ex on 17/08/16.
 */
public class ChartAxisFormatter implements AxisValueFormatter {

    private String[] mValues;
    private long base;

    public ChartAxisFormatter(long base) {
       this.base = base;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return convertDate((long)value + base);
    }

    /** this is only needed if numbers are returned, else return 0 */
    @Override
    public int getDecimalDigits() { return 0; }

    public static String convertDate(long dateInMilliseconds) {
        return DateFormat.format("dd-MM-yyyy HH:mm", dateInMilliseconds).toString();
    }
}
