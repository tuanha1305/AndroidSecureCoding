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
package org.jssec.android.service.partnerservice.aidl;

import org.jssec.android.service.partnerservice.aidl.IPartnerAIDLService;
import org.jssec.android.service.partnerservice.aidl.IPartnerAIDLServiceCallback;
import org.jssec.android.shared.PkgCertWhitelists;
import org.jssec.android.shared.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.widget.Toast;

public class PartnerAIDLService extends Service {
	   private static final int REPORT_MSG = 1;
	   private static final int GETINFO_MSG = 2;
    
    // Serviceからクライアントに通知する値
    private int mValue = 0;

	// ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
	private static PkgCertWhitelists sWhitelists = null;
	private static void buildWhitelists(Context context) {
		boolean isdebug = Utils.isDebuggable(context);
		sWhitelists = new PkgCertWhitelists();
		
		// パートナーアプリ org.jssec.android.service.partnerservice.aidluser の証明書ハッシュ値を登録
		sWhitelists.add("org.jssec.android.service.partnerservice.aidluser", isdebug ?
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
    			"0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255" :
				// keystoreの"partner key"の証明書ハッシュ値
    			"1F039BB5 7861C27A 3916C778 8E78CE00 690B3974 3EB8259F E2627B8D 4C0EC35A");
		
		// 以下同様に他のパートナーアプリを登録...
	}
	
	private static boolean checkPartner(Context context, String pkgname) {
		if (sWhitelists == null) buildWhitelists(context);
		return sWhitelists.test(context, pkgname);
	}
    
	// コールバックを登録するオブジェクト。
    // RemoteCallbackList の提供するメソッドはスレッドセーフになっている。
    private final RemoteCallbackList<IPartnerAIDLServiceCallback> mCallbacks =
        new RemoteCallbackList<IPartnerAIDLServiceCallback>();

    // コールバックに対してServiceからデータを送信するためのHandler
    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
            case REPORT_MSG: {
                // 通知を開始する
                // beginBroadcast()は、getBroadcastItem()で取得可能なコピーを作成している
                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                	IPartnerAIDLServiceCallback target = mCallbacks.getBroadcastItem(i);
                    try {
                            // ★ポイント5★ パートナーアプリに開示してよい情報に限り送信してよい
                        target.valueChanged("パートナーアプリに開示してよい情報(callback from Service) No." + (++mValue));
                        
                    } catch (RemoteException e) {
                        // RemoteCallbackListがコールバックを管理しているので、ここではunregeisterしない
                        // RemoteCallbackList.kill()によって全て解除される
                    }
                }
                // finishBroadcast()は、beginBroadcast()と対になる処理
                mCallbacks.finishBroadcast();
                
                // 10秒後に繰り返す
                sendEmptyMessageDelayed(REPORT_MSG, 10000);
                break;
             }
            case GETINFO_MSG: {
                Toast.makeText(PartnerAIDLService.this, 
             		   (String)msg.obj, Toast.LENGTH_LONG).show();
            	break;
              }
            default:
                super.handleMessage(msg);
                break;
            } // switch
        }
    };
    
    // AIDLで定義したインターフェース
    private final IPartnerAIDLService.Stub mBinder = new IPartnerAIDLService.Stub() {
    	private final boolean checkPartner() {
        	Context ctx = PartnerAIDLService.this;
            if (!PartnerAIDLService.checkPartner(ctx, Utils.getPackageNameFromPid(ctx, getCallingPid()))) {
            	Toast.makeText(ctx, "利用元アプリはパートナーアプリではない。", Toast.LENGTH_LONG).show();
            	return false;
            }
           return true;
    	}
        public void registerCallback(IPartnerAIDLServiceCallback cb) {
            // ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
          if (!checkPartner()) {
            	return;
            }
          if (cb != null) mCallbacks.register(cb);
        }
        public String getInfo(String param) {
            // ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
          if (!checkPartner()) {
            	return null;
            }
    		// ★ポイント4★ パートナーアプリからのIntentであっても、受信Intentの安全性を確認する
    		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
          Message msg = new Message();
          msg.what = GETINFO_MSG;
          msg.obj = String.format("パートナーアプリからのメソッド呼び出し。「%s」を受信した。", param);
          PartnerAIDLService.this.mHandler.sendMessage(msg);
            
            // ★ポイント5★ パートナーアプリに開示してよい情報に限り返送してよい
    		return new String("パートナーアプリに開示してよい情報(method from Service)");        	
        }
        
        public void unregisterCallback(IPartnerAIDLServiceCallback cb) {
            // ★ポイント2★ 利用元アプリの証明書がホワイトリストに登録されていることを確認する
           if (!checkPartner()) {
            	return;
            }
 
           if (cb != null) mCallbacks.unregister(cb);
        }
    };
    
    @Override
    public IBinder onBind(Intent intent) {
    	// ★ポイント3★ onBindで呼び出し元がパートナーかどうか判別できない
		// AIDL で定義したメソッドの呼び出し毎にチェックが必要になる。

        return mBinder;
    }
    
    @Override
    public void onCreate() {
        Toast.makeText(this, this.getClass().getSimpleName() + " - onCreate()", Toast.LENGTH_SHORT).show();       

        // Serviceが実行中の間は、定期的にインクリメントした数字を通知する
        mHandler.sendEmptyMessage(REPORT_MSG);
    }
    
    @Override
    public void onDestroy() {
        Toast.makeText(this, this.getClass().getSimpleName() + " - onDestroy()", Toast.LENGTH_SHORT).show();
        
        // コールバックを全て解除する
        mCallbacks.kill();
        
        mHandler.removeMessages(REPORT_MSG);
    }
}