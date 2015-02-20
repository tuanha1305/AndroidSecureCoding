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

package org.jssec.android.service.inhouseservice.messenger;

import org.jssec.android.shared.SigPerm;
import org.jssec.android.shared.Utils;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class InhouseMessengerService extends Service{
	// 自社のSignature Permission
	private static final String MY_PERMISSION = "org.jssec.android.service.inhouseservice.messenger.MY_PERMISSION";
	
	// 自社の証明書のハッシュ値
	private static String sMyCertHash = null;
	private static String myCertHash(Context context) {
		if (sMyCertHash == null) {
			if (Utils.isDebuggable(context)) {
				// debug.keystoreの"androiddebugkey"の証明書ハッシュ値
				sMyCertHash = "0EFB7236 328348A9 89718BAD DF57F544 D5CCB4AE B9DB34BC 1E29DD26 F77C8255";
			} else {
				// keystoreの"my company key"の証明書ハッシュ値
				sMyCertHash = "D397D343 A5CBC10F 4EDDEB7C A10062DE 5690984F 1FB9E88B D7B3A7C2 42E142CA";
			}
		}
		return sMyCertHash;
	}
	
	
	// Serviceのクライアント(データ送信先)をリストで管理する
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    
    // クライアントからのデータを受信するときに利用するMessenger
    private final Messenger mMessenger = new Messenger(new ServiceSideHandler());
    
    // クライアントから受け取ったMessageを処理するHandler
    private class ServiceSideHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
            case CommonValue.MSG_REGISTER_CLIENT:
                // クライアントから受け取ったMessengerを追加
                mClients.add(msg.replyTo);
                break;
            case CommonValue.MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case CommonValue.MSG_SET_VALUE:
                // クライアントにデータを送る
                sendMessageToClients();
                break;
            default:
                super.handleMessage(msg);
               break;
            }
        }
    }
    
    /**
     * クライアントにデータを送る
     */
    private void sendMessageToClients(){
        
		// ★ポイント6★ 利用元アプリは自社アプリであるから、センシティブな情報を返送してよい
    	String sendValue = "センシティブな情報(from Service)";
        
        // 登録されているクライアントへ、順番に送信する
        // ループ途中でremoveしても全てのデータにアクセスしたいのでIteratorを利用する
        Iterator<Messenger> ite = mClients.iterator();
        while(ite.hasNext()){
            try {
                Message sendMsg = Message.obtain(null, CommonValue.MSG_SET_VALUE, null);

                Bundle data = new Bundle();
                data.putString("key", sendValue);
                sendMsg.setData(data);

                Messenger next = ite.next();
                next.send(sendMsg);
                
            } catch (RemoteException e) {
                // クライアントが存在しない場合は、リストから取り除く
                ite.remove();
            }
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
		
		// ★ポイント4★ 独自定義Signature Permissionが自社アプリにより定義されていることを確認する
		if (!SigPerm.test(this, MY_PERMISSION, myCertHash(this))) {
			Toast.makeText(this, "独自定義Signature Permissionが自社アプリにより定義されていない。", Toast.LENGTH_LONG).show();
    		return null;
    	}

		// ★ポイント5★ 自社アプリからのIntentであっても、受信Intentの安全性を確認する
		// サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
		String param = intent.getStringExtra("PARAM");
    	Toast.makeText(this, String.format("パラメータ「%s」を受け取った。", param), Toast.LENGTH_LONG).show();

    	return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service - onCreate()", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service - onDestroy()", Toast.LENGTH_SHORT).show();
    }
}