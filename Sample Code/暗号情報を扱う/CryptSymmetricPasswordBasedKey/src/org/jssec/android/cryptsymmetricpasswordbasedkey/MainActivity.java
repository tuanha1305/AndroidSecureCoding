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
package org.jssec.android.cryptsymmetricpasswordbasedkey;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	static final int LAUNCH_SAVELOAD_ACTIVITY = 0x00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void onSave(View view) {
    	launchIntent(true, getString(R.string.button_encrypt_save_title));
    }

    public void onLoad(View view) {
    	launchIntent(false, getString(R.string.button_load_decrypt_title));
    }
    
    private void launchIntent(final boolean saveMode, final String buttonTitle) {
    	Intent intent = new Intent(this, SaveLoadActivity.class);
    	intent.putExtra("saveMode", saveMode);
    	intent.putExtra("buttonTitle", buttonTitle);
    	
    	if (saveMode == true) {
    		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
    		String memo = textViewMemo.getText().toString();
    		intent.putExtra("memo", memo);
    	}
    	
    	startActivityForResult(intent, LAUNCH_SAVELOAD_ACTIVITY);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

    	if (requestCode == LAUNCH_SAVELOAD_ACTIVITY) {
    		if (resultCode == SaveLoadActivity.RESULT_SUCCEEDED && intent != null) {
        		TextView textViewMemo = (TextView)findViewById(R.id.editTextMemo);
        		String memo = intent.getStringExtra("memo");
    			textViewMemo.setText(memo);
    		}
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
