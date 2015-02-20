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

package org.jssec.android.log.proguard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ProGuardActivity extends Activity {

	final static String LOG_TAG = "ProGuardActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_proguard);

		// ★ポイント1★ センシティブな情報はLog.e()/w()/i()、System.out/errで出力しない
		Log.e(LOG_TAG, "センシティブではない情報(ERROR)");
		Log.w(LOG_TAG, "センシティブではない情報(WARN)");
		Log.i(LOG_TAG, "センシティブではない情報(INFO)");

		// ★ポイント2★ センシティブな情報をログ出力する場合はLog.d()/v()で出力する
		// ★ポイント3★ Log.d()/v()の呼び出しでは戻り値を使用しない(代入や比較)
		Log.d(LOG_TAG, "センシティブな情報(DEBUG)");
		Log.v(LOG_TAG, "センシティブな情報(VERBOSE)");
	}
}
