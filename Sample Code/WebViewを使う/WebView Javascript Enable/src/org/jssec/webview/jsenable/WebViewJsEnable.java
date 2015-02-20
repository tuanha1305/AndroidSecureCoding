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

package org.jssec.webview.jsenable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class WebViewJsEnable extends Activity {
	private EditText textUrl;
	private Button buttonGo;
	private WebView webView;
	private String TAG = "WebViewJsEnable";

	// javascriptを有効にしてよいURLはhttpsのみとする
	// このやり方ではonPageStarted()呼び出しをページ切り替えのトリガーとして使用しているが、
	//frameやiframeをつかったページではonPageStarted()を呼び出さない。、
	String[] TARGET = { "https://developer.android.com/",
			"https://www.google.co.jp/" };

	// この Activity が独自に URL リクエストをハンドリングできるようにするために定義
	private class WebViewJsEnableClient extends WebViewClient {
		// javascriptを実行してよいURLかどうか確認する
		private boolean isTargetURL(String url) {
			for (String targetURL : TARGET) {

				Log.w(TAG, url + " (" + url.length() + ") " + targetURL);
				if (url.startsWith(targetURL)) {
					Log.w(TAG, url + " (" + url.length() + ") " + targetURL
							+ " TRUE");
					return true;
				}
			}
			Log.w(TAG, url + " (" + url.length() + ")  false");
			return false;
		}

		// Webページの読み込み開始処理
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			webView = (WebView) findViewById(R.id.webview);
			if (isTargetURL(url)) {
				webView.getSettings().setJavaScriptEnabled(true);
			} else {
				webView.getSettings().setJavaScriptEnabled(false);
			}
			// ページ読み込み中はEditTextに読み込んでいるURLと"Loding "を表示させ、文字色を青にする
			textUrl.setTextColor(Color.BLUE);
			textUrl.setText("Loding " + url);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			Log.w(TAG, "onLoadResource " + url + " "
					+ webView.getSettings().getJavaScriptEnabled());
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			// Webページの読み込みが終わったら、EditTextに読み込み終わったURLをEditTextに表示させ、文字色を黒にする。
			textUrl.setTextColor(Color.BLACK);
			textUrl.setText(url);
			Log.w(TAG, "onPageFinished " + url + " "
					+ webView.getSettings().getJavaScriptEnabled());
		}

		// SSLで問題があると2.2.2では白いページ、4.0.4では「ページが見つかりませんでした」となり
		// わかりづらいので、setWebViewClientをオーバーライドしてダイアログを表示させる
		@Override
		public void onReceivedSslError(final WebView view,
				SslErrorHandler handler, SslError error) {
			super.onReceivedSslError(view, handler, error);
			AlertDialog.Builder ad = new AlertDialog.Builder(
					WebViewJsEnable.this);
			ad.setTitle("SSL接続エラー");
			ad.setMessage("SSL接続で問題が発生しました:\n" + error);
			ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					setResult(RESULT_OK);
					textUrl.setText(view.getUrl());
				}
			});
			ad.create();
			ad.show();
			Log.e(TAG, "SSL接続で問題が発生しました:\n" + error.toString());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.main);
		webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewJsEnableClient());
		webView.loadUrl(getString(R.string.texturl));
		textUrl = (EditText) findViewById(R.id.texturl);
		textUrl.setText(getString(R.string.texturl));
		buttonGo = (Button) findViewById(R.id.go);
		buttonGo.setText("Go");
		buttonGo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				webView.loadUrl(textUrl.getText().toString());
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		} else if ((keyCode == KeyEvent.KEYCODE_BACK) && !webView.canGoBack()) {
			this.finish();
			return true;
		} else if ((keyCode == KeyEvent.KEYCODE_MENU) && webView.canGoForward()) {
			webView.goForward();
			return true;
		} else if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(findViewById(R.id.webview)
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
