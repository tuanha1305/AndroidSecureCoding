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

package org.jssec.android.cryptasymmetrickey;

public class Utils {

	public static final byte[] decodeHex(String src) {
		// 長さが偶数でない場合は失敗
		if (src.length() % 2 != 0) {
			return null;
		}
		byte[] buf = new byte[src.length() / 2];
		for (int i = 0; i < src.length(); i += 2) {
			byte b1 = Byte.parseByte(src.substring(i, i + 1), 16);
			byte b2 = Byte.parseByte(src.substring(i + 1, i + 2), 16);
			buf[i / 2] = b2;
			buf[i / 2] |= b1 << 4;
		}
		return buf;
	}

	public static String encodeHex(byte[] data) {
		if (data == null)
			return null;
		final String digit = "0123456789ABCDEF";
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			int h = (b >> 4) & 15;
			int l = b & 15;
			sb.append(digit.charAt(h));
			sb.append(digit.charAt(l));
		}
		return sb.toString();
	}

}
