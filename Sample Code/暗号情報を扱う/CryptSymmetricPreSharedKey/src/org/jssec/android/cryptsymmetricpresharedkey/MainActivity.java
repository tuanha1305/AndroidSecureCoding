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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	final static byte[] keyData = Utils.decodeHex("d4ba999e6af80c096c32d8f732e902e7");

	class CryptData {
		CryptData(final byte[] iv, final byte[] data) {
			mIV = iv;
			mData = data;
		}
		private byte[] mIV = null;
		private byte[] mData = null;
	}

	private final static String filename = "encrypted.dat";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void onSave(View view) {
		TextView textViewMemo = (TextView) findViewById(R.id.editTextMemo);
		String memo = textViewMemo.getText().toString();

		AesCryptoPreSharedKey cipher = new AesCryptoPreSharedKey();

		byte[] encrypted = cipher.encrypt(keyData, memo.getBytes());
		if (encrypted != null) {
			CryptData data = new CryptData(cipher.getIV(), encrypted);
			save(filename, data);
		}
	}

	public void onLoad(View view) {
		CryptData data = load(filename);

		AesCryptoPreSharedKey cipher = new AesCryptoPreSharedKey(data.mIV);
		byte[] plain = cipher.decrypt(keyData, data.mData);
		if (plain != null) {
			String memo = new String(plain);

			TextView textViewMemo = (TextView) findViewById(R.id.editTextMemo);
			textViewMemo.setText(memo);
		}
	}

	private CryptData load(final String name) {
		FileInputStream fileInput = null;
		CryptData ret = null;

		try {
			fileInput = openFileInput(name);

			int length = fileInput.read();
			byte[] iv = new byte[length];
			fileInput.read(iv);

			long lengthEnc = fileInput.getChannel().size() - iv.length - 1;

			byte[] encrypted = new byte[(int) lengthEnc];

			fileInput.read(encrypted);

			ret = new CryptData(iv, encrypted);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException e) {
				}
			}
		}

		return ret;
	}

	private boolean save(final String name, final CryptData data) {
		boolean ret = false;
		FileOutputStream fileOutput = null;

		try {
			fileOutput = openFileOutput(name, Context.MODE_PRIVATE);
			fileOutput.write(data.mIV.length);
			fileOutput.write(data.mIV);
			fileOutput.write(data.mData);
			ret = true;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (fileOutput != null) {
				try {
					fileOutput.close();
				} catch (IOException e) {
				}
			}
		}

		return ret;
	}
}
