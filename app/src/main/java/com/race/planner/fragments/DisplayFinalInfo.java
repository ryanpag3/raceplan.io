package com.race.planner.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.race.planner.R;
import com.race.planner.utils.FragmentListenerInterface;

public class DisplayFinalInfo extends Fragment
{
    private FragmentListenerInterface mListener;
    ViewPager viewPager;
    private AdView mAdView;

    public DisplayFinalInfo()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisplayFinalInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayFinalInfo newInstance(String param1, String param2)
    {
        return new DisplayFinalInfo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_final_info, container, false);

        // TextView finalText = (TextView) view.findViewById(R.id.text_el_fin);

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdView mAdView = (AdView) view.findViewById(R.id.adView);

        // for ad testing
//        String android_id = Settings.Secure.getString(getActivity().getContentResolver(),
//                Settings.Secure.ANDROID_ID);
//        AdRequest request = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
//                .addTestDevice(android_id)  // My Galaxy Nexus test phone
//                .build();
//        mAdView.loadAd(request );

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof FragmentListenerInterface)
        {
            mListener = (FragmentListenerInterface) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    private class ViewPagerAdapter extends PagerAdapter
    {
        private Context mContext;

        public ViewPagerAdapter()
        {
        }

        // instantiates based on panel position
        public Object instantiateItem(ViewGroup collection, int position)
        {
            int resId = 0;
            switch (position)
            {
                case 0:
                    resId = R.id.view_pager_slide_1;
                    break;
                case 1:
                    resId = R.id.view_pager_slide_2;
                    break;
            }
            return getView().findViewById(resId);
        }

        // not used because we increased the limit of how many objects can be instantiated at a time
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);

        }

        // determines the amount of panels to create
        @Override
        public int getCount()
        {
            return 2;
        }

        // checks to see if the current view on the view pager is from the object called
        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view == (View) object;
        }

    }
}
