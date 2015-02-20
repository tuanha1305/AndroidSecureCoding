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

package org.jssec.android.intent.maliciousactivity;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MaliciousActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.malicious_activity);

		// ActivityManagerを取得する
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// タスクの履歴を最新100件取得する
		List<ActivityManager.RecentTaskInfo> list = activityManager
				.getRecentTasks(100, ActivityManager.RECENT_WITH_EXCLUDED);
		for (ActivityManager.RecentTaskInfo r : list) {
			// ルートActivityに送信されたIntentを取得し、Logに表示する
			Intent intent = r.baseIntent;
			Log.v("baseIntent", intent.toString());
			Log.v("  action:", intent.getAction());
			Log.v("  data:", intent.getDataString());
			if (r.origActivity != null) {
				Log.v("  pkg:", r.origActivity.getPackageName() + r.origActivity.getClassName());
			}
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Set<String> keys = extras.keySet();
				for(String key : keys) {
					Log.v("  extras:", key + "=" + extras.get(key).toString());
				}
			}
		}
	}
}
