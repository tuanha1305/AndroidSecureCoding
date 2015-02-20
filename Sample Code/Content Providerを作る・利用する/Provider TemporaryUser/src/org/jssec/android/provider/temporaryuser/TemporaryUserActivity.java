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

package org.jssec.android.provider.temporaryuser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TemporaryUserActivity extends Activity {

    // Provider Activityに関する情報
    private static final String TARGET_PACKAGE =  "org.jssec.android.provider.temporaryprovider";
    private static final String TARGET_ACTIVITY = "org.jssec.android.provider.temporaryprovider.TemporaryPassiveGrantActivity";

    // 利用先のContent Provider情報
	private static final String AUTHORITY = "org.jssec.android.provider.temporaryprovider";
	private interface Address {
		public static final String PATH = "addresses";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
	}

    private static final int REQUEST_CODE = 1;

	public void onQueryClick(View view) {

    	logLine("[Query]");

        Cursor cursor = null;
    	try {
	    	if (!providerExists(Address.CONTENT_URI)) {
	    		logLine("  Content Providerが不在");
	    		return;
	    	}

	    	// ★ポイント10★ センシティブな情報をリクエストに含めてはならない
	    	// リクエスト先のアプリがマルウェアである可能性がある。
	    	// マルウェアに取得されても問題のない情報であればリクエストに含めてもよい。
	        cursor = getContentResolver().query(Address.CONTENT_URI, null, null, null, null);

	        // ★ポイント11★ 結果データの安全性を確認する
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
    	} catch (SecurityException ex) {
    		logLine("  例外:" + ex.getMessage());
    	}
        finally {
            if (cursor != null) cursor.close();
        }
	}

	// このアプリが一時的なアクセス許可を要求し、Content Provider側アプリが受動的にアクセス許可を与えるケース
	public void onGrantRequestClick(View view) {
		Intent intent = new Intent();
		intent.setClassName(TARGET_PACKAGE, TARGET_ACTIVITY);
		try {
		    startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			logLine("Grantの要求に失敗しました。\nTemporaryProviderがインストールされているか確認してください。");
		}
	}

    private boolean providerExists(Uri uri) {
    	ProviderInfo pi = getPackageManager().resolveContentProvider(uri.getAuthority(), 0);
    	return (pi != null);
    }

	private TextView mLogView;

    // Content Provider側アプリが能動的にこのアプリにアクセス許可を与えるケース
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