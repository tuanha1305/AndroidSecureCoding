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

package org.jssec.android.signsymmetricpresharedkey;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	final static byte[] keyData = Utils.decodeHex("d4ba999e6af80c096c32d8f732e902e7");

	private final static String filename = "plainWithHmac.dat";

	class CryptData {
		CryptData(final byte[] hmac, final byte[] data) {
			mHmac = hmac;
			mData = data;
		}
		private byte[] mHmac = null;
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
    
    public void onSave(View view) {
		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
		String memo = textViewMemo.getText().toString();
		
		HmacPreSharedKey cipher = new HmacPreSharedKey();
    	
    	byte[] hmac = cipher.sign(memo.getBytes(), keyData);
    	if (hmac != null) {
    		CryptData data = new CryptData(hmac, memo.getBytes());
    		save(filename, data);
    	}
    }

    public void onLoad(View view) {

    	CryptData data = load(filename);
    	
    	HmacPreSharedKey cipher = new HmacPreSharedKey();
    	
    	boolean verified = cipher.verify(data.mHmac, data.mData, keyData);
		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
    	if (verified == false) {
    		textViewMemo.setText(null);
			textViewMemo.setHint("Failed to verify your memo! Possibly the memo corrupted or the key wrong!");
    		
    	} else {
	    	String memo = new String(data.mData);
	    	
			textViewMemo.setText(memo);
    	}
    }
    
    private CryptData load(final String name) {
		FileInputStream fileInput = null;
		CryptData ret = null;
		
		try {
			int length = 0;
	    	byte[] hmac = null;
			byte[] plain = null;
			
			fileInput = openFileInput(name);

			length = fileInput.read();
			hmac = new byte[length];
			fileInput.read(hmac);

			long lengthEnc = fileInput.getChannel().size() - hmac.length - 1;
			
			plain = new byte [(int) lengthEnc];
			
			fileInput.read(plain);
			
			ret = new CryptData(hmac, plain);
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
			fileOutput.write(data.mHmac.length);
			fileOutput.write(data.mHmac);
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
