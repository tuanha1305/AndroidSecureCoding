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

package org.jssec.android.activity.partneruser;

import org.jssec.android.shared.PkgCertWhitelists;
import org.jssec.android.shared.Utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PartnerUserActivity extends Activity {

	// ★ポイント7★ 利用先パートナー限定Activityアプリの証明書がホワイトリストに登録されていることを確認する
	private static PkgCertWhitelists sWhitelists = null;
	private static void buildWhitelists(Context context) {
		boolean isdebug = Utils.isDebuggable(context);
		sWhitelists = new PkgCertWhitelists();
		
		// パートナー限定Activityアプリ org.jssec.android.activity.partneractivity の証明書ハッシュ値を登録
		sWhitelists.add("org.jssec.android.activity.partneractivity", isdebug ?
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
    			"0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255" :
				// keystoreの"my company key"の証明書ハッシュ値
    			"D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA");
		
		// 以下同様に他のパートナー限定Activityアプリを登録...
	}
	private static boolean checkPartner(Context context, String pkgname) {
		if (sWhitelists == null) buildWhitelists(context);
		return sWhitelists.test(context, pkgname);
	}
	
    private static final int REQUEST_CODE = 1;

    // 利用先のパートナー限定Activityに関する情報
    private static final String TARGET_PACKAGE =  "org.jssec.android.activity.partneractivity";
    private static final String TARGET_ACTIVITY = "org.jssec.android.activity.partneractivity.PartnerActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onUseActivityClick(View view) {

        // ★ポイント7★ 利用先パートナー限定Activityアプリの証明書がホワイトリストに登録されていることを確認する
    	if (!checkPartner(this, TARGET_PACKAGE)) {
        	Toast.makeText(this, "利用先 Activity アプリはホワイトリストに登録されていない。", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
        	Intent intent = new Intent();
        	
        	// ★ポイント8★ Activityに送信するIntentには、フラグFLAG_ACTIVITY_NEW_TASKを設定しない
        	
            // ★ポイント9★ 利用先パートナー限定アプリに開示してよい情報をputExtra()を使う場合に限り送信してよい
        	intent.putExtra("PARAM", "パートナーアプリに開示してよい情報");
        	
        	// ★ポイント10★ 明示的Intentによりパートナー限定Activityを呼び出す
        	intent.setClassName(TARGET_PACKAGE, TARGET_ACTIVITY);
        	
        	// ★ポイント11★ startActivityForResult()によりパートナー限定Activityを呼び出す
        	startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e) {
        	Toast.makeText(this, "利用先Activityが見つからない。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;
        
		switch (requestCode) {
		case REQUEST_CODE:
			String result = data.getStringExtra("RESULT");
			
			// ★ポイント12★ パートナー限定アプリからの結果情報であっても、受信Intentの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
        	Toast.makeText(this,
        			String.format("結果「%s」を受け取った。", result), Toast.LENGTH_LONG).show();
			break;
		}
    }
}