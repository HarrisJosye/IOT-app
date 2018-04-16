package it.ex.routex;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by ex on 28/06/16.
 */
public class TabbedServiceFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager viewPager;
    DeviceService service;

    public TabbedServiceFragment() {}

    @SuppressLint("ValidFragment")
    public TabbedServiceFragment(DeviceService s) {
        service = s;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tabbed_service, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = (TabLayout) v.findViewById(R.id.service_tabs);

        viewPager = (ViewPager) v.findViewById(R.id.service_tab_viewpager);
        final ServiceViewPagerAdapter svpa = new ServiceViewPagerAdapter(getActivity().getSupportFragmentManager(), service, getActivity());
        viewPager.setAdapter(svpa);
        viewPager.setOffscreenPageLimit(4);

        tabLayout.setupWithViewPager(viewPager);

        /* Adapter */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                TabFragmentInterface fragment = (TabFragmentInterface) svpa.instantiateItem(viewPager, position);
                if (fragment != null) {
                    fragment.fragmentBecameVisible();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    public interface TabFragmentInterface {
        void fragmentBecameVisible();
    }
}
