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

package org.jssec.android.privacypolicynocomprehensive;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	private final String BASE_URL = "https://www.example.com/pp";
	private final String GET_ID_URI = BASE_URL + "/get_id.php";
	private final String SEND_DATA_URI = BASE_URL + "/send_data.php";
	private final String DEL_ID_URI = BASE_URL + "/del_id.php";

	private final String ID_KEY = "id";
	private final String NICK_NAME_KEY = "nickname";

	private final String PRIVACY_POLICY_PREF_NAME = "privacypolicy_preference";

	private String UserId = "";

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
	}

	public void onSendToServer(View view) {
		String nickname = ((TextView) findViewById(R.id.editTextNickname)).getText().toString();
		Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + "\n - nickname : " + nickname, Toast.LENGTH_SHORT).show();
		new sendDataAsyncTack().execute(SEND_DATA_URI, UserId, nickname);
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
			// ★ポイント1★ ユーザーがアプリ・プライバシーポリシーを確認できる手段を用意する
			Intent intent = new Intent();
			intent.setClass(this, WebViewAssetsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_del_id:
			// ★ポイント2★ 送信した情報をユーザー操作により削除する手段を用意する
			new sendDataAsyncTack().execute(DEL_ID_URI, UserId);
			return true;
		case R.id.action_donot_send_id:
			// ★ポイント3★ ユーザー操作により利用者情報の送信を停止する手段を用意する

			// 本サンプルでは利用者情報を送信しない場合、ユーザーに提供する機能が無くなるため
			// この段階でアプリを終了する。この処理はアプリ毎の都合に合わせて変更すること。
			String message  = getString(R.string.stopSendUserData);
			Toast.makeText(MainActivity.this, this.getClass().getSimpleName() + " - " + message, Toast.LENGTH_SHORT).show();
			finish();
			
			return true;
		}
		return false;
	}

	private class GetDataAsyncTask extends AsyncTask<String, Void, String> {
		private String extMessage = "";

		@Override
		protected String doInBackground(String... params) {
			// ★ポイント4★ 利用者情報の紐づけにはUUID/cookieを利用する
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

	private class sendDataAsyncTack extends AsyncTask<String, Void, Boolean> {
		private String extMessage = "";

		@Override
		protected Boolean doInBackground(String... params) {
			String url = params[0];
			String id = params[1];
			String nickname = params.length > 2 ? params[2] : null;

			Boolean result = false;
			try {
				JSONObject jsonData = new JSONObject();
				jsonData.put(ID_KEY, id);

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
