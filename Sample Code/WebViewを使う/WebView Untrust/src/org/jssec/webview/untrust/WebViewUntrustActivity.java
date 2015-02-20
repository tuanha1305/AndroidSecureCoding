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

package org.jssec.webview.untrust;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class WebViewUntrustActivity extends Activity {
	/*
	 * 自社管理以外のコンテンツを表示する (簡易ブラウザとして機能するサンプルプログラム)
	 */

	private EditText textUrl;
	private Button buttonGo;
	private WebView webView;

	// この Activity が独自に URL リクエストをハンドリングできるようにするために定義
	private class WebViewUnlimitedClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView webView, String url) {
			webView.loadUrl(url);
			textUrl.setText(url);
			return true;
		}

		// Webページの読み込み開始処理
		@Override
		public void onPageStarted(WebView webview, String url, Bitmap favicon) {
			buttonGo.setEnabled(false);
			textUrl.setText(url);
		}

		// SSL通信で問題があるとエラーダイアログを表示し、
		// 接続を中止する
		@Override
		public void onReceivedSslError(WebView webview,
				SslErrorHandler handler, SslError error) {
			// ★ポイント 1★ HTTPS 通信の場合にはSSL通信のエラーを適切にハンドリングする
			AlertDialog errorDialog = createSslErrorDialog(error);
			errorDialog.show();
			handler.cancel();
			textUrl.setText(webview.getUrl());
			buttonGo.setEnabled(true);
		}

		// Webページのloadが終わったら表示されたページのURLをEditTextに表示させる
		@Override
		public void onPageFinished(WebView webview, String url) {
			textUrl.setText(url);
			buttonGo.setEnabled(true);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewUnlimitedClient());

		// ★ポイント 2★ JavaScriptを有効にしない
		// デフォルトの設定でJavaScript無効となっているが、明示的に無効化する
		webView.getSettings().setJavaScriptEnabled(false);

		webView.loadUrl(getString(R.string.texturl));
		textUrl = (EditText) findViewById(R.id.texturl);
		buttonGo = (Button) findViewById(R.id.go);
	}

	public void onClickButtonGo(View v) {
		webView.loadUrl(textUrl.getText().toString());
	}

	private AlertDialog createSslErrorDialog(SslError error) {
		// ダイアログに表示するエラーメッセージ
		String errorMsg = createErrorMessage(error);
		// ダイアログのOKボタン押下時の挙動
		DialogInterface.OnClickListener onClickOk = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setResult(RESULT_OK);
			}
		};
		// ダイアログの作成
		AlertDialog dialog = new AlertDialog.Builder(
				WebViewUntrustActivity.this).setTitle("SSL接続エラー")
				.setMessage(errorMsg).setPositiveButton("OK", onClickOk)
				.create();
		return dialog;
	}

	private String createErrorMessage(SslError error) {
		SslCertificate cert = error.getCertificate();
		StringBuilder result = new StringBuilder()
		.append("サイトのセキュリティ証明書が信頼できません。接続を終了しました。\n\nエラーの原因\n");
		switch (error.getPrimaryError()) {
		case SslError.SSL_EXPIRED:
			result.append("証明書の有効期限が切れています。\n\n終了時刻=")
			.append(cert.getValidNotAfter());
			return result.toString();
		case SslError.SSL_IDMISMATCH:
			result.append("ホスト名が一致しません。\n\nCN=")
			.append(cert.getIssuedTo().getCName());
			return result.toString();
		case SslError.SSL_NOTYETVALID:
			result.append("証明書はまだ有効ではありません\n\n開始時刻=")
			.append(cert.getValidNotBefore());
			return result.toString();
		case SslError.SSL_UNTRUSTED:
			result.append("証明書を発行した認証局が信頼できません\n\n認証局\n")
			.append(cert.getIssuedBy().getDName());
			return result.toString();
		default:
			result.append("原因不明のエラーが発生しました");
			return result.toString();
		}
	}
}
