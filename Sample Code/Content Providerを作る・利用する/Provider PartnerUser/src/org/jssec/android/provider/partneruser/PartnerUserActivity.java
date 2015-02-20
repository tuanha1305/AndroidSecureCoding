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

package org.jssec.android.provider.partneruser;

import org.jssec.android.shared.PkgCertWhitelists;
import org.jssec.android.shared.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PartnerUserActivity extends Activity {

	// 利用先のContent Provider情報
	private static final String AUTHORITY = "org.jssec.android.provider.partnerprovider";
	private interface Address {
		public static final String PATH = "addresses";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
	}

	// ★ポイント5★ 利用先パートナー限定Content Providerアプリの証明書がホワイトリストに登録されていることを確認する
	private static PkgCertWhitelists sWhitelists = null;
	private static void buildWhitelists(Context context) {
		boolean isdebug = Utils.isDebuggable(context);
		sWhitelists = new PkgCertWhitelists();

		// パートナー限定Content Providerアプリ org.jssec.android.provider.partnerprovider の証明書ハッシュ値を登録
		sWhitelists.add("org.jssec.android.provider.partnerprovider", isdebug ?
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
    			"0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255" :
				// keystoreの"my company key"の証明書ハッシュ値
    			"D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA");

		// 以下同様に他のパートナー限定Content Providerアプリを登録...
	}
	private static boolean checkPartner(Context context, String pkgname) {
		if (sWhitelists == null) buildWhitelists(context);
		return sWhitelists.test(context, pkgname);
	}
	// uriをAUTHORITYとするContent Providerのパッケージ名を取得
    private String providerPkgname(Uri uri) {
    	String pkgname = null;
    	ProviderInfo pi = getPackageManager().resolveContentProvider(uri.getAuthority(), 0);
    	if (pi != null) pkgname = pi.packageName;
    	return pkgname;
    }

    public void onQueryClick(View view) {

    	logLine("[Query]");

    	// ★ポイント5★ 利用先パートナー限定Content Providerアプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(this, providerPkgname(Address.CONTENT_URI))) {
        	logLine("  利用先 Content Provider アプリはホワイトリストに登録されていない。");
        	return;
        }

    	// ★ポイント6★ パートナー限定Content Providerアプリに開示してよい情報に限りリクエストに含めてよい
        Cursor cursor = null;
        try {
        	cursor = getContentResolver().query(Address.CONTENT_URI, null, null, null, null);

	        // ★ポイント7★ パートナー限定Content Providerアプリからの結果であっても、結果データの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
	        if (cursor == null) {
	            logLine("  null cursor");
	        } else {
	            boolean moved = cursor.moveToFirst();
	            while (moved) {
	            	logLine(String.format("  %d, %s", cursor.getInt(0), cursor.getString(1)));
	                moved = cursor.moveToNext();
	            }
	        }
        }
        finally {
        	if (cursor != null) cursor.close();
        }
    }

    public void onInsertClick(View view) {

    	logLine("[Insert]");

    	// ★ポイント5★ 利用先パートナー限定Content Providerアプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(this, providerPkgname(Address.CONTENT_URI))) {
        	logLine("  利用先 Content Provider アプリはホワイトリストに登録されていない。");
        	return;
        }

    	// ★ポイント6★ パートナー限定Content Providerアプリに開示してよい情報に限りリクエストに含めてよい
    	ContentValues values = new ContentValues();
    	values.put("pref", "東京都");
        Uri uri = getContentResolver().insert(Address.CONTENT_URI, values);

        // ★ポイント7★ パートナー限定Content Providerアプリからの結果であっても、結果データの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
        logLine("  uri:" + uri);
    }

    public void onUpdateClick(View view) {

    	logLine("[Update]");

    	// ★ポイント5★ 利用先パートナー限定Content Providerアプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(this, providerPkgname(Address.CONTENT_URI))) {
        	logLine("  利用先 Content Provider アプリはホワイトリストに登録されていない。");
        	return;
        }

    	// ★ポイント6★ パートナー限定Content Providerアプリに開示してよい情報に限りリクエストに含めてよい
    	ContentValues values = new ContentValues();
    	values.put("pref", "東京都");
    	String where = "_id = ?";
    	String[] args = { "4" };
        int count = getContentResolver().update(Address.CONTENT_URI, values, where, args);

        // ★ポイント7★ パートナー限定Content Providerアプリからの結果であっても、結果データの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
        logLine(String.format("  %s records updated", count));
    }

    public void onDeleteClick(View view) {

    	logLine("[Delete]");

    	// ★ポイント5★ 利用先パートナー限定Content Providerアプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(this, providerPkgname(Address.CONTENT_URI))) {
        	logLine("  利用先 Content Provider アプリはホワイトリストに登録されていない。");
        	return;
        }

    	// ★ポイント6★ パートナー限定Content Providerアプリに開示してよい情報に限りリクエストに含めてよい
        int count = getContentResolver().delete(Address.CONTENT_URI, null, null);

        // ★ポイント7★ パートナー限定Content Providerアプリからの結果であっても、結果データの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
        logLine(String.format("  %s records deleted", count));
    }

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
