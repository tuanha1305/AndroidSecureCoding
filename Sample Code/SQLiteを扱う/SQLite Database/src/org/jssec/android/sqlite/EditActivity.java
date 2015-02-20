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

package org.jssec.android.sqlite;

import org.jssec.android.sqlite.R;

import android.content.Intent;
import android.widget.TextView;

public class EditActivity extends SubActivity {
    private int     mRequestMode;
    
    protected void init(Intent intent) {
        mRequestMode = intent.getIntExtra(CommonData.EXTRA_REQUEST_MODE, CommonData.REQUEST_NEW);

        if (mRequestMode == CommonData.REQUEST_NEW) {
            this.setTitle(R.string.ACTIVITY_TITLE_NEW);
        } else {
            this.setTitle(R.string.ACTIVITY_TITLE_EDIT);            
        }
        //画面の表示
        setContentView(R.layout.data_edit); 	

        //編集モードの時はNo欄は入力不可
        if (mRequestMode == CommonData.REQUEST_EDIT) {
            ((TextView)findViewById(R.id.Field_IdNo)).setFocusable(false);
            ((TextView)findViewById(R.id.Field_IdNo)).setClickable(false);
        }
    }
    protected boolean refrectEditText() {
    	return true;
    }
}
