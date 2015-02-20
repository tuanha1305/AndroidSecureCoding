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

package org.jssec.android.activity.partneractivity;

import org.jssec.android.shared.PkgCertWhitelists;
import org.jssec.android.shared.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PartnerActivity extends Activity {
	
	// ★ポイント4★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
	private static PkgCertWhitelists sWhitelists = null;
	private static void buildWhitelists(Context context) {
		boolean isdebug = Utils.isDebuggable(context);
		sWhitelists = new PkgCertWhitelists();
		
		// パートナーアプリ org.jssec.android.activity.partneruser の証明書ハッシュ値を登録
		sWhitelists.add("org.jssec.android.activity.partneruser", isdebug ?
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
    			"0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255" :
				// keystoreの"partner key"の証明書ハッシュ値
    			"1F039BB5 7861C27A 3916C778 8E78CE00 690B3974 3EB8259F E2627B8D 4C0EC35A");
		
		// 以下同様に他のパートナーアプリを登録...
	}
	private static boolean checkPartner(Context context, String pkgname) {
		if (sWhitelists == null) buildWhitelists(context);
		return sWhitelists.test(context, pkgname);
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // ★ポイント4★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(this, getCallingActivity().getPackageName())) {
        	Toast.makeText(this, "利用元アプリはパートナーアプリではない。", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }
        
		// ★ポイント5★ パートナーアプリからのIntentであっても、受信Intentの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
        Toast.makeText(this, "パートナーアプリからアクセスあり", Toast.LENGTH_LONG).show();
    }
    
	public void onReturnResultClick(View view) {

		// ★ポイント6★ パートナーアプリに開示してよい情報に限り返送してよい
		Intent intent = new Intent();
		intent.putExtra("RESULT", "パートナーアプリに開示してよい情報");
		setResult(RESULT_OK, intent);
		finish();
	}
}