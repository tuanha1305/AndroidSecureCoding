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
package org.jssec.android.service.privateservice.localbind;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class PrivateLocalBindService extends Service
implements IPrivateLocalBindService{   
    /**
     * Serviceに接続するためのクラス
     */
    public class LocalBinder extends Binder {
        PrivateLocalBindService getService() {
            return PrivateLocalBindService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
		// ★ポイント3★ 同一アプリからのIntentであっても、受信Intentの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
		String param = intent.getStringExtra("PARAM");
    	Toast.makeText(this, String.format("パラメータ「%s」を受け取った。", param), Toast.LENGTH_LONG).show();
    	
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, this.getClass().getSimpleName() + " - onCreate()", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroy() {
        Toast.makeText(this, this.getClass().getSimpleName() + " - onDestroy()", Toast.LENGTH_SHORT).show();
    }
    
    // 用意したインターフェース
    @Override
    public String getInfo() {
        // ★ポイント2★ 利用元アプリは同一アプリであるから、センシティブな情報を返してもよい
        return new String("センシティブな情報(from Service)");
    }
}