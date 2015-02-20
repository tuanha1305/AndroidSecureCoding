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

package org.jssec.android.https.imagesearch;

import javax.net.ssl.SSLException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.net.Uri;
import android.os.AsyncTask;

public abstract class HttpsImageSearch extends AsyncTask<String, Void, Object> {

	@Override
	protected Object doInBackground(String... params) {
		
		// HttpClientを2回のGETリクエストに使いまわすので、finallyでshutdownする
		DefaultHttpClient client = new DefaultHttpClient();
		
		try {
			// --------------------------------------------------------
			// 通信1回目：画像検索する
			// --------------------------------------------------------
			
			// ★ポイント1★ URIはhttps://で始める
			// ★ポイント2★ 送信データにセンシティブな情報を含めてよい
			String search_url = Uri
					.parse("https://ajax.googleapis.com/ajax/services/search/images?v=1.0")
					.buildUpon()
					.appendQueryParameter("q", params[0])
					.build().toString();
			HttpGet request = new HttpGet(search_url);
			HttpResponse response = client.execute(request);
			checkResponse(response);
			
			// ★ポイント3★ HTTPS接続したサーバーからのデータであっても、受信データの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
			String result_json = EntityUtils.toString(response.getEntity(), "UTF-8");
			String image_url = new JSONObject(result_json).getJSONObject("responseData")
					.getJSONArray("results").getJSONObject(0).getString("url");
			
			// --------------------------------------------------------
			// 通信2回目：画像を取得する
			// --------------------------------------------------------
			
			// ★ポイント1★ URIはhttps://で始める
			// ★ポイント2★ 送信データにセンシティブな情報を含めてよい
			request = new HttpGet(image_url);
			response = client.execute(request);
			checkResponse(response);
			
			// ★ポイント3★ HTTPS接続したサーバーからのデータであっても、受信データの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
			return EntityUtils.toByteArray(response.getEntity());
		} catch(SSLException e) {
			// ★ポイント4★ SSLExceptionに対してアプリに適した例外処理を行う
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
