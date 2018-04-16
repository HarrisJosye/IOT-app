package it.ex.routex;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import it.ex.routex.CommandsFragment;
import it.ex.routex.DeviceFragment;
import it.ex.routex.DeviceListFragment;
import it.ex.routex.DeviceService;
import it.ex.routex.GraphFragment;
import it.ex.routex.R;
import it.ex.routex.ScheduleFragment;
import it.ex.routex.TriggerFragment;

/**
 * Created by ex on 17/08/16.
 */
public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    Context context;

    public MainViewPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        context = c;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                DeviceListFragment f = new DeviceListFragment();
                MainActivity.deviceListFragment = f;
                return f;
            case 1:
                return new AllServiceListFragment();
            case 2:
                TriggerFragment tf = new TriggerFragment();
                MainActivity.triggerFragment = tf;
                return tf;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        /* Attuator */
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.frag_devices);
            case 1:
                return context.getResources().getString(R.string.frag_services);
            case 2:
                return context.getResources().getString(R.string.frag_triggers);
        }
        return null;
    }
}
