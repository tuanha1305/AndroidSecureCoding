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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class JssecAuthenticator extends AbstractAccountAuthenticator {

	public static final String JSSEC_ACCOUNT_TYPE = "org.jssec.android.accountmanager";
	public static final String JSSEC_AUTHTOKEN_TYPE = "webservice";
	public static final String JSSEC_AUTHTOKEN_LABEL = "JSSEC Web Service";
	public static final String RE_AUTH_NAME = "reauth_name";
	
	protected final Context mContext;

	public JssecAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
			String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {

		AccountManager am = AccountManager.get(mContext);
		Account[] accounts = am.getAccountsByType(JSSEC_ACCOUNT_TYPE);
		Bundle bundle = new Bundle();
		if (accounts.length > 0) {
			// 本サンプルコードではアカウントが既に存在する場合はエラーとする
			bundle.putString(AccountManager.KEY_ERROR_CODE, String.valueOf(-1));
			bundle.putString(AccountManager.KEY_ERROR_MESSAGE,
					mContext.getString(R.string.error_account_exists));
		} else {
			// ★ポイント2★ ログイン画面ActivityはAuthenticatorアプリで実装する
			// ★ポイント4★ KEY_INTENTには、ログイン画面Activityのクラス名を指定した明示的Intentを与える
			Intent intent = new Intent(mContext, LoginActivity.class);
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		}
		return bundle;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
			String authTokenType, Bundle options) throws NetworkErrorException {

		Bundle bundle = new Bundle();
		if (accountExist(account)) {
			// ★ポイント4★ KEY_INTENTには、ログイン画面Activityのクラス名を指定した明示的Intentを与える
			Intent intent = new Intent(mContext, LoginActivity.class);
			intent.putExtra(RE_AUTH_NAME, account.name);
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		} else {
			// 指定されたアカウントが存在しない場合はエラーとする
			bundle.putString(AccountManager.KEY_ERROR_CODE, String.valueOf(-2));
			bundle.putString(AccountManager.KEY_ERROR_MESSAGE,
					mContext.getString(R.string.error_account_not_exists));
		}
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return JSSEC_AUTHTOKEN_LABEL;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
			Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
			String authTokenType, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
			String[] features) throws NetworkErrorException {
		Bundle result = new Bundle();
		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
		return result;
	}

	private boolean accountExist(Account account) {
		AccountManager am = AccountManager.get(mContext);
		Account[] accounts = am.getAccountsByType(JSSEC_ACCOUNT_TYPE);
		for (Account ac : accounts) {
			if (ac.equals(account)) {
				return true;
			}
		}
		return false;
	}
}
