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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public abstract class SubActivity extends Activity {
    private String  mItemIdNo;
    private String  mItemName;
    private String  mItemInfo;
    
    protected abstract void init(Intent intent);
    protected abstract boolean refrectEditText();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Intentの中からデータを取り出す
        Intent intent = getIntent();
        mItemIdNo    = intent.getStringExtra(CommonData.EXTRA_ITEM_IDNO);
        mItemName    = intent.getStringExtra(CommonData.EXTRA_ITEM_NAME);
        mItemInfo    = intent.getStringExtra(CommonData.EXTRA_ITEM_INFO);

        init(intent);
        
        //表示項目の編集
        ((TextView)findViewById(R.id.Field_IdNo)).setText(mItemIdNo);
        ((TextView)findViewById(R.id.Field_Name)).setText(mItemName);
        ((TextView)findViewById(R.id.Field_Info)).setText(mItemInfo);
                        
        //OKボタン
        Button btnOK = (Button)findViewById(R.id.Button_OK);
        btnOK.setOnClickListener(new OnClickListener(){
        	
        	 String getText(int id) {
        		 String str = null;
        		 
        		 Editable editable = ((TextView)findViewById(id)).getEditableText();

        		 if (editable != null) {
        			 str = editable.toString();
        		 }
        		 
        		 return str;
        	 }

            @Override
            public void onClick(View v) {
            	  if (refrectEditText()) {
	                  //データを取得して更新処理を呼び出す
	                mItemIdNo = getText(R.id.Field_IdNo);
	                mItemName = getText(R.id.Field_Name);
	                mItemInfo = getText(R.id.Field_Info);
            	  }
            	  
                Intent intent = new Intent();
                intent.putExtra(CommonData.EXTRA_ITEM_IDNO, mItemIdNo);
                intent.putExtra(CommonData.EXTRA_ITEM_NAME, mItemName);
                intent.putExtra(CommonData.EXTRA_ITEM_INFO, mItemInfo);
                
                Log.i("debug", mItemIdNo + mItemName + mItemInfo);
                
                setResult(Activity.RESULT_OK, intent);
                finish();
            }});
        
        //Cancelボタン
        Button btnCancel = (Button)findViewById(R.id.Button_CANCEL);
        btnCancel.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // キャンセルの時は何もせずに返る
                setResult(Activity.RESULT_CANCELED, null);
                finish();
            }});
    }
}
