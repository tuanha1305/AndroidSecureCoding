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

package org.jssec.android.https.privatecertificate;

import java.security.KeyStore;

import javax.net.ssl.SSLException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;

public abstract class PrivateCertificateHttpsGet extends AsyncTask<String, Void, Object> {

	private Context mContext;
	
	public PrivateCertificateHttpsGet(Context context) {
		mContext = context;
	}
	
	@Override
	protected Object doInBackground(String... params) {
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		try {
			// ★ポイント1★ プライベート証明書でサーバー証明書を検証する
			// assetsに格納しておいたプライベート証明書だけを含むKeyStoreをclientに設定
			KeyStore ks = KeyStoreUtil.getEmptyKeyStore();
			KeyStoreUtil.loadX509Certificate(ks,
					mContext.getResources().getAssets().open("cacert.crt"));
			Scheme sch = new Scheme("https", new SSLSocketFactory(ks), 443);
			client.getConnectionManager().getSchemeRegistry().register(sch);
			
			// ★ポイント2★ URIはhttps://で始める
			// ★ポイント3★ 送信データにセンシティブな情報を含めてよい
			HttpGet request = new HttpGet(params[0]);
			HttpResponse response = client.execute(request);
			checkResponse(response);
			
			// ★ポイント4★ 受信データを接続先サーバーと同じ程度に信用してよい
			return EntityUtils.toByteArray(response.getEntity());
		} catch(SSLException e) {
			// ★ポイント5★ SSLExceptionに対しユーザーに通知する等の適切な例外処理をする
			// サンプルにつき例外処理は割愛
			return e;
		} catch(Exception e) {
			return e;
		} finally {
			// 確実にHttpClientをshutdownする
			client.getConnectionManager().shutdown();
		}
	}
	
	private void checkResponse(HttpResponse response) throws HttpException {
		int statusCode = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK != statusCode) {
			throw new HttpException("HttpStatus: " + statusCode);
		}
	}
}
