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

package org.jssec.webview.assets;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewAssetsActivity extends Activity {
	/**
	 * assets内のコンテンツを表示する
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		WebView webView = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = webView.getSettings();

		// ★ポイント1★ assetsとresディレクトリ以外の場所に配置したファイルへのアクセスを禁止にする
		webSettings.setAllowFileAccess(false);

		// ★ポイント2★ JavaScriptを有効にしてよい
		webSettings.setJavaScriptEnabled(true);

		// assets内に配置したコンテンツを表示する
		webView.loadUrl("file:///android_asset/sample/index.html");
	}
}
