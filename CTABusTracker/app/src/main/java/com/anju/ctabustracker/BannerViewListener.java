package com.anju.ctabustracker;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;



import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;

import java.util.logging.Handler;

public class BannerViewListener implements BannerView.IListener {

    private static final String TAG = "BannerViewListener";
    private final Activity activity;
    private static final int MAX_RETRY_ATTEMPTS = 5; // Max retries before stopping
    private int retryCount = 0;

    public BannerViewListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onBannerLoaded(BannerView bannerView) {
        Log.d(TAG, "onBannerLoaded: ");

    }

    @Override
    public void onBannerShown(BannerView bannerAdView) {
        Log.d(TAG, "onBannerShown: ");
    }

    @Override
    public void onBannerClick(BannerView bannerView) {
        Log.d(TAG, "onBannerClick: ");
    }

    @Override
    public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
        Log.d(TAG, "onBannerFailedToLoad: ");
        if (activity instanceof StopsActivity) {
            // Cast to StopsActivity and call the loadFailed() method
            ((StopsActivity) activity).loadFailed(bannerErrorInfo.errorMessage);
        } else if (activity instanceof MainActivity) {
            // Cast to MainActivity and call the loadFailed() method
            ((MainActivity) activity).loadFailed(bannerErrorInfo.errorMessage);
        }
        else if (activity instanceof PredictionsActivity) {
            // Cast to MainActivity and call the loadFailed() method
            ((PredictionsActivity) activity).loadFailed(bannerErrorInfo.errorMessage);
        }
        retryAdLoad(bannerView);

    }
    private void retryAdLoad(BannerView bannerView) {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            int retryDelay =  1500; // Exponential backoff: 2^n * 1000ms

            Log.d(TAG, "Retrying banner ad load in " + retryDelay + " ms...");

            bannerView.postDelayed(() -> {
                Log.d(TAG, "Attempting to reload banner ad...");
                bannerView.load();  // Attempt to reload the ad
                retryCount++;  // Increment retry count
            }, retryDelay);
        } else {
            Log.e(TAG, "Max retry attempts reached. Stopping further retries.");
        }
    }
    @Override
    public void onBannerLeftApplication(BannerView bannerView) {
        Log.d(TAG, "onBannerLeftApplication: ");
    }
}
