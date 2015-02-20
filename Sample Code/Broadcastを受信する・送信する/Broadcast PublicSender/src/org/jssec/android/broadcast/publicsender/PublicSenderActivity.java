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

package org.jssec.android.broadcast.publicsender;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PublicSenderActivity extends Activity {
	
	private static final String MY_BROADCAST_PUBLIC =
		"org.jssec.android.broadcast.MY_BROADCAST_PUBLIC";
	
	public void onSendNormalClick(View view) {
		// ★ポイント4★ センシティブな情報を送信してはならない
		Intent intent = new Intent(MY_BROADCAST_PUBLIC);
		intent.putExtra("PARAM", "センシティブではない情報 from Sender");
		sendBroadcast(intent);
	}
	
	public void onSendOrderedClick(View view) {
		// ★ポイント4★ センシティブな情報を送信してはならない
		Intent intent = new Intent(MY_BROADCAST_PUBLIC);
		intent.putExtra("PARAM", "センシティブではない情報 from Sender");
		sendOrderedBroadcast(intent, null, mResultReceiver, null, 0, null, null);
	}
	
	public void onSendStickyClick(View view) {
		// ★ポイント4★ センシティブな情報を送信してはならない
		Intent intent = new Intent(MY_BROADCAST_PUBLIC);
		intent.putExtra("PARAM", "センシティブではない情報 from Sender");
		sendStickyBroadcast(intent);
	}

	public void onSendStickyOrderedClick(View view) {
		// ★ポイント4★ センシティブな情報を送信してはならない
		Intent intent = new Intent(MY_BROADCAST_PUBLIC);
		intent.putExtra("PARAM", "センシティブではない情報 from Sender");
		sendStickyOrderedBroadcast(intent, mResultReceiver, null, 0, null, null);
	}
	
	public void onRemoveStickyClick(View view) {
		Intent intent = new Intent(MY_BROADCAST_PUBLIC);
		removeStickyBroadcast(intent);
	}

	private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			// ★ポイント5★ 結果を受け取る場合、結果データの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
			String data = getResultData();
			PublicSenderActivity.this.logLine(
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