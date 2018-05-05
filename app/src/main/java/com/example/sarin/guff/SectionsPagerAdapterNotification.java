package com.example.sarin.guff;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPagerAdapterNotification extends FragmentPagerAdapter {

    public SectionsPagerAdapterNotification(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
//                NotificationFriendsFragment mNotificationFriendsFragment = new NotificationFriendsFragment();
//                return mNotificationFriendsFragment;
            case 1:
//                return new NotificationMeFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public String getPageTitle(int position){
        switch (position){
            case 0:
                return "Friends Activity";
            case 1:
                return "Yours Activity";
            default:
                return null;
        }
    }
}
