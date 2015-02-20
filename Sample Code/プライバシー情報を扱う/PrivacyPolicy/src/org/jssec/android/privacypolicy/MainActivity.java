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

package org.jssec.android.privacypolicy;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jssec.android.privacypolicy.ConfirmFragment.DialogListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, DialogListener {
	private final String BASE_URL = "https://www.example.com/pp";
	private final String GET_ID_URI = BASE_URL + "/get_id.php";
	private final String SEND_DATA_URI = BASE_URL + "/send_data.php";
	private final String DEL_ID_URI = BASE_URL + "/del_id.php";

	private final String ID_KEY = "id";
	private final String LOCATION_KEY = "location";
	private final String NICK_NAME_KEY = "nickname";

	private final String PRIVACY_POLICY_AGREED_KEY = "privacyPolicyAgreed";

	private final String PRIVACY_POLICY_PREF_NAME = "privacypolicy_preference";
	private final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 257;

	private String UserId = "";
	private LocationClient mLocationClient = null;

	private final int DIALOG_TYPE_COMPREHENSIVE_AGREEMENT = 1;
	private final int DIALOG_TYPE_PRE_CONFIRMATION = 2;

	private final int VERSION_TO_SHOW_COMPREHENSIVE_AGREEMENT_ANEW = 1;

	private TextWatcher watchHandler = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			boolean buttonEnable = (s.length() > 0);

			MainActivity.this.findViewById(R.id.buttonStart).setEnabled(buttonEnable);
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ユーザー識別用IDをサーバーから取得する
		new GetDataAsyncTask().execute();

		findViewById(R.id.buttonStart).setEnabled(false);
		((TextView) findViewById(R.id.editTextNickname)).addTextChangedListener(watchHandler);

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode == ConnectionResult.SUCCESS) {
			mLocationClient = new LocationClient(this, this, this);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		SharedPreferences pref = getSharedPreferences(PRIVACY_POLICY_PREF_NAME, MODE_PRIVATE);
		int privacyPolicyAgreed = pref.getInt(PRIVACY_POLICY_AGREED_KEY, -1);

		if (privacyPolicyAgreed <= VERSION_TO_SHOW_COMPREHENSIVE_AGREEMENT_ANEW) {
			// ★ポイント1★ 初回起動時(アップデート時)に、アプリが扱う利用者情報の送信について包括同意を得る
			// アップデート時については、新しい利用者情報を扱うようになった場合にのみ、再度包括同意を得る必要がある。
			ConfirmFragment dialog = ConfirmFragment.newInstance(R.string.privacyPolicy, R.string.agreePrivacyPolicy, DIALOG_TYPE_COMPREHENSIVE_AGREEMENT);
			dialog.setDialogListener(this);
			FragmentManager fragmentManager = getSupportFragmentManager();
			dialog.show(fragmentManager, "dialog");
		}

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

	public void onSendToServer(View view) {
		// ★ポイント3★ 慎重な取り扱いが求められる利用者情報を送信する場合は、個別にユーザーの同意を得る
		ConfirmFragment dialog = ConfirmFragment.newInstance(R.string.sendLocation, R.string.cofirmSendLocation, DIALOG_TYPE_PRE_CONFIRMATION);
		dialog.setDialogListener(this);
		FragmentManager fragmentManager = getSupportFragmentManager();
		dialog.show(fragmentManager, "dialog");
	}

	public void onPositiveButtonClick(int type) {
		if (type == DIALOG_TYPE_COMPREHENSIVE_AGREEMENT) {
			// ★ポイント1★ 初回起動時(アップデート時)に、アプリが扱う利用者情報の送信について包括同意を得る
			SharedPreferences.Editor pref = getSharedPreferences(PRIVACY_POLICY_PREF_NAME, MODE_PRIVATE).edit();
			pref.putInt(PRIVACY_POLICY_AGREED_KEY, getVersionCode());
			pref.apply();
		} else if (type == DIALOG_TYPE_PRE_CONFIRMATION) {
			// ★ポイント3★ 慎重な取り扱いが求められる利用者情報を送信する場合は、個別にユーザーの同意を得る
			if (mLocationClient != null && mLocationClient.isConnected()) {
				Location currentLocation = mLocationClient.getLastLocation();
				if (currentLocation != null) {
					String locationData = "Latitude:" + currentLocation.getLatitude() + ", Longitude:" + currentLocation.getLongitude();
					String nickname = ((TextView) findViewById(R.id.editTextNickname)).getText().toString();

					Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + "\n - nickname : " + nickname + "\n - location : " + locationData, Toast.LENGTH_SHORT).show();

					new SendDataAsyncTack().execute(SEND_DATA_URI, UserId, locationData, nickname);
				}
			}
		}
	}

	public void onNegativeButtonClick(int type) {
		if (type == DIALOG_TYPE_COMPREHENSIVE_AGREEMENT) {
			// ★ポイント2★ ユーザーの包括同意が得られていない場合は、利用者情報の送信はしない
			// サンプルアプリではアプリケーションを終了する
			finish();
		} else if (type == DIALOG_TYPE_PRE_CONFIRMATION) {
			// ★ポイント4★ ユーザーの個別同意が得られていない場合は、該当情報の送信はしない
			// ユーザー同意が得られなかったので何もしない
		}
	}

	private int getVersionCode() {
		int versionCode = -1;
		PackageManager packageManager = this.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			versionCode = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
		}

		return versionCode;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_show_pp:
			// ★ポイント5★ ユーザーがアプリ・プライバシーポリシーを確認できる手段を用意する
			Intent intent = new Intent();
			intent.setClass(this, WebViewAssetsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_del_id:
			// ★ポイント6★ 送信した情報をユーザー操作により削除する手段を用意する
			new SendDataAsyncTack().execute(DEL_ID_URI, UserId);
			return true;
		case R.id.action_donot_send_id:
			// ★ポイント7★ ユーザー操作により利用者情報の送信を停止する手段を用意する

			// 利用者情報の送信を停止した場合、包括同意に関する同意は破棄されたものとする
			SharedPreferences.Editor pref = getSharedPreferences(PRIVACY_POLICY_PREF_NAME, MODE_PRIVATE).edit();
			pref.putInt(PRIVACY_POLICY_AGREED_KEY, 0);
			pref.apply();
			
			// 本サンプルでは利用者情報を送信しない場合、ユーザーに提供する機能が無くなるため
			// この段階でアプリを終了する。この処理はアプリ毎の都合に合わせて変更すること。
			String message  = getString(R.string.stopSendUserData);
			Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + " - " + message, Toast.LENGTH_SHORT).show();
			finish();
			
			return true;
		}
		
		return false;
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			Location currentLocation = mLocationClient.getLastLocation();
			if (currentLocation != null) {
				String locationData = "Latitude \t: " + currentLocation.getLatitude() + "\n\tLongitude \t: " + currentLocation.getLongitude();

				String text = "\n" + getString(R.string.your_location_title) + "\n\t" + locationData;

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
	}

	private class GetDataAsyncTask extends AsyncTask<String, Void, String> {
		private String extMessage = "";

		@Override
		protected String doInBackground(String... params) {
			// ★ポイント8★ 利用者情報の紐づけにはUUID/cookieを利用する
			// 本サンプルではサーバー側で生成したIDを利用する
			SharedPreferences sp = getSharedPreferences(PRIVACY_POLICY_PREF_NAME, MODE_PRIVATE);
			UserId = sp.getString(ID_KEY, null);
			if (UserId == null) {
				// SharedPreferences内にトークンが存在しなため、サーバーからIDを取り寄せる。
				try {
					UserId = NetworkUtil.getCookie(GET_ID_URI, "", "id");
				} catch (IOException e) {
					// 証明書エラーなどの例外をキャッチする
					extMessage = e.toString();
				}

				// 取り寄せたIDをSharedPreferencesに保存する。
				sp.edit().putString(ID_KEY, UserId).commit();
			}
			return UserId;
		}

		@Override
		protected void onPostExecute(final String data) {
			String status = (data != null) ? "success" : "error";
			Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + " - " + status + " : " + extMessage, Toast.LENGTH_SHORT).show();
		}
	}

	private class SendDataAsyncTack extends AsyncTask<String, Void, Boolean> {
		private String extMessage = "";

		@Override
		protected Boolean doInBackground(String... params) {
			String url = params[0];
			String id = params[1];
			String location = params.length > 2 ? params[2] : null;
			String nickname = params.length > 3 ? params[3] : null;

			Boolean result = false;
			try {
				JSONObject jsonData = new JSONObject();
				jsonData.put(ID_KEY, id);
				if (location != null)
					jsonData.put(LOCATION_KEY, location);

				if (nickname != null)
					jsonData.put(NICK_NAME_KEY, nickname);

				NetworkUtil.sendJSON(url, "", jsonData.toString());

				result = true;
			} catch (IOException e) {
				// 証明書エラーなどの例外をキャッチする
				extMessage = e.toString();
			} catch (JSONException e) {
				extMessage = e.toString();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			String status = result ? "Success" : "Error";
			Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + " - " + status + " : " + extMessage, Toast.LENGTH_SHORT).show();
		}
	}
}
