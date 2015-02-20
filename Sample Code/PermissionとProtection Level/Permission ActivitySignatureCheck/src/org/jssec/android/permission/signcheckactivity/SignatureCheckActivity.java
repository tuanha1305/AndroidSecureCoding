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

package org.jssec.android.permission.signcheckactivity;

import org.jssec.android.shared.PkgCert;
import org.jssec.android.shared.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class SignatureCheckActivity extends Activity {
	// 自己証明書のハッシュ値
	private static String sMyCertHash = null;
	private static String myCertHash(Context context) {
		if (sMyCertHash == null) {
			if (Utils.isDebuggable(context)) {
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
				sMyCertHash = "0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255";
			} else {
				// keystoreの"my company key"の証明書ハッシュ値
				sMyCertHash = "D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA";
			}
		}
		return sMyCertHash;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// ★ポイント1★ 主要な処理を行うまでの間に、アプリの証明書が開発者の証明書であることを確認する
		if (!PkgCert.test(this, this.getPackageName(), myCertHash(this))) {
			Toast.makeText(this, "自己署名の照合　NG", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		Toast.makeText(this, "自己署名の照合　OK", Toast.LENGTH_LONG).show();
	}
}