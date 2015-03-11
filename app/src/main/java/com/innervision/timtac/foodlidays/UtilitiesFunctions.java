package com.innervision.timtac.foodlidays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilitiesFunctions extends Activity{

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static boolean isEmailValid(String email)
    {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@((([0-1]?" +
                "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])" +
                "\\.([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?[0-9]{1,2}|25[0-5]|2[0-4]" +
                "[0-9])){1}|([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        Pattern pattern;
        pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public static CharSequence addIconToText(Drawable d, String s){

        SpannableStringBuilder sb = new SpannableStringBuilder(" " + s );
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;

    }

    public static boolean isNetworkConnected(Context c) {
        ConnectivityManager conManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conManager.getActiveNetworkInfo();
        return ( netInfo != null && netInfo.isConnected() );
    }

    public static Boolean AnyEmpty(String s1, String s2)
    {
        return !s1.isEmpty() && !s2.isEmpty();
    }

    public static void DisplayError(String s, Activity activity)
    {
        SuperCardToast superCardToast = new SuperCardToast(activity);
        superCardToast.setText(s);
        superCardToast.setDuration(SuperToast.Duration.LONG);
        superCardToast.setBackground(SuperToast.Background.RED);
        superCardToast.setTextColor(Color.WHITE);
        superCardToast.setSwipeToDismiss(true);
        superCardToast.setAnimations(SuperToast.Animations.FLYIN);
        superCardToast.show();
    }



}
