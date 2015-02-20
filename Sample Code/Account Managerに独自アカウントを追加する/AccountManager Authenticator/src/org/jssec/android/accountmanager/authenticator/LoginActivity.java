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

package org.jssec.android.accountmanager.authenticator;

import org.jssec.android.accountmanager.webservice.WebService;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class LoginActivity extends AccountAuthenticatorActivity {
	private static final String TAG = AccountAuthenticatorActivity.class.getSimpleName();
	private String mReAuthName = null;
	private EditText mNameEdit = null;
	private EditText mPassEdit = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		// アラートアイコン表示
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.login_activity);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.ic_dialog_alert);

		// widgetを見つけておく
		mNameEdit = (EditText) findViewById(R.id.username_edit);
		mPassEdit = (EditText) findViewById(R.id.password_edit);
		
		// ★ポイント3★ ログイン画面Activityは公開Activityとして他のアプリからの攻撃アクセスを想定する
		// 外部入力はIntent#extrasのString型のRE_AUTH_NAMEだけしか扱わない
		// この外部入力StringはTextEdit#setText()、WebService#login()、new Account()に
		// 引数として渡されるが、どんな文字列が与えられても問題が起きないことを確認している
		mReAuthName = getIntent().getStringExtra(JssecAuthenticator.RE_AUTH_NAME);
		if (mReAuthName != null) {
			// ユーザー名指定でLoginActivityが呼び出されたので、ユーザー名を編集不可とする
			mNameEdit.setText(mReAuthName);
			mNameEdit.setInputType(InputType.TYPE_NULL);
			mNameEdit.setFocusable(false);
			mNameEdit.setEnabled(false);
		}
	}

	// ログインボタン押下時に実行される
	public void handleLogin(View view) {
		String name = mNameEdit.getText().toString();
		String pass = mPassEdit.getText().toString();

		if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pass)) {
			// 入力値が不正である場合の処理
			setResult(RESULT_CANCELED);
			finish();
		}

		// 入力されたアカウント情報によりオンラインサービスにログインする
		WebService web = new WebService();
		String authToken = web.login(name, pass);
		if (TextUtils.isEmpty(authToken)) {
			// 認証が失敗した場合の処理
			setResult(RESULT_CANCELED);
			finish();
		}
		
		//　以下、ログイン成功時の処理

		// ★ポイント5★ アカウント情報や認証トークンなどのセンシティブな情報はログ出力しない
		Log.i(TAG, "WebService login succeeded");


		if (mReAuthName == null) {
			// ログイン成功したアカウントをAccountManagerに登録する
			// ★ポイント6★ Account Managerにパスワードを保存しない
			AccountManager am = AccountManager.get(this);
			Account account = new Account(name, JssecAuthenticator.JSSEC_ACCOUNT_TYPE);
			am.addAccountExplicitly(account, null, null);
			am.setAuthToken(account, JssecAuthenticator.JSSEC_AUTHTOKEN_TYPE, authToken);
			Intent intent = new Intent();
			intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, name);
			intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE,
					JssecAuthenticator.JSSEC_ACCOUNT_TYPE);
			setAccountAuthenticatorResult(intent.getExtras());
			setResult(RESULT_OK, intent);
		} else {
			// 認証トークンを返却する
			Bundle bundle = new Bundle();
			bundle.putString(AccountManager.KEY_ACCOUNT_NAME, name);
			bundle.putString(AccountManager.KEY_ACCOUNT_TYPE,
					JssecAuthenticator.JSSEC_ACCOUNT_TYPE);
			bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			setAccountAuthenticatorResult(bundle);
			setResult(RESULT_OK);
		}
		finish();
	}
}