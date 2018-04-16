package it.ex.routex;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ex on 17/08/16.
 */
public class TabbedMainFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager viewPager;

    public TabbedMainFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        //Log.w("RESUMED", "OK");
        final MainViewPagerAdapter mvpa = new MainViewPagerAdapter(getActivity().getSupportFragmentManager(), getActivity());
        viewPager.setAdapter(mvpa);
        viewPager.setOffscreenPageLimit(4);

        tabLayout.setupWithViewPager(viewPager);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tabbed_main, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = (TabLayout) v.findViewById(R.id.main_tabs);

        viewPager = (ViewPager) v.findViewById(R.id.main_tab_viewpager);
        final MainViewPagerAdapter mvpa = new MainViewPagerAdapter(getActivity().getSupportFragmentManager(), getActivity());
        viewPager.setAdapter(mvpa);
        viewPager.setOffscreenPageLimit(4);

        tabLayout.setupWithViewPager(viewPager);

        /* Adapter */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /*TabbedMainFragment.TabFragmentInterface fragment = (TabbedServiceFragment.TabFragmentInterface) mvpa.instantiateItem(viewPager, position);
                if (fragment != null) {
                    fragment.fragmentBecameVisible();
                }*/
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

}
