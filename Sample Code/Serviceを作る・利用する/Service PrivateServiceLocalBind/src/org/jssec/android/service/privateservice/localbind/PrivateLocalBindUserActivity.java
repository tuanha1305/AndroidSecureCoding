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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

public class PrivateLocalBindUserActivity extends Activity {

    private boolean mIsBound;
    private Context mContext;

    // Serviceに実装するインターフェースは、IPrivateLocalBindService クラスとして定義ている
    private IPrivateLocalBindService mServiceInterface;
    
    // Serviceと接続する時に利用するコネクション。bindServiceで実装する場合は必要になる。
    private ServiceConnection mConnection = new ServiceConnection() {

        // Serviceに接続された場合に呼ばれる
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mServiceInterface = ((PrivateLocalBindService.LocalBinder)service).getService();
            Toast.makeText(mContext, "Connect to service", Toast.LENGTH_SHORT).show();
        }
        // Serviceが異常終了して、コネクションが切断された場合に呼ばれる
        @Override
        public void onServiceDisconnected(ComponentName className) {
            // Serviceは利用できないのでnullをセット
            mServiceInterface = null;
            Toast.makeText(mContext, "Disconnected from service", Toast.LENGTH_SHORT).show();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privateservice_activity);

        mContext = this;
    }

    // サービス開始ボタン
	public void onStartServiceClick(View v) {
	    // bindServiceを実行する
	    doBindService();
	}
	
	// 情報取得ボタン
	public void onGetInfoClick(View v) {
		getServiceinfo();
    }
	
	// サービス停止ボタン
	public void onStopServiceClick(View v) {
	    doUnbindService();
	}
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    /**
     * Serviceに接続する
     */
    void doBindService() {
       if (!mIsBound)
        {
           // ★ポイント1★ 同一アプリ内Serviceはクラス指定の明示的Intentで呼び出す  
    	   Intent intent = new Intent(this, PrivateLocalBindService.class);
           
    	   // ★ポイント2★ 利用先アプリは同一アプリであるから、センシティブな情報を送信してもよい
            intent.putExtra("PARAM", "センシティブな情報(from activity)");
            
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    /**
     * Serviceへの接続を切断する
     */
    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * Serviceから情報を取得する
     */
    void getServiceinfo() {
        if (mIsBound) {
            String info = mServiceInterface.getInfo();
            
            Toast.makeText(mContext, String.format("サービスから「%s」を取得した。", info), Toast.LENGTH_SHORT).show();
         }
    }
}