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

package org.jssec.android.signasymmetrickey;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class RsaSignAsymmetricKey {

	// ★ポイント1★ 明示的に暗号モードとパディングを設定する
	// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
	// Cipher クラスの getInstance に渡すパラメータ （/[暗号アルゴリズム]/[ブロック暗号モード]/[パディングルール])
	// サンプルでは、暗号アルゴリズム=RSA、ブロック暗号モード=NONE、パディングルール=OAEPPADDING
	private static final String TRANSFORMATION = "SHA256withRSA";
	
	// 暗号アルゴリズム
	private static final String KEY_ALGORITHM = "RSA";
	
	// ★ポイント3★ 十分安全な長さを持つ鍵を利用する
	// 鍵長チェック
	private static final int MIN_KEY_LENGTH = 2000;

	RsaSignAsymmetricKey() {
	}
	
	public final byte[] sign(final byte[] plain, final byte[] keyData) {
		// 本来、署名処理はサーバー側で実装すべきものであるが、
		// 本サンプルでは動作確認用に、アプリ内でも署名処理を実装した。
		// 実際にサンプルコードを利用する場合は、アプリ内に秘密鍵を保持しないようにすること。

		byte[] sign = null;

		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Signature signature = Signature.getInstance(TRANSFORMATION);

			PrivateKey privateKey = generatePriKey(keyData);
			signature.initSign(privateKey);
			signature.update(plain);

			sign = signature.sign();
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeyException e) {
		} catch (SignatureException e) {
		} finally {
		}

		return sign;
	}

	public final boolean verify(final byte[] sign, final byte[] plain, final byte[] keyData) {

		boolean ret = false;
		
		try {
			// ★ポイント1★ 明示的に暗号モードとパディングを設定する
			// ★ポイント2★ 脆弱でない(基準を満たす)暗号技術（アルゴリズム・モード・パディング等）を使用する
			Signature signature = Signature.getInstance(TRANSFORMATION);

			PublicKey publicKey = generatePubKey(keyData);
			signature.initVerify(publicKey);				
			signature.update(plain);
			
			ret = signature.verify(sign);
						
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeyException e) {
		} catch (SignatureException e) {
		} finally {
		}

		return ret;
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
