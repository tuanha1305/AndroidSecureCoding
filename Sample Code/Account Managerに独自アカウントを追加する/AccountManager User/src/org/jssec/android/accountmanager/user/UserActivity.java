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

package org.jssec.android.accountmanager.user;

import java.io.IOException;

import org.jssec.android.shared.PkgCert;
import org.jssec.android.shared.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UserActivity extends Activity {

	// 利用するAuthenticatorの情報
	private static final String JSSEC_ACCOUNT_TYPE = "org.jssec.android.accountmanager";
	private static final String JSSEC_TOKEN_TYPE = "webservice";
	private TextView mLogView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity);
		
		mLogView = (TextView)findViewById(R.id.logview);
	}

	public void addAccount(View view) {
		logLine();
		logLine("新しいアカウントを追加します");

		// ★ポイント1★ Authenticatorが正規のものであることを確認してからアカウント処理を実施する
		if (!checkAuthenticator()) return;

		AccountManager am = AccountManager.get(this);
		am.addAccount(JSSEC_ACCOUNT_TYPE, JSSEC_TOKEN_TYPE, null, null, this,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle result = future.getResult();
							String type = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
							String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
							if (type != null && name != null) {
								logLine("以下のアカウントを追加しました：");
								logLine("　　アカウント種別: %s", type);
								logLine("　　アカウント名: %s", name);
							} else {
								String code = result.getString(AccountManager.KEY_ERROR_CODE);
								String msg = result.getString(AccountManager.KEY_ERROR_MESSAGE);
								logLine("アカウントが追加できませんでした");
								logLine("  エラーコード %s: %s", code, msg);
							}
						} catch (OperationCanceledException e) {
						} catch (AuthenticatorException e) {
						} catch (IOException e) {
						}
					}
				},
				null);
	}

	public void getAuthToken(View view) {
		logLine();
		logLine("トークンを取得します");

		// ★ポイント1★ Authenticatorが正規のものであることを確認してからアカウント処理を実施する
		if (!checkAuthenticator()) return;

		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(JSSEC_ACCOUNT_TYPE);
		if (accounts.length > 0) {
			Account account = accounts[0];
			am.getAuthToken(account, JSSEC_TOKEN_TYPE, null, this,
					new AccountManagerCallback<Bundle>() {
						@Override
						public void run(AccountManagerFuture<Bundle> future) {
							try {
								Bundle result = future.getResult();
								String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
								String authtoken = result.getString(AccountManager.KEY_AUTHTOKEN);
								logLine("  %sさんのトークン:", name);
								if (authtoken != null) {
									logLine("    %s", authtoken);
								} else {
									logLine("    取得できませんでした");
								}
							} catch (OperationCanceledException e) {
								logLine("  例外: %s",e.getClass().getName());
							} catch (AuthenticatorException e) {
								logLine("  例外: %s",e.getClass().getName());
							} catch (IOException e) {
								logLine("  例外: %s",e.getClass().getName());
							}
						}
					}, null);
		} else {
			logLine("アカウントが登録されていません");
		}
	}

	// ★ポイント1★ Authenticatorが正規のものであることを確認する
	private boolean checkAuthenticator() {
		AccountManager am = AccountManager.get(this);
		String pkgname = null;
		for (AuthenticatorDescription ad : am.getAuthenticatorTypes()) {
			if (JSSEC_ACCOUNT_TYPE.equals(ad.type)) {
				pkgname = ad.packageName;
				break;
			}
		}
		
		if (pkgname == null) {
			logLine("Authenticatorが見つかりません");
			return false;
		}
		
		logLine("  アカウントタイプ： %s", JSSEC_ACCOUNT_TYPE);
		logLine("  Authenticatorのパッケージ名：");
		logLine("    %s", pkgname);

		if (!PkgCert.test(this, pkgname, getTrustedCertificateHash(this))) {
			logLine("  正規のAuthenticatorではありません（証明書不一致）");
			return false;
		}
		
		logLine("  正規のAuthenticatorです");
		return true;
	}

	// 正規のAuthenticatorアプリの証明書ハッシュ値
	// サンプルアプリ JSSEC CertHash Checker で証明書ハッシュ値は確認できる
	private String getTrustedCertificateHash(Context context) {
		if (Utils.isDebuggable(context)) {
			// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
			return "0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255";
		} else {
			// keystoreの"my company key"の証明書ハッシュ値
			return "D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA";
		}
	}
	
	private void log(String str) {
		mLogView.append(str);
	}
	
	private void logLine(String line) {
		log(line + "\n");
	}
	
	private void logLine(String fmt, Object... args) {
		logLine(String.format(fmt, args));
	}

	private void logLine() {
		log("\n");
	}
}