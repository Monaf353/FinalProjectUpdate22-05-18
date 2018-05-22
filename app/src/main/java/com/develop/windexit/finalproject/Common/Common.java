package com.develop.windexit.finalproject.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.develop.windexit.finalproject.Model.User;
import com.develop.windexit.finalproject.Remote.APIService;
import com.develop.windexit.finalproject.Remote.IGoogleService;
import com.develop.windexit.finalproject.Remote.RetrofitClient;
import com.develop.windexit.finalproject.Remote.RetrofitGoogleMapClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by WINDEX IT on 16-Feb-18.
 */

public class Common {

    public static String topicName = "News";
    public static User currentUser;
    public static String currentKey;


    public static final int PICK_IMAGE_REQUEST = 701;

    private static final String BASE_URL = "https://fcm.googleapis.com/";
    public static final String googleAPIUrl = "https://maps.googleapis.com/";

    public static String PHONE_TEXT = "userPhone";

    public static IGoogleService getGoogleMapAPI()
    {
        return RetrofitGoogleMapClient.getReofitGoogleMapClient(googleAPIUrl).create(IGoogleService.class);
    }


    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
        if (status.equals("0")) {
            return "Placed";
        } else if (status.equals("1")) {
            return "On my way";
        }  else if (status.equals("2")) {
            return "Shipping";
        }else {
            return "Shipped";
        }
    }

    public static String convertCodeToAvailAbility(String available) {
        if (available.equals("0")) {
            return "Available";
        } else {
            return "Not Available";
        }

    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static boolean isConnectedToINternet(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    //This will convert currency to number base on Locale
   public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException{

       NumberFormat format = NumberFormat.getCurrencyInstance(locale);
       if(format instanceof DecimalFormat)
           ((DecimalFormat)format).setParseBigDecimal(true);
       return (BigDecimal)format.parse(amount.replace("[^\\d.,]",""));
   }

    public static String getDate(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(
                android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", calendar).toString());
        return date.toString();
    }

    public static String date(){
        Calendar calendar = Calendar.getInstance();
        // calendar.setTimeInMillis(time);
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = simpleDateFormat.format(calendar.getTime());

         return date;
    }

    /* public static boolean isConnectedToINternet(Context context) {

        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connec != null) {
            NetworkInfo info[] = connec.getAllNetworkInfo();  //All network info

            if (info != null)
            {
                int i;
                for (i = 0; i < info.length; i++) ;
                {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return false;
                }

            }
        }
        return false;
    }*/
}

