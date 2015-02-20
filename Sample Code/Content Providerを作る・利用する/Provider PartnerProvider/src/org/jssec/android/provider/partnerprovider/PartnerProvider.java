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

package org.jssec.android.provider.partnerprovider;

import java.util.List;

import org.jssec.android.shared.PkgCertWhitelists;
import org.jssec.android.shared.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;

public class PartnerProvider extends ContentProvider {

    public static final String AUTHORITY = "org.jssec.android.provider.partnerprovider";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.jssec.contenttype";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.jssec.contenttype";

    // Content Providerが提供するインターフェースを公開
    public interface Download {
        public static final String PATH = "downloads";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
    }
    public interface Address {
        public static final String PATH = "addresses";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
    }

    // UriMatcher
    private static final int DOWNLOADS_CODE = 1;
    private static final int DOWNLOADS_ID_CODE = 2;
    private static final int ADDRESSES_CODE = 3;
    private static final int ADDRESSES_ID_CODE = 4;
    private static UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, Download.PATH, DOWNLOADS_CODE);
        sUriMatcher.addURI(AUTHORITY, Download.PATH + "/#", DOWNLOADS_ID_CODE);
        sUriMatcher.addURI(AUTHORITY, Address.PATH, ADDRESSES_CODE);
        sUriMatcher.addURI(AUTHORITY, Address.PATH + "/#", ADDRESSES_ID_CODE);
    }

    // DBを使用せずに固定値を返す例にしているため、queryメソッドで返すCursorを事前に定義
    private static MatrixCursor sAddressCursor = new MatrixCursor(new String[] { "_id", "pref" });
    static {
        sAddressCursor.addRow(new String[] { "1", "北海道" });
        sAddressCursor.addRow(new String[] { "2", "青森" });
        sAddressCursor.addRow(new String[] { "3", "岩手" });
    }
    private static MatrixCursor sDownloadCursor = new MatrixCursor(new String[] { "_id", "path" });
    static {
        sDownloadCursor.addRow(new String[] { "1", "/sdcard/downloads/sample.jpg" });
        sDownloadCursor.addRow(new String[] { "2", "/sdcard/downloads/sample.txt" });
    }

	// ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
	private static PkgCertWhitelists sWhitelists = null;
	private static void buildWhitelists(Context context) {
		boolean isdebug = Utils.isDebuggable(context);
		sWhitelists = new PkgCertWhitelists();

		// パートナーアプリ org.jssec.android.provider.partneruser の証明書ハッシュ値を登録
		sWhitelists.add("org.jssec.android.provider.partneruser", isdebug ?
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
	// 利用元アプリのパッケージ名を取得
	private String getCallingPackage(Context context) {
		String pkgname = null;
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = am.getRunningAppProcesses();
		int callingPid = Binder.getCallingPid();
		if (procList != null) {
			for (RunningAppProcessInfo proc : procList) {
				if (proc.pid == callingPid) {
					pkgname = proc.pkgList[proc.pkgList.length - 1];
					break;
				}
			}
		}
		return pkgname;
	}

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {

		switch (sUriMatcher.match(uri)) {
		case DOWNLOADS_CODE:
		case ADDRESSES_CODE:
			return CONTENT_TYPE;

		case DOWNLOADS_ID_CODE:
		case ADDRESSES_ID_CODE:
			return CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Invalid URI：" + uri);
		}
	}

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

    	// ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(getContext(), getCallingPackage(getContext()))) {
        	throw new SecurityException("利用元アプリはパートナーアプリではない。");
        }

    	// ★ポイント3★ パートナーアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
		// 「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント4★ パートナーアプリに開示してよい情報に限り返送してよい
    	// queryの結果がパートナーアプリに開示してよい情報かどうかはアプリ次第。
        switch (sUriMatcher.match(uri)) {
        case DOWNLOADS_CODE:
        case DOWNLOADS_ID_CODE:
            return sDownloadCursor;

        case ADDRESSES_CODE:
        case ADDRESSES_ID_CODE:
            return sAddressCursor;

        default:
            throw new IllegalArgumentException("Invalid URI：" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

    	// ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(getContext(), getCallingPackage(getContext()))) {
        	throw new SecurityException("利用元アプリはパートナーアプリではない。");
        }

    	// ★ポイント3★ パートナーアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
		// 「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント4★ パートナーアプリに開示してよい情報に限り返送してよい
    	// Insert結果、発番されるIDがパートナーアプリに開示してよい情報かどうかはアプリ次第。
        switch (sUriMatcher.match(uri)) {
        case DOWNLOADS_CODE:
            return ContentUris.withAppendedId(Download.CONTENT_URI, 3);

        case ADDRESSES_CODE:
            return ContentUris.withAppendedId(Address.CONTENT_URI, 4);

        default:
            throw new IllegalArgumentException("Invalid URI：" + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {

    	// ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(getContext(), getCallingPackage(getContext()))) {
        	throw new SecurityException("利用元アプリはパートナーアプリではない。");
        }

    	// ★ポイント3★ パートナーアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
		// 「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント4★ パートナーアプリに開示してよい情報に限り返送してよい
    	// Updateされたレコード数がセンシティブな意味を持つかどうかはアプリ次第。
        switch (sUriMatcher.match(uri)) {
        case DOWNLOADS_CODE:
        	return 5;	// updateされたレコード数を返す

        case DOWNLOADS_ID_CODE:
            return 1;

        case ADDRESSES_CODE:
        	return 15;

        case ADDRESSES_ID_CODE:
            return 1;

        default:
            throw new IllegalArgumentException("Invalid URI：" + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

    	// ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
        if (!checkPartner(getContext(), getCallingPackage(getContext()))) {
        	throw new SecurityException("利用元アプリはパートナーアプリではない。");
        }

    	// ★ポイント3★ パートナーアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
		// 「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント4★ パートナーアプリに開示してよい情報に限り返送してよい
    	// Deleteされたレコード数がセンシティブな意味を持つかどうかはアプリ次第。
        switch (sUriMatcher.match(uri)) {
        case DOWNLOADS_CODE:
        	return 10;	// deleteされたレコード数を返す

        case DOWNLOADS_ID_CODE:
            return 1;

        case ADDRESSES_CODE:
        	return 20;

        case ADDRESSES_ID_CODE:
            return 1;

        default:
            throw new IllegalArgumentException("Invalid URI：" + uri);
        }
    }
}
