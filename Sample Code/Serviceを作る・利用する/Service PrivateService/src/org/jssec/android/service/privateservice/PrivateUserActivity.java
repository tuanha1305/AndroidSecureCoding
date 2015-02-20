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
package org.jssec.android.service.privateservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PrivateUserActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privateservice_activity);
    }
    
    // サービス開始
    
    public void onStartServiceClick(View v) {
          // ★ポイント4★ 同一アプリ内Serviceはクラス指定の明示的Intentで呼び出す                
        Intent intent = new Intent(this, PrivateStartService.class);
          
          // ★ポイント5★ 利用先アプリは同一アプリであるから、センシティブな情報を送信してもよい
        intent.putExtra("PARAM", "センシティブな情報");

        startService(intent);
    }
    
    // サービス停止ボタン
    public void onStopServiceClick(View v) {
    	doStopService();
    }
        
    @Override
    public void onStop(){
        super.onStop();
        // サービスが終了していない場合は終了する
        doStopService();
    }
    // サービスを停止する
    private void doStopService() {
        // ★ポイント4★ 同一アプリ内Serviceはクラス指定の明示的Intentで呼び出す                
        Intent intent = new Intent(this, PrivateStartService.class);
        stopService(intent);    	
    }

    // IntentService 開始ボタン

    public void onIntentServiceClick(View v) {
          // ★ポイント4★ 同一アプリ内Serviceはクラス指定の明示的Intentで呼び出す                
        Intent intent = new Intent(this, PrivateIntentService.class);
          
          // ★ポイント5★ 利用先アプリは同一アプリであるから、センシティブな情報を送信してもよい
        intent.putExtra("PARAM", "センシティブな情報");

        startService(intent);
    }
        
}