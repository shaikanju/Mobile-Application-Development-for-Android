package com.anju.ctabustracker;

import android.app.Activity;
import android.util.Log;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

public class UnityInitializationListener implements IUnityAdsInitializationListener {

    private static final String TAG = "UnityInitializationList";
    private final Activity Activity;

    public UnityInitializationListener(Activity activity) {
        this.Activity = activity;
    }

    @Override
    public void onInitializationComplete() {
        Log.d(TAG, "onInitializationComplete: ");
        if (Activity instanceof StopsActivity) {
            // Cast to StopsActivity and call the showBanner() method
            ((StopsActivity) Activity).showBanner();
        } else if (Activity instanceof MainActivity) {
            // Cast to MainActivity and call the showBanner() method
            ((MainActivity) Activity).showBanner();
        }
        else if (Activity instanceof PredictionsActivity) {
            // Cast to MainActivity and call the showBanner() method
            ((PredictionsActivity) Activity).showBanner();
        }
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String s) {
        Log.d(TAG, "onInitializationFailed: ");
        if (Activity instanceof StopsActivity) {
            // Cast to StopsActivity and call the initFailed() method
            ((StopsActivity) Activity).initFailed(s);
        } else if (Activity instanceof MainActivity) {
            // Cast to MainActivity and call the initFailed() method
            ((MainActivity) Activity).initFailed(s);
        }
        else if (Activity instanceof PredictionsActivity) {
            // Cast to MainActivity and call the initFailed() method
            ((PredictionsActivity) Activity).initFailed(s);
        }

    }
}
