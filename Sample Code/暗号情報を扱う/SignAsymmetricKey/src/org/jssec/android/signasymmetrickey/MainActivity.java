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

	private final static String filename = "plainWithSign.dat";

	class CryptData {
		CryptData(final byte[] sign, final byte[] data) {
			mSign = sign;
			mData = data;
		}
		private byte[] mSign = null;
		private byte[] mData = null;
	}

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
		
		RsaSignAsymmetricKey cipher = new RsaSignAsymmetricKey();
		
		byte[] bufKey = readAssetsData("private.pk8");
		
    	byte[] sign = cipher.sign(memo.getBytes(), bufKey);
    	if (sign != null) {
    		CryptData data = new CryptData(sign, memo.getBytes());
    		save(filename, data);
    	}
    }

    public void onLoad(View view) {
    	RsaSignAsymmetricKey cipher = new RsaSignAsymmetricKey();
    	
		byte[] bufKey = readAssetsData("public.der");
		
		CryptData data = load(filename);

		boolean verified = cipher.verify(data.mSign, data.mData, bufKey);
		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
    	if (verified == false) {
    		textViewMemo.setText(null);
			textViewMemo.setHint("Failed to verify your memo! Possibly the memo corrupted or the key wrong!");
    		
    	} else {
	    	String memo = new String(data.mData);
	    	
			textViewMemo.setText(memo);//　こっちだとエミュレータでIMMが動き出してしまう。
//	    	Editable editable = (Editable)textViewMemo.getText();
//	    	editable.clear();
//	    	editable.append(memo);
    	}
    }
    
    private CryptData load(final String name) {
		FileInputStream fileInput = null;
		CryptData ret = null;
		
		try {
			int length = 0;
			byte[] sign = null;
			byte[] plain = null;
						
			fileInput = openFileInput(name);
			
			length |= fileInput.read() & 0xff;
			length |= (fileInput.read() & 0xff) << 8;
			length |= (fileInput.read() & 0xff) << 16;
			length |= (fileInput.read() & 0xff) << 24;
			sign = new byte [length];
			fileInput.read(sign);

			long lengthPlain = fileInput.getChannel().size() - sign.length - 4;
			
			plain = new byte [(int) lengthPlain];
			
			fileInput.read(plain);
			
			ret = new CryptData(sign, plain);
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
			int length = data.mSign.length;
			fileOutput.write(length & 0xff);
			fileOutput.write((length >> 8) & 0xff);
			fileOutput.write((length >> 16) & 0xff);
			fileOutput.write((length >> 24) & 0xff);
			fileOutput.write(data.mSign);
			
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
