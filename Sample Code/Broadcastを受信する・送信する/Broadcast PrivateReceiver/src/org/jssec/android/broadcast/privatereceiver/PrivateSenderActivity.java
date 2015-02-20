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

package org.jssec.android.broadcast.privatereceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PrivateSenderActivity extends Activity {

	public void onSendNormalClick(View view) {
		// ★ポイント4★ 同一アプリ内Receiverはクラス指定の明示的IntentでBroadcast送信する
		Intent intent = new Intent(this, PrivateReceiver.class);

		// ★ポイント5★ 送信先は同一アプリ内Receiverであるため、センシティブな情報を送信してよい
		intent.putExtra("PARAM", "センシティブな情報 from Sender");
		sendBroadcast(intent);
	}
	
	public void onSendOrderedClick(View view) {
		// ★ポイント4★ 同一アプリ内Receiverはクラス指定の明示的IntentでBroadcast送信する
		Intent intent = new Intent(this, PrivateReceiver.class);

		// ★ポイント5★ 送信先は同一アプリ内Receiverであるため、センシティブな情報を送信してよい
		intent.putExtra("PARAM", "センシティブな情報 from Sender");
		sendOrderedBroadcast(intent, null, mResultReceiver, null, 0, null, null);
	}
	
	private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			// ★ポイント6★ 同一アプリ内Receiverからの結果情報であっても、受信データの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
			String data = getResultData();
			PrivateSenderActivity.this.logLine(
					String.format("結果「%s」を受信した。", data));
		}
	};
	
	private TextView mLogView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mLogView = (TextView)findViewById(R.id.logview);
    }
	
	private void logLine(String line) {
		mLogView.append(line);
		mLogView.append("\n");
	}
}