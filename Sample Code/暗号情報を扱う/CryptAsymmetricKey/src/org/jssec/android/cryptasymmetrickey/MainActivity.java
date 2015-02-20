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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final static String filename = "encrypted.dat";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private byte[] readAssetsData(final String name) {
		InputStream in = null;
		byte[] buf = null;
		try {
			in = getAssets().open(name);
			buf = new byte[in.available()];
			in.read(buf);
		} catch (IOException e) {
			buf = null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		
		return buf;
	}

	public void onSave(View view) {
		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
		String memo = textViewMemo.getText().toString();
		
		RsaCryptoAsymmetricKey cipher = new RsaCryptoAsymmetricKey();
		
		byte[] bufKey = readAssetsData("public.der");
		
    	byte[] encrypted = cipher.encrypt(memo.getBytes(), bufKey);
    	save(filename, encrypted);
    }

    public void onLoad(View view) {
    	RsaCryptoAsymmetricKey cipher = new RsaCryptoAsymmetricKey();
    	
		byte[] bufKey = readAssetsData("private.pk8");

		byte[] encrypted = load(filename);
		byte[] plain = cipher.decrypt(encrypted, bufKey);
    	String memo = new String(plain);
    	
		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
		textViewMemo.setText(memo);
    }

	private byte[] load(final String name) {
		byte[] ret = null;
		
		FileInputStream fileInput = null;
		
		try {
			fileInput = openFileInput(name);

			long lengthEnc = fileInput.getChannel().size();
			
			byte[] encrypted = new byte [(int) lengthEnc];
			
			fileInput.read(encrypted);
			
			ret = encrypted;
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

	private boolean save(final String name, final byte[] encrypted) {
		boolean ret = false;
		FileOutputStream fileOutput = null;
		
		try {
			fileOutput = openFileOutput(name, Context.MODE_PRIVATE);
			fileOutput.write(encrypted);
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
