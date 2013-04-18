package com.google.android.location.provider;

import android.annotation.TargetApi;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;

import com.android.location.provider.LocationProviderBase;
import com.android.location.provider.LocationRequestUnbundled;
import com.android.location.provider.ProviderPropertiesUnbundled;
import com.android.location.provider.ProviderRequestUnbundled;
import com.google.android.location.NetworkLocationThread;
import com.google.android.location.data.LocationData;
import com.google.android.location.data.LocationDataProvider;

@TargetApi(17)
public class NetworkLocationProviderV2 extends LocationProviderBase implements
		NetworkLocationProviderBase {

	private final static String IDENTIFIER = "network";

	private static final String TAG = "NetworkLocationProviderV2";

	private final NetworkLocationThread background;

	public NetworkLocationProviderV2() {
		super(TAG, ProviderPropertiesUnbundled.create(true, false, true, false,
				false, false, false, Criteria.POWER_LOW,
				Criteria.ACCURACY_COARSE));
		background = new NetworkLocationThread();
		background.start();
	}

	@Deprecated
	public NetworkLocationProviderV2(final boolean internal) {
		this();
	}

	@Override
	public boolean isActive() {
		return background != null && background.isAlive()
				&& background.isActive();
	}

	@Override
	public void onDisable() {
		background.setActive(false);
	}

	@Override
	public void onEnable() {
		background.setActive(true);
	}

	@Override
	public int onGetStatus(final Bundle arg0) {
		return android.location.LocationProvider.AVAILABLE;
	}

	@Override
	public long onGetStatusUpdateTime() {
		return background.getLastTime();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			background.setLastTime(SystemClock.elapsedRealtime());
			background.setLastLocation(location);
			final Bundle b = new Bundle();
			b.putString("networkLocationType", location.getProvider());
			location.setExtras(b);
			makeComplete(location);
			location = LocationDataProvider.Stub.renameSource(location,
					IDENTIFIER);
			reportLocation(location);
		}
	}
	
	private void makeComplete(Location location) {
        if (location.getProvider() == null) location.setProvider("?");
        if (!location.hasAccuracy()) {
            location.setAccuracy(100.0f);
        }
        if (location.getTime() == 0) location.setTime(System.currentTimeMillis());
    }

	@Override
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onSetRequest(final ProviderRequestUnbundled requests,
			final WorkSource ws) {
		long autoTime = Long.MAX_VALUE;
		boolean autoUpdate = false;
		for (final LocationRequestUnbundled request : requests
				.getLocationRequests()) {
			if (request.getInterval() < autoTime) {
				autoTime = request.getInterval();
			}
			autoUpdate = true;
		}
		if (autoTime < 5000) {
			autoTime = 5000;
		}
		background.setAuto(autoUpdate, autoTime);
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
	}

	@Override
	public void setData(final LocationData data) {
		background.setData(data);
	}

}
