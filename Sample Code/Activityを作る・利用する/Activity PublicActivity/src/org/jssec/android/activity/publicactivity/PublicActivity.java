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

package org.jssec.android.activity.publicactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PublicActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		// ★ポイント2★ 受信Intentの安全性を確認する
    	// 公開Activityであるため利用元アプリがマルウェアである可能性がある。
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
		String param = getIntent().getStringExtra("PARAM");
    	Toast.makeText(this, String.format("パラメータ「%s」を受け取った。", param), Toast.LENGTH_LONG).show();
	}

	public void onReturnResultClick(View view) {
		
		// ★ポイント3★ 結果を返す場合、センシティブな情報を含めない
    	// 公開Activityであるため利用元アプリがマルウェアである可能性がある。
    	// マルウェアに取得されても問題のない情報であれば結果として返してもよい。

		Intent intent = new Intent();
		intent.putExtra("RESULT", "センシティブではない情報");
		setResult(RESULT_OK, intent);
		finish();
	}
}