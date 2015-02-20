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

package org.jssec.android.broadcast.publicreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PublicReceiver extends BroadcastReceiver {

	private static final String MY_BROADCAST_PUBLIC =
		"org.jssec.android.broadcast.MY_BROADCAST_PUBLIC";
	
	public boolean isDynamic = false;
	private String getName() {
		return isDynamic ? "公開動的 Broadcast Receiver" : "公開静的 Broadcast Receiver";
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// ★ポイント2★ 受信Intentの安全性を確認する
    	// 公開Broadcast Receiverであるため利用元アプリがマルウェアである可能性がある。
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
		if (MY_BROADCAST_PUBLIC.equals(intent.getAction())) {
			String param = intent.getStringExtra("PARAM");
	    	Toast.makeText(context,
	    			String.format("%s:\n「%s」を受信した。", getName(), param),
	    			Toast.LENGTH_SHORT).show();
		}
		
		// ★ポイント3★ 結果を返す場合、センシティブな情報を含めない
    	// 公開Broadcast Receiverであるため、
    	// Broadcastの送信元アプリがマルウェアである可能性がある。
    	// マルウェアに取得されても問題のない情報であれば結果として返してもよい。
		setResultCode(Activity.RESULT_OK);
		setResultData(String.format("センシティブではない情報 from %s", getName()));
		abortBroadcast();
	}
}
