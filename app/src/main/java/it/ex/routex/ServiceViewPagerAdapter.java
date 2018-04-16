package it.ex.routex;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by ex on 28/06/16.
 */
public class ServiceViewPagerAdapter  extends FragmentStatePagerAdapter {

    DeviceService service;
    Device device;
    Context context;

    public ServiceViewPagerAdapter(FragmentManager fm, DeviceService serv, Context c) {
        super(fm);
        service = serv;
        device = serv.getDevice();
        context = c;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new CommandsFragment(service);
            case 1:
                return new ScheduleFragment(service);
            case 2:
                return new GraphFragment(service);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        /* Attuator */
        if (service.getType().matches("Status")) {
            return 2;
        }

        /* Sensor */
        else return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.frag_commands);
            case 1:
                return context.getResources().getString(R.string.frag_schedule);
            case 2:
                return context.getResources().getString(R.string.frag_graph);
        }
        return null;
    }
}

