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

package org.jssec.android.service.inhouseservice.messengeruser;

import org.jssec.android.shared.PkgCert;
import org.jssec.android.shared.SigPerm;
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
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

public class InhouseMessengerUserActivity extends Activity {

    private boolean mIsBound;
    private Context mContext;

    // 利用先のActivity情報
    private static final String TARGET_PACKAGE =  "org.jssec.android.service.inhouseservice.messenger";
    private static final String TARGET_CLASS = "org.jssec.android.service.inhouseservice.messenger.InhouseMessengerService";

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

    // Serviceからデータを受信するときに利用するMessenger
    private Messenger mServiceMessenger = null;

    // Serviceにデータを送信するときに利用するMessenger
    private final Messenger mActivityMessenger = new Messenger(new ActivitySideHandler());

    // Serviceから受け取ったMessageを処理するHandler
    private class ActivitySideHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonValue.MSG_SET_VALUE:
                    Bundle data = msg.getData();
                    String info = data.getString("key");
                       // ★ポイント13★ 自社アプリからの結果情報であっても、値の安全性を確認する
                       // サンプルにつき割愛。「3.2 入力データの安全性を確認する」を参照。
                    Toast.makeText(mContext, String.format("サービスから「%s」を取得した。", info),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    // Serviceと接続する時に利用するコネクション。bindServiceで実装する場合は必要になる。
    private ServiceConnection mConnection = new ServiceConnection() {

        // Serviceに接続された場合に呼ばれる
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mServiceMessenger = new Messenger(service);
            Toast.makeText(mContext, "Connect to service", Toast.LENGTH_SHORT).show();

            try {
                // Serviceに自分のMessengerを渡す
                Message msg = Message.obtain(null, CommonValue.MSG_REGISTER_CLIENT);
                msg.replyTo = mActivityMessenger;
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                // Serviceが異常終了していた場合
            }
        }

        // Serviceが異常終了して、コネクションが切断された場合に呼ばれる
        @Override
        public void onServiceDisconnected(ComponentName className) {
            mServiceMessenger = null;
            Toast.makeText(mContext, "Disconnected from service", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.publicservice_activity);

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
        if (!mIsBound){
            // ★ポイント9★ 独自定義Signature Permissionが自社アプリにより定義されていることを確認する
            if (!SigPerm.test(this, MY_PERMISSION, myCertHash(this))) {
                Toast.makeText(this, "独自定義Signature Permissionが自社アプリにより定義されていない。", Toast.LENGTH_LONG).show();
                return;
            }

            // ★ポイント10★ 利用先アプリの証明書が自社の証明書であることを確認する
            if (!PkgCert.test(this, TARGET_PACKAGE, myCertHash(this))) {
                Toast.makeText(this, "利用先サービスは自社アプリではない。", Toast.LENGTH_LONG).show();
                return;
            }

          Intent intent = new Intent();

            // ★ポイント11★ 利用先アプリは自社アプリであるから、センシティブな情報を送信してもよい
          intent.putExtra("PARAM", "センシティブな情報");

            // ★ポイント12★ 明示的Intentにより自社限定Serviceを呼び出す
          intent.setClassName(TARGET_PACKAGE, TARGET_CLASS);

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
        if (mServiceMessenger != null) {
            try {
                  // 情報の送信を要求する
                Message msg = Message.obtain(null, CommonValue.MSG_SET_VALUE);
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                // Serviceが異常終了していた場合
            }
         }
    }

}