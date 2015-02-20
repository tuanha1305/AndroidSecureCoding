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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class RsaCryptoAsymmetricKey {

	// ★ポイント1★ 明示的に暗号モードとパディングを設定する
	// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
	// Cipher クラスの getInstance に渡すパラメータ （/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール])
	// サンプルでは、暗号アルゴリズム=RSA、ブロック暗号モード=NONE、パディングルール=OAEPPADDING
	private static final String TRANSFORMATION = "RSA/NONE/OAEPPADDING";

	// 暗号アルゴリズム
	private static final String KEY_ALGORITHM = "RSA";

	// ★ポイント3★ 十分安全な長さを持つ鍵を利用する
	// 鍵長チェック
	private static final int MIN_KEY_LENGTH = 2000;

	RsaCryptoAsymmetricKey() {
	}

	public final byte[] encrypt(final byte[] plain, final byte[] keyData) {
		byte[] encrypted = null;

		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			PublicKey publicKey = generatePubKey(keyData);
			if (publicKey != null) {
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
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

	public final byte[] decrypt(final byte[] encrypted, final byte[] keyData) {
		// 本来、復号処理はサーバー側で実装すべきものであるが、
		// 本サンプルでは動作確認用に、アプリ内でも復号処理を実装した。
		// 実際にサンプルコードを利用する場合は、アプリ内に秘密鍵を保持しないようにすること。
		
		byte[] plain = null;

		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);

			PrivateKey privateKey = generatePriKey(keyData);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			plain = cipher.doFinal(encrypted);
		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchPaddingException e) {
		} catch (InvalidKeyException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) {
		} finally {
		}

		return plain;
	}

	private static final PublicKey generatePubKey(final byte[] keyData) {
		PublicKey publicKey = null;
		KeyFactory keyFactory = null;

		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyData));
		} catch (IllegalArgumentException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeySpecException e) {
		} finally {
		}

		// ★ポイント3★ 十分安全な長さを持つ鍵を利用する
		// 鍵長のチェック
		if (publicKey instanceof RSAPublicKey) {
			int len = ((RSAPublicKey) publicKey).getModulus().bitLength();
			if (len < MIN_KEY_LENGTH) {
				publicKey = null;
			}
		}

		return publicKey;
	}

	private static final PrivateKey generatePriKey(final byte[] keyData) {
		PrivateKey privateKey = null;
		KeyFactory keyFactory = null;

		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyData));
		} catch (IllegalArgumentException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeySpecException e) {
		} finally {
		}

		return privateKey;
	}
}
