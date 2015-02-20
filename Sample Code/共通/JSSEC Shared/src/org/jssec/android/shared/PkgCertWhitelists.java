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

package org.jssec.android.shared;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class PkgCertWhitelists {
	private Map<String, String> mWhitelists = new HashMap<String, String>();
	
	public boolean add(String pkgname, String sha256) {
		if (pkgname == null) return false;
		if (sha256 == null) return false;
		
		sha256 = sha256.replaceAll(" ", "");
		if (sha256.length() != 64) return false;	// SHA-256は32バイト
		sha256 = sha256.toUpperCase();
		if (sha256.replaceAll("[0-9A-F]+", "").length() != 0) return false;	// 0-9A-F 以外の文字がある
		
		mWhitelists.put(pkgname, sha256);
		return true;
	}
	
	public boolean test(Context ctx, String pkgname) {
		// pkgnameに対応する正解のハッシュ値を取得する
		String correctHash = mWhitelists.get(pkgname);
		
		// pkgnameの実際のハッシュ値と正解のハッシュ値を比較する
		return PkgCert.test(ctx, pkgname, correctHash);
	}
}
