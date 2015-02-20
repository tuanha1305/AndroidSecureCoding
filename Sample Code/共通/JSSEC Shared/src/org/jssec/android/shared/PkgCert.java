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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

public class PkgCert {

	public static boolean test(Context ctx, String pkgname, String correctHash) {
		if (correctHash == null) return false;
		correctHash = correctHash.replaceAll(" ", "");
		return correctHash.equals(hash(ctx, pkgname));
	}

    public static String hash(Context ctx, String pkgname) {
    	if (pkgname == null) return null;
    	try {
    		PackageManager pm = ctx.getPackageManager();
			PackageInfo pkginfo = pm.getPackageInfo(pkgname, PackageManager.GET_SIGNATURES);
			if (pkginfo.signatures.length != 1) return null;	// 複数署名は扱わない
			Signature sig = pkginfo.signatures[0];
			byte[] cert = sig.toByteArray();
			byte[] sha256 = computeSha256(cert);
			return byte2hex(sha256);
		} catch (NameNotFoundException e) {
			return null;
		}
    }

    private static byte[] computeSha256(byte[] data) {
    	try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
    }

    private static String byte2hex(byte[] data) {
    	if (data == null) return null;
    	final StringBuilder hexadecimal = new StringBuilder();
    	for (final byte b : data) {
    		hexadecimal.append(String.format("%02X", b));
    	}
    	return hexadecimal.toString();
    }
}
