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

package org.jssec.android.provider.temporaryprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class TemporaryProvider extends ContentProvider {
    public static final String AUTHORITIY = "org.jssec.android.provider.temporaryprovider";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.jssec.contenttype";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.jssec.contenttype";

    // Content Providerが提供するインターフェースを公開
    public interface Download {
        public static final String PATH = "downloads";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIY + "/" + PATH);
    }
    public interface Address {
        public static final String PATH = "addresses";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIY + "/" + PATH);
    }

    // UriMatcher
    private static final int DOWNLOADS_CODE = 1;
    private static final int DOWNLOADS_ID_CODE = 2;
    private static final int ADDRESSES_CODE = 3;
    private static final int ADDRESSES_ID_CODE = 4;
    private static UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITIY, Download.PATH, DOWNLOADS_CODE);
        sUriMatcher.addURI(AUTHORITIY, Download.PATH + "/#", DOWNLOADS_ID_CODE);
        sUriMatcher.addURI(AUTHORITIY, Address.PATH, ADDRESSES_CODE);
        sUriMatcher.addURI(AUTHORITIY, Address.PATH + "/#", ADDRESSES_ID_CODE);
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

    	// ★ポイント4★ 一時的に許可したアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
    	// その他のパラメータの確認はサンプルにつき省略。「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント5★ 一時的に許可したアプリに開示してよい情報に限り返送してよい
    	// queryの結果がセンシティブな意味を持つかどうかはアプリ次第。
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

    	// ★ポイント4★ 一時的に許可したアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
    	// その他のパラメータの確認はサンプルにつき省略。「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント5★ 一時的に許可したアプリに開示してよい情報に限り返送してよい
    	// Insert結果、発番されるIDがセンシティブな意味を持つかどうかはアプリ次第。
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

    	// ★ポイント4★ 一時的に許可したアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
    	// その他のパラメータの確認はサンプルにつき省略。「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント5★ 一時的に許可したアプリに開示してよい情報に限り返送してよい
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

    	// ★ポイント4★ 一時的に許可したアプリからのリクエストであっても、パラメータの安全性を確認する
    	// ここではuriが想定の範囲内であることを、UriMatcher#match()とswitch caseで確認している。
    	// その他のパラメータの確認はサンプルにつき省略。「3.2 入力データの安全性を確認する」を参照。
    	// ★ポイント5★ 一時的に許可したアプリに開示してよい情報に限り返送してよい
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
