/*
 * Copyright (C) 2012 Japan Smartphone Security Association
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

import org.jssec.android.service.partnerservice.aidl.IPartnerAIDLServiceCallback;

interface IPartnerAIDLService {

    /**
     * コールバックを登録する
     */
    void registerCallback(IPartnerAIDLServiceCallback cb);
    
    /**
     * 情報を取得する
     */     
    String getInfo(String param);

    /**
     * コールバックを解除する
     */
    void unregisterCallback(IPartnerAIDLServiceCallback cb);
}
