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

package org.jssec.android.sqlite;

public class CommonData {
    //各画面呼び出し時のリクエストコード
    public static final int REQUEST_NEW           = 0;
    public static final int REQUEST_EDIT          = 1;
    public static final int REQUEST_DELETE        = 2;
    public static final int REQUEST_SEARCH        = 3;
    
    //Intentデータ識別子
    public static final String EXTRA_REQUEST_MODE   = "EXTRA_REQUEST_MODE";
    public static final String EXTRA_ITEM_IDNO      = "EXTRA_ITEM_IDNO";
    public static final String EXTRA_ITEM_NAME      = "EXTRA_ITEM_NAME";
    public static final String EXTRA_ITEM_INFO      = "EXTRA_ITEM_INFO";
    public static final String EXTRA_TABLE_NAME     = "EXTRA_TABLE_NAME";
    
    // データベースファイル名
    public static final String DBFILE_NAME = "SQLiteDatabase.db";
    // データベースのバージョン
    public static final int DB_VERSION = 1;
    
    public static final String TABLE_NAME = "SQLiteTable";
    
    public static final Integer TEXT_DATA_LENGTH_MAX	   = 10;
    
}
