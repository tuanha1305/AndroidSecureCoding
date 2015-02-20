/*
 * Copyright (C) 2012-2014 Japan Smartphone Security Association
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jssec.android.privacypolicynoinfosent;

import org.jssec.android.privacypolicynoinfosent.MainActivity;
import org.jssec.android.privacypolicynoinfosent.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private LocationClient mLocationClient = null;

	private final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 257;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLocationClient = new LocationClient(this, this, this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Location情報取得用
		if (mLocationClient != null) {
			mLocationClient.connect();
		}
	}

	@Override
	protected void onStop() {
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onStartMap(View view) {
		// ★ポイント1★ 取得した情報を端末内部でのみ利用する場合、アプリ・プライバシーポリシーを表示しなくても良い
		if (mLocationClient != null && mLocationClient.isConnected()) {
			Location currentLocation = mLocationClient.getLastLocation();
			if (currentLocation != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + currentLocation.getLatitude() + "," + currentLocation.getLongitude()));
				startActivity(intent);
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			Location currentLocation = mLocationClient.getLastLocation();
			if (currentLocation != null) {
				String locationData = "Latitude \t: " + currentLocation.getLatitude() + "\n\tLongitude \t: " + currentLocation.getLongitude();

				String text = "\n" + getString(R.string.your_location_title) + "\n\t" + locationData;

				Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + text, Toast.LENGTH_SHORT).show();

				TextView appText = (TextView) findViewById(R.id.appText);
				appText.setText(text);
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDisconnected() {
		mLocationClient = null;
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}
}
