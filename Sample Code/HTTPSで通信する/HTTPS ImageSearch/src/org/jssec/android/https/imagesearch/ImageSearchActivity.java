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

package org.jssec.android.https.imagesearch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageSearchActivity extends Activity {

	private EditText mQueryBox;
	private TextView mMsgBox;
	private ImageView mImgBox;
	private AsyncTask<String, Void, Object> mAsyncTask ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mQueryBox = (EditText)findViewById(R.id.querybox);
        mMsgBox = (TextView)findViewById(R.id.msgbox);
		mImgBox = (ImageView)findViewById(R.id.imageview);
    }

    @Override
	protected void onPause() {
    	// このあとActivityが破棄される可能性があるので非同期処理をキャンセルしておく
    	if (mAsyncTask != null) mAsyncTask.cancel(true);
		super.onPause();
	}

	public void onHttpSearchClick(View view) {
    	String query = mQueryBox.getText().toString();
		mMsgBox.setText("HTTP:" + query);
		mImgBox.setImageBitmap(null);
    	
    	// 直前の非同期処理が終わってないこともあるのでキャンセルしておく
    	if (mAsyncTask != null) mAsyncTask.cancel(true);
    	
    	// UIスレッドで通信してはならないので、AsyncTaskによりワーカースレッドで通信する
    	mAsyncTask = new HttpImageSearch() {
			@Override
			protected void onPostExecute(Object result) {
				// UIスレッドで通信結果を処理する
				if (result instanceof Exception) {
					Exception e = (Exception)result;
					mMsgBox.append("\n例外発生\n" + e.toString());
				} else {
					// サンプルにつき画像表示の際の例外処理は割愛
					byte[] data = (byte[])result;
					Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					mImgBox.setImageBitmap(bmp);
				}
			}
    	}.execute(query);	// 検索文字列を渡して非同期処理を開始
    }

	public void onHttpsSearchClick(View view) {
    	String query = mQueryBox.getText().toString();
		mMsgBox.setText("HTTPS:" + query);
		mImgBox.setImageBitmap(null);
    	
    	// 直前の非同期処理が終わってないこともあるのでキャンセルしておく
    	if (mAsyncTask != null) mAsyncTask.cancel(true);
    	
    	// UIスレッドで通信してはならないので、AsyncTaskによりワーカースレッドで通信する
    	mAsyncTask = new HttpsImageSearch() {
			@Override
			protected void onPostExecute(Object result) {
				// UIスレッドで通信結果を処理する
				if (result instanceof Exception) {
					Exception e = (Exception)result;
					mMsgBox.append("\n例外発生\n" + e.toString());
				} else {
					byte[] data = (byte[])result;
					Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					mImgBox.setImageBitmap(bmp);
				}
			}
    	}.execute(query);	// 検索文字列を渡して非同期処理を開始
    }
}
