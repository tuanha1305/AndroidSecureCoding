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
package org.jssec.android.cryptsymmetricpresharedkey;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AesCryptoPreSharedKey {

	// ★ポイント1★ 明示的に暗号モードとパディングを設定する
	// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
	// Cipher クラスの getInstance に渡すパラメータ （/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール])
	// サンプルでは、暗号アルゴリズム=AES、ブロック暗号モード=CBC、パディングルール=PKCS7Padding
	private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";

	// 暗号アルゴリズム
	private static final String KEY_ALGORITHM = "AES";

	// IVのバイト長
	public static final int IV_LENGTH_BYTES = 16;

	// ★ポイント3★ 十分安全な長さを持つ鍵を利用する
	// 鍵長チェック
	private static final int MIN_KEY_LENGTH_BYTES = 16;

	private byte[] mIV = null;

	public byte[] getIV() {
		return mIV;
	}

	AesCryptoPreSharedKey(final byte[] iv) {
		mIV = iv;
	}

	AesCryptoPreSharedKey() {
	}

	public final byte[] encrypt(final byte[] keyData, final byte[] plain) {
		byte[] encrypted = null;

		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			SecretKey secretKey = generateKey(keyData);
			if (secretKey != null) {
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
				mIV = cipher.getIV();

				encrypted = cipher.doFinal(plain);
			}
		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchPaddingException e) {
		} catch (InvalidKeyException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) {
		} finally {
		}

		return encrypted;
	}

	public final byte[] decrypt(final byte[] keyData, final byte[] encrypted) {
		byte[] plain = null;

		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			SecretKey secretKey = generateKey(keyData);
			if (secretKey != null) {
				IvParameterSpec ivParameterSpec = new IvParameterSpec(mIV);
				cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

				plain = cipher.doFinal(encrypted);
			}
		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchPaddingException e) {
		} catch (InvalidKeyException e) {
		} catch (InvalidAlgorithmParameterException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) {
		} finally {
		}

		return plain;
	}

	private static final SecretKey generateKey(final byte[] keyData) {
		SecretKey secretKey = null;

		try {
			// ★ポイント3★ 十分安全な長さを持つ鍵を利用する
			if (keyData.length >= MIN_KEY_LENGTH_BYTES) {
				// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
				secretKey = new SecretKeySpec(keyData, KEY_ALGORITHM);
			}
		} catch (IllegalArgumentException e) {
		} finally {
		}

		return secretKey;
	}
}
