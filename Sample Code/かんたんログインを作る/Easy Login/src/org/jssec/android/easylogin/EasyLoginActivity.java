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

package org.jssec.android.easylogin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class EasyLoginActivity extends Activity {
	// ★ポイント 5★ トークンを送受信するAPIはHTTPSで通信する
	// 実際にサンプルコードで動作確認する際には、以下BASE_URLを書き換えること
	private static final String BASE_URL = "https://easylogin.android.jssec.org";
	private static final String GET_TOKEN_URI = BASE_URL + "/get_token.php";
	private static final String GET_TEXT_URI = BASE_URL + "/get_text.php";
	private static final String PUT_TEXT_URI = BASE_URL + "/put_text.php";
	private static final String DEL_TOKEN_URI = BASE_URL + "/del_token.php";

	private static final String API_KEY = "api_key=0123456789";
	private static final String TEXT_KEY = "text";
	private static final String TOKEN_KEY = "token";

	private static final String EASYLOGIN_PREF_NAME = "easylogin_preference";
	private static final String TOKEN_PREF_KEY = "token";
	private static final String NO_ALERT_PREF_KEY = "no_alert";

	private static final String USER_AGREE = "user_agree";

	EditText memo = null;
	Button send_text = null;
	Button del_token = null;
	AlertDialog alert_dlg = null;
	CheckBox alert_cb = null;
	Boolean userAgree = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		memo = (EditText) findViewById(R.id.memo);
		send_text = (Button) findViewById(R.id.send);
		del_token = (Button) findViewById(R.id.del_token);

		// ★ポイント1★ 端末を譲渡・売却・破棄する際のトークン削除を促す警告を表示しユーザーの同意を得る
		// ユーザー同意状態が保存されている場合は、復帰する
		if (savedInstanceState != null)
			userAgree = savedInstanceState.getBoolean(USER_AGREE);
		if ((getSharedPreferences(EASYLOGIN_PREF_NAME, MODE_PRIVATE).getString(
				NO_ALERT_PREF_KEY, null) == null)
				&& !userAgree) {
			// 「次回以降表示しない」が選択されるまで、警告ダイアログを表示する
			View layout = getLayoutInflater().inflate(R.layout.alert, null);
			alert_dlg = new AlertDialog.Builder(this).setView(layout).show();
			alert_dlg.setOnDismissListener(new AlertDialogListner());
			alert_cb = (CheckBox) alert_dlg.findViewById(R.id.alert_cb);
		} else {
			// 以前に「次回以降表示しない」が選択されている場合、サーバーに保存したテキストデータを取得
			new GetTextAsyncTask().execute();
		}
	}

	public void onAlertOKClick(View view) {
		// ★ポイント1★ 端末を譲渡・売却・破棄する際のトークン削除を促す警告を表示しユーザーの同意を得る
		// このメソッドはユーザーが警告ダイアログで「アプリ開始」を選んだときに呼ばれる。
		userAgree = true;
		alert_dlg.cancel();
	}

	public void onAlertCancelClick(View view) {
		// ★ポイント1★ 端末を譲渡・売却・破棄する際のトークン削除を促す警告を表示しユーザーの同意を得る
		// このメソッドはユーザーが警告ダイアログで「アプリ終了」を選んだときに呼ばれる。
		alert_dlg.cancel();
	}

	private class AlertDialogListner implements OnDismissListener {
		// ★ポイント1★ 端末を譲渡・売却・破棄する際のトークン削除を促す警告を表示しユーザーの同意を得る
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (userAgree) {
				// ユーザーがアプリ起動を同意した場合
				if (alert_cb != null && alert_cb.isChecked()) {
					// 「次回以降表示しない」が選択がされたため
					// 警告ダイアログを表示しないように設定値を保存
					getSharedPreferences(EASYLOGIN_PREF_NAME, MODE_PRIVATE)
							.edit().putString(NO_ALERT_PREF_KEY, "No display")
							.commit();
				}
				// サーバーに保存したテキストデータを取得
				new GetTextAsyncTask().execute();
			} else {
				// ユーザーがアプリ起動に同意しなかった場合は、アプリを終了する
				finish();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// ユーザー同意の状態を保存
		super.onSaveInstanceState(outState);
		outState.putBoolean(USER_AGREE, userAgree);
	}

	public void onSendTextClick(View view) {
		// サーバーにテキストデータを送信
		new PutTextAsyncTask().execute(memo.getText().toString());
	}

	public void onDeleteTokenClick(View view) {
		// サーバーと端末からトークンを削除
		new DelTokenAsyncTask().execute();
	}

	private class GetTextAsyncTask extends AsyncTask<String, Void, String> {
		private String extMessage = "";

		@Override
		protected String doInBackground(String... params) {
			String text = "";
			try {
				// ★ポイント2★ アプリ初回起動時にサーバーからトークンを取得し、アプリ内にMODE_PRIVATEで保存する
				SharedPreferences sp = getSharedPreferences(
						EASYLOGIN_PREF_NAME, MODE_PRIVATE);
				String token = sp.getString(TOKEN_PREF_KEY, null);
				if (token == null) {
					// SharedPreferences内にトークンが存在しないのでアプリ初回起動時である。
					// ゆえにサーバーからトークンを取り寄せる。
					token = NetworkUtil.getCookie(GET_TOKEN_URI, API_KEY, TOKEN_KEY);

					// 取り寄せたトークンをSharedPreferencesに保存する。
					sp.edit().putString(TOKEN_PREF_KEY, token).commit();
				}

				// ★ポイント3★ ユーザー識別が必要なサーバーAPIを利用するときにはアプリ内に保存したトークンを送信する
				text = NetworkUtil.getJSON(GET_TEXT_URI, token, TEXT_KEY);
			} catch (IOException e) {
				// 証明書エラーなどの例外をキャッチする
				extMessage = e.toString();
				text = null;
			} catch (JSONException e) {
				extMessage = e.toString();
				text = null;
			}
			return text;
		}

		@Override
		protected void onPostExecute(final String data) {
			String status = (data != null) ? "success" : "error";
			Toast.makeText(
					EasyLoginActivity.this,
					this.getClass().getSimpleName() + " - " + status
							+ " : read text." + extMessage, Toast.LENGTH_SHORT)
					.show();

			// 取得したデータをEditTextに表示
			if (data != null)
				memo.setText(data);

			// サーバーへのデータ送信を有効にする
			send_text.setEnabled(true);
			del_token.setEnabled(true);
		}
	}

	private class PutTextAsyncTask extends AsyncTask<String, Void, Boolean> {
		private String extMessage = "";

		@Override
		protected Boolean doInBackground(String... param) {
			String text = param[0];
			Boolean result = false;
			try {
				// ★ポイント2★ アプリ初回起動時にサーバーからトークンを取得し、アプリ内にMODE_PRIVATEで保存する
				SharedPreferences sp = getSharedPreferences(
						EASYLOGIN_PREF_NAME, MODE_PRIVATE);
				String token = sp.getString(TOKEN_PREF_KEY, null);
				if (token == null) {
					// SharedPreferences内にトークンが存在しないのでアプリ初回起動時である。
					// ゆえにサーバーからトークンを取り寄せる。
					token = NetworkUtil.getCookie(GET_TOKEN_URI, API_KEY,
							TOKEN_KEY);

					// 取り寄せたトークンをSharedPreferencesに保存する。
					sp.edit().putString(TOKEN_PREF_KEY, token).commit();
				}

				// ★ポイント3★ ユーザー識別が必要なサーバーAPIを利用するときにはアプリ内に保存したトークンを送信する
				JSONObject jsonData = new JSONObject();
				jsonData.put(TEXT_KEY, text);
				NetworkUtil.sendJSON(PUT_TEXT_URI, token, jsonData.toString());

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
			String status = result ? "success" : "error";
			Toast.makeText(
					EasyLoginActivity.this,
					this.getClass().getSimpleName() + " - " + status
							+ " : put text." + extMessage, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class DelTokenAsyncTask extends AsyncTask<String, Void, Boolean> {
		private String extMessage = "";

		@Override
		protected Boolean doInBackground(String... param) {
			Boolean result = false;
			try {
				// ★ポイント4★ トークンおよびユーザー情報をサーバーおよびアプリ内から削除できるようにする
				SharedPreferences sp = getSharedPreferences(
						EASYLOGIN_PREF_NAME, MODE_PRIVATE);
				String token = sp.getString(TOKEN_PREF_KEY, null);
				if (token != null) {
					// SharedPreferences内にトークンが存在する
					// ゆえに、サーバーからトークンおよびユーザー情報を削除する
					NetworkUtil.getCookie(DEL_TOKEN_URI, token, TOKEN_KEY);

					// 合わせて、端末に保存したトークンを削除する
					sp.edit().remove(TOKEN_PREF_KEY).commit();
				}

				result = true;
			} catch (IOException e) {
				// 証明書エラーなどの例外をキャッチする
				extMessage = e.toString();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			String status = result ? "success" : "error";
			Toast.makeText(
					EasyLoginActivity.this,
					this.getClass().getSimpleName() + " - " + status
							+ " : delete token and data." + extMessage,
					Toast.LENGTH_SHORT).show();
		}
	}
}