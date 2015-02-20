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
package org.jssec.android.service.publicserviceuser;

import org.jssec.android.service.publicserviceuser.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PublicUserActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.publicservice_activity);
    }
    
    // サービス開始
    public void onStartServiceClick(View v) {              
        Intent intent = new Intent("org.jssec.android.service.publicservice.action.startservice");
          
    	// ★ポイント4★ センシティブな情報を送信してはならない
        intent.putExtra("PARAM", "センシティブではない情報");

        startService(intent);
        
     // ★ポイント5★ 結果を受け取る場合、結果データの安全性を確認する
     // 本サンプルは startService()を使ったService利用の例の為、結果情報は受け取らない
    }
    
    // サービス停止ボタン
    public void onStopServiceClick(View v) {
    	doStopService();
    }
        
    // IntentService 開始ボタン

    public void onIntentServiceClick(View v) {      
        Intent intent = new Intent("org.jssec.android.service.publicservice.action.intentservice");
          
    	// ★ポイント4★ センシティブな情報を送信してはならない
        intent.putExtra("PARAM", "センシティブではない情報");

        startService(intent);
    }
        
    @Override
    public void onStop(){
        super.onStop();
        // サービスが終了していない場合は終了する
        doStopService();
    }
    
    // サービスを停止する
    private void doStopService() {            
        Intent intent = new Intent("org.jssec.android.service.publicservice.action.startservice");
        stopService(intent);    	
    }
}