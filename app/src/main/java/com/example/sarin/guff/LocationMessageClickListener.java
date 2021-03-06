package com.example.sarin.guff;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;



public class LocationMessageClickListener implements View.OnClickListener {

    AppCompatActivity activity;
    LatLng latLng;

    public LocationMessageClickListener(AppCompatActivity activity, LatLng latLng) {
        this.activity = activity;
        this.latLng = latLng;
    }



    @Override
    public void onClick(View v) {
        openLocationInGoogleMaps(latLng.latitude, latLng.longitude);
    }

    private void openLocationInGoogleMaps(Double latitude, Double longitude){
        try {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f (%s)", latitude, longitude, latitude, longitude, "Mark");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastHelper.show(activity, R.string.permission_request_denied);
        }
    }

}


