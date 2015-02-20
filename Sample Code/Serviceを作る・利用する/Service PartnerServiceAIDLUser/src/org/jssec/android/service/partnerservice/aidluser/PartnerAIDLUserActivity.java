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
package org.jssec.android.service.partnerservice.aidluser;

import org.jssec.android.service.partnerservice.aidl.IPartnerAIDLService;
import org.jssec.android.service.partnerservice.aidl.IPartnerAIDLServiceCallback;
import org.jssec.android.shared.PkgCertWhitelists;
import org.jssec.android.shared.Utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

public class PartnerAIDLUserActivity extends Activity {

    private boolean mIsBound;
    private Context mContext;
    
    private final static int MGS_VALUE_CHANGED = 1;
    
	// ★ポイント6★ 利用先パートナー限定Serviceアプリの証明書がホワイトリストに登録されていることを確認する
	private static PkgCertWhitelists sWhitelists = null;
	private static void buildWhitelists(Context context) {
		boolean isdebug = Utils.isDebuggable(context);
		sWhitelists = new PkgCertWhitelists();
		
		// パートナー限定Serviceアプリ org.jssec.android.service.partnerservice.aidl の証明書ハッシュ値を登録
		sWhitelists.add("org.jssec.android.service.partnerservice.aidl", isdebug ?
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
    			"0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255" :
				// keystoreの"my company key"の証明書ハッシュ値
    			"D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA");
		
		// 以下同様に他のパートナー限定Serviceアプリを登録...
	}
	private static boolean checkPartner(Context context, String pkgname) {
		if (sWhitelists == null) buildWhitelists(context);
		return sWhitelists.test(context, pkgname);
	}

    // 利用先のパートナー限定Activityに関する情報
    private static final String TARGET_PACKAGE =  "org.jssec.android.service.partnerservice.aidl";
    private static final String TARGET_CLASS = "org.jssec.android.service.partnerservice.aidl.PartnerAIDLService";

    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
	            case MGS_VALUE_CHANGED: {
	            	  String info = (String)msg.obj;
	                Toast.makeText(mContext, String.format("コールバックで「%s」を受信した。", info), Toast.LENGTH_SHORT).show();
	                break;
	              }
	            default:
	                super.handleMessage(msg);
	                break;
	       } // switch
        }    
    };
    
    // AIDLで定義したインターフェース。Serviceからの通知を受け取る。
    private final IPartnerAIDLServiceCallback.Stub mCallback =
        new IPartnerAIDLServiceCallback.Stub() {
            @Override
            public void valueChanged(String info) throws RemoteException {
            	Message msg = mHandler.obtainMessage(MGS_VALUE_CHANGED, info);
            	mHandler.sendMessage(msg);
            }
    };
    
    // AIDLで定義したインターフェース。Serviceへ通知する。
    private IPartnerAIDLService mService = null;
    
    // Serviceと接続する時に利用するコネクション。bindServiceで実装する場合は必要になる。
    private ServiceConnection mConnection = new ServiceConnection() {

        // Serviceに接続された場合に呼ばれる
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IPartnerAIDLService.Stub.asInterface(service);
            
            try{
                // Serviceに接続
                mService.registerCallback(mCallback);
                
            }catch(RemoteException e){
                // Serviceが異常終了した場合
            }
            
            Toast.makeText(mContext, "Connected to service", Toast.LENGTH_SHORT).show();
        }

        // Serviceが異常終了して、コネクションが切断された場合に呼ばれる
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Toast.makeText(mContext, "Disconnected from service", Toast.LENGTH_SHORT).show();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.partnerservice_activity);

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
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    /**
     * Serviceに接続する
     */
    private void doBindService() {
        if (!mIsBound){
            // ★ポイント6★ 利用先パートナー限定Serviceアプリの証明書がホワイトリストに登録されていることを確認する
        	if (!checkPartner(this, TARGET_PACKAGE)) {
            	Toast.makeText(this, "利用先 Service アプリはホワイトリストに登録されていない。", Toast.LENGTH_LONG).show();
                return;
            }
            
        	Intent intent = new Intent();
        	
            // ★ポイント7★ 利用先パートナー限定アプリに開示してよい情報に限り送信してよい
        	intent.putExtra("PARAM", "パートナーアプリに開示してよい情報");
        	
        	// ★ポイント8★ 明示的Intentによりパートナー限定Serviceを呼び出す
        	intent.setClassName(TARGET_PACKAGE, TARGET_CLASS);
            	 
	      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	      mIsBound = true;
        }
    }

    /**
     * Serviceへの接続を切断する
     */
    private void doUnbindService() {
        if (mIsBound) {
            // 登録していたレジスタがある場合は解除
            if(mService != null){
                try{
                    mService.unregisterCallback(mCallback);
                }catch(RemoteException e){
                    // Serviceが異常終了していた場合
                    // サンプルにつき処理は割愛
                }
            }
            
          unbindService(mConnection);
            
          Intent intent = new Intent();
    	
           // ★ポイント8★ 明示的Intentによりパートナー限定Serviceを呼び出す
          intent.setClassName(TARGET_PACKAGE, TARGET_CLASS);
 
          stopService(intent);
          
          mIsBound = false;
        }
    }

    /**
     * Serviceから情報を取得する
     */
    void getServiceinfo() {
        if (mIsBound && mService != null) {
            String info = null;
            
            try {
                // ★ポイント7★ 利用先パートナー限定アプリに開示してよい情報に限り送信してよい
            	info = mService.getInfo(new String("パートナーアプリに開示してよい情報(method from activity)"));
			} catch (RemoteException e) {
			}
			// ★ポイント9★ パートナー限定アプリからの結果情報であっても、受信Intentの安全性を確認する
			// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。            
            Toast.makeText(mContext, String.format("サービスから「%s」を取得した。", info), Toast.LENGTH_SHORT).show();
         }
    }
}