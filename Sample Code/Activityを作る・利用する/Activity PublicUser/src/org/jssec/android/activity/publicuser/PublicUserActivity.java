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

package org.jssec.android.activity.publicuser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PublicUserActivity extends Activity {

    private static final int REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onUseActivityClick(View view) {
    	
    	try {
	    	// ★ポイント4★ センシティブな情報を送信してはならない
	        Intent intent = new Intent("org.jssec.android.activity.MY_ACTION");
	        intent.putExtra("PARAM", "センシティブではない情報");
	        startActivityForResult(intent, REQUEST_CODE);
    	} catch (ActivityNotFoundException e) {
        	Toast.makeText(this, "利用先Activityが見つからない。", Toast.LENGTH_LONG).show();
    	}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

		// ★ポイント5★ 結果を受け取る場合、結果データの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
        if (resultCode != RESULT_OK) return;
		switch (requestCode) {
		case REQUEST_CODE:
			String result = data.getStringExtra("RESULT");
        	Toast.makeText(this, String.format("結果「%s」を受け取った。", result), Toast.LENGTH_LONG).show();
			break;
		}
	}
}