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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;

public class SigPerm {
	
	public static boolean test(Context ctx, String sigPermName, String correctHash) {
		if (correctHash == null) return false;
		correctHash = correctHash.replaceAll(" ", "");
		return correctHash.equals(hash(ctx, sigPermName));
	}

	public static String hash(Context ctx, String sigPermName) {
		if (sigPermName == null) return null;
		try {
			// sigPermNameを定義したアプリのパッケージ名を取得する
			PackageManager pm = ctx.getPackageManager();
			PermissionInfo pi;
			pi = pm.getPermissionInfo(sigPermName, PackageManager.GET_META_DATA);
			String pkgname = pi.packageName;
			
			// 非Signature Permissionの場合は失敗扱い
			if (pi.protectionLevel != PermissionInfo.PROTECTION_SIGNATURE) return null;
			
			// sigPermNameを定義したアプリの証明書のハッシュ値を返す
			return PkgCert.hash(ctx, pkgname);
			
		} catch (NameNotFoundException e) {
			return null;
		}
	}
}
