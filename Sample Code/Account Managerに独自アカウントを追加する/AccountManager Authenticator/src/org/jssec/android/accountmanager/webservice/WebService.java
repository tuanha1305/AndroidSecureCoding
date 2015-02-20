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

package org.jssec.android.accountmanager.webservice;

public class WebService {

	/**
	 * オンラインサービスのアカウント管理機能にアクセスする想定
	 * 
	 * @param username アカウント名文字列
	 * @param password パスワード文字列
	 * @return 認証トークンを返す
	 */
	public String login(String username, String password) {
		// ★ポイント7★ Authenticatorとオンラインサービスとの通信はHTTPSで行う
		// 実際には、サーバーとの通信処理を実装するが、 サンプルにつき割愛
		return getAuthToken(username, password);
	}

	private String getAuthToken(String username, String password) {
		// 実際にはサーバーから、ユニーク性と推測不可能性を保証された値を取得するが
		// サンプルにつき、通信は行わずに固定値を返す
		return "c2f981bda5f34f90c0419e171f60f45c";
	}
}