package it.ex.routex;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONArray;

import java.util.ArrayList;

import it.ex.routex.gcm.RegistrationIntentService;

public class MainActivity extends AppCompatActivity {

    public static FragmentManager fragMan;

    /* Utility Variables */
    public static boolean isInErrorFrag;
    public static int showing;
    public static int DEVICE_LIST = 0;
    public static int DEVICE = 1;
    public static int GRAPH = 2;
    public static int SCHEDULE = 3;
    public static int TRIGGER = 4;
    public static int ALL_SERVICE_GRAPH = 5;
    public static int NONE = 6;

    /* Fragments for Callbacks */
    public static DeviceListFragment deviceListFragment;
    public static DeviceFragment deviceFragment;
    public static GraphFragment graphFragment;
    public static ScheduleFragment scheduleFragment;
    public static TriggerFragment triggerFragment;
    public static AllServiceGraph allServiceGraphFragment;

    public static TabbedMainFragment mainTabbedFragment;

    /* Google Cloud Message Token */
    public static String gcm_id = new String();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Setting Portrait or Landscape, based on screen size */
        String size = getSizeName(this) ;
        if ( size.matches("large") || size.matches("xlarge") ) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ;
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Set default SharedPreference values */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        if (sp.getString("getdevices_url", "null").matches("null"))
                editor.putString("getdevices_url", getResources().getString(R.string.default_server_url));
        editor.commit();

        /* Set Initial Values */
        showing = DEVICE_LIST;
        isInErrorFrag = false;

        /* Launch Devices Fragment */
        mainTabbedFragment = new TabbedMainFragment();
        fragMan = getSupportFragmentManager() ;
        fragMan.beginTransaction().add(R.id.container, mainTabbedFragment).commit();

        /* Google Cloud Messaging */
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            startService(new Intent(this, RegistrationIntentService.class));
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reload) {
            Log.w("Showing Fragment", showing+" ");

            /* If in ErrorFragment reload previous fragment and retry loading data */
            if (isInErrorFrag) {
                Log.w("ErrorFrag", isInErrorFrag+"");
                fragMan.popBackStack();
            }

            else if (showing == DEVICE_LIST) {
                if (deviceListFragment != null) {
                    deviceListFragment.triggerLoadDevices();
                }
                if (triggerFragment != null) {
                    triggerFragment.triggerLoadTrigger();
                }
                isInErrorFrag = false;
            }

            if (showing == DEVICE) {
                if (deviceFragment != null) {
                    deviceFragment.triggerLoadServices();
                }
                isInErrorFrag = false;
            }

            if (showing == GRAPH) {
                if (graphFragment != null) {
                    graphFragment.reloadGraph();
                }
            }

            if (showing == SCHEDULE) {
                if (scheduleFragment != null) {
                    scheduleFragment.triggerLoadSchedule();
                }
            }

            if (showing == TRIGGER) {
                if (triggerFragment != null) {
                    triggerFragment.triggerLoadTrigger();
                }
            }

            if (showing == ALL_SERVICE_GRAPH) {
                if (allServiceGraphFragment != null) {
                    allServiceGraphFragment.reloadGraph();
                }
            }

            if (showing == NONE) {
            }

            return true;
        }

        /* Launch SettingsActivity */
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        /* Clicked on left arrow in NavigationBar, show previous fragment */
        if (id == android.R.id.home) {
            fragMan.popBackStack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Create notifications */
    public static void createNotification(String msg, Context context) {

            /* Get settings values */
        boolean n = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_enabled", true);
        boolean n_sound = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_sound", true);
        boolean n_vib = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_vibrate", true);

        if (n) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.icon_router_orange)
                            .setContentTitle("Routex")
                            .setContentText(msg)
                            .setAutoCancel(true)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setPriority(Notification.PRIORITY_HIGH);

            if (n_sound) mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

            long pattern[] = {0, 500, 500};
            if (n_vib) mBuilder.setVibrate(pattern);

            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
    }

    /* Add the current fragment to the backStack, and load a new one */
    public static void replaceFrag (Fragment fragment) {
        android.support.v4.app.FragmentTransaction transaction = fragMan.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.container , fragment );
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /* Load the ErrorFragment */
    public static void toErrorFrag (String errorMsg) {
        android.support.v4.app.FragmentTransaction transaction = fragMan.beginTransaction();
        transaction.replace(R.id.container , new ErrorFragment(errorMsg));
        transaction.addToBackStack(null);
        if (isInErrorFrag) fragMan.popBackStack();
        transaction.commit();
        isInErrorFrag = true;
    }

    /* Get screen size */
    private static String getSizeName(Context context) {
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "xlarge";
            default:
                return "undefined";
        }
    }
}
