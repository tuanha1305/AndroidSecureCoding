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

package org.jssec.android.signsymmetricpasswordbasedkey;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class HmacPBEKey {

	// ★ポイント1★ 明示的に暗号モードとパディングを設定する
	// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
	// Mac クラスの getInstance に渡すパラメータ （認証モード)
	private static final String TRANSFORMATION = "PBEWITHHMACSHA1";

	// 鍵を生成するクラスのインスタンスを取得するための文字列
	private static final String KEY_GENERATOR_MODE = "PBEWITHHMACSHA1";

	// ★ポイント3★ パスワードから鍵を生成する場合は、Saltを使用する
	// Saltのバイト長
	public static final int SALT_LENGTH_BYTES = 20;

	// ★ポイント4★ パスワードから鍵を生成する場合は、適正なハッシュの繰り返し回数を指定する
	// PBE で鍵を生成する際の攪拌の繰り返し回数
	private static final int KEY_GEN_ITERATION_COUNT = 1024;

	// ★ポイント5★ 十分安全な長さを持つ鍵を利用する
	// 鍵のビット長
	private static final int KEY_LENGTH_BITS = 160;

	private byte[] mSalt = null;

	public byte[] getSalt() {
		return mSalt;
	}

	HmacPBEKey() {
		initSalt();
	}

	HmacPBEKey(final byte[] salt) {
		mSalt = salt;
	}

	private void initSalt() {
		mSalt = new byte[SALT_LENGTH_BYTES];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(mSalt);
	}

	public final byte[] sign(final byte[] plain, final char[] password) {
		return calculate(plain, password);
	}

	private final byte[] calculate(final byte[] plain, final char[] password) {
		byte[] hmac = null;

		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Mac mac = Mac.getInstance(TRANSFORMATION);

			// ★ポイント3★ パスワードから鍵を生成する場合は、Saltを使用する
			SecretKey secretKey = generateKey(password, mSalt);
			mac.init(secretKey);

			hmac = mac.doFinal(plain);
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeyException e) {
		} finally {
		}

		return hmac;
	}

	public final boolean verify(final byte[] hmac, final byte[] plain, final char[] password) {

		byte[] hmacForPlain = calculate(plain, password);

		if (Arrays.equals(hmac, hmacForPlain)) {
			return true;
		}
		return false;
	}

	private static final SecretKey generateKey(final char[] password, final byte[] salt) {
		SecretKey secretKey = null;
		PBEKeySpec keySpec = null;

		try {
			// ★ポイント2★ 脆弱でない(基準を満たす)アルゴリズム・モード・パディングを使用する
			// 鍵を生成するクラスのインスタンスを取得する
			// 例では、AES-CBC 128 ビット用の鍵を SHA1 を利用して生成する KeyFactory を使用。
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_MODE);

			// ★ポイント3★ パスワードから鍵を生成する場合は、Saltを使用する
			// ★ポイント4★ パスワードから鍵を生成する場合は、適正なハッシュの繰り返し回数を指定する
			// ★ポイント5★ 十分安全な長さを持つ鍵を利用する
			keySpec = new PBEKeySpec(password, salt, KEY_GEN_ITERATION_COUNT, KEY_LENGTH_BITS);
			// passwordのクリア
			Arrays.fill(password, '?');
			// 鍵を生成する
			secretKey = secretKeyFactory.generateSecret(keySpec);
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeySpecException e) {
		} finally {
			keySpec.clearPassword();
		}

		return secretKey;
	}

}
