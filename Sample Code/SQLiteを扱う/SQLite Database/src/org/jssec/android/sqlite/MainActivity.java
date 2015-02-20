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

import org.jssec.android.sqlite.task.DataDeleteTask;
import org.jssec.android.sqlite.task.DataInsertTask;
import org.jssec.android.sqlite.task.DataSearchTask;
import org.jssec.android.sqlite.task.DataUpdateTask;
import org.jssec.android.sqlite.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity {
    private SampleDbOpenHelper       mSampleDbOpenHelper;   //データベースオープンヘルパー
    private Cursor                   mCursor = null;        //画面表示に使用するカーソル
    
    //設定中の検索条件
    private String  mItemIdNoSearch = null;
    private String  mItemNameSearch = null;
    private String  mItemInfoSearch = null;   
    
    //現在選択されている行の情報
    private String  mItemIdNo = null;
    private String  mItemName = null;
    private String  mItemInfo = null;   
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //OpenHelperの準備
        mSampleDbOpenHelper = SampleDbOpenHelper.newHelper(this);
        mSampleDbOpenHelper.openDatabaseWithHelper();
    }
    
    @Override 
    protected void onStart() {
        super.onStart();
        
        //リストが選択された時の処理
        ListView lv = (ListView)findViewById(R.id.DataList);
        lv.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setCurrentData(arg1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //選択行データのクリア
                clearCurrentData();
            }
        });

        //リストがクリックされた時の処理
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //選択された行のデータをセットする
                setCurrentData(arg1);
                //編集画面の呼び出し
                startEditActivity();
            }
        });

        //長押しされた時の処理
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //選択された行のデータをセットする
                setCurrentData(arg1);
                return false;
            }
        });

        //コンテクストメニューの準備
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu arg0, View arg1, ContextMenuInfo arg2) {
                //タイトルの設定
                String strTitle = mItemIdNo + "/" + mItemName; 
                arg0.setHeaderTitle(strTitle);
                // メニューの作成
                arg0.add(0, EDIT_MENUITEM_ID, 1, R.string.EDIT_MENUITEM);
                arg0.add(0, DELETE_MENUITEM_ID, 2, R.string.DELETE_MENUITEM);
            }
        });
        
        //初期データ作成
        initData();

        //DBデータの表示
        showDbData();
    }

    //現在選択されている行の情報をセットする
    private void setCurrentData(View currentLine) {
        mItemIdNo   = ((TextView)currentLine.findViewById(R.id.dlc_IdNo)).getText().toString();
        mItemName = ((TextView)currentLine.findViewById(R.id.dlc_Name)).getText().toString();
        mItemInfo = ((TextView)currentLine.findViewById(R.id.dlc_Info)).getText().toString();      
    }

    //現在選択されている行の情報をクリアする
    private void clearCurrentData() {
        mItemIdNo   = null;
        mItemName = null;
        mItemInfo = null;       
    }

    //DB版初期データ作成（テスト用）
    private void initData() {
        //データが１件でもあったらデータ投入しない
        Cursor cur = mSampleDbOpenHelper.getDb().rawQuery("SELECT * FROM " + CommonData.TABLE_NAME + " LIMIT 1", null);
        if (cur.getCount() > 0) {
            cur.close();
            return;   //データがあるので初期投入しない
        }

        //初期データの作成
        ContentValues insertValues = new ContentValues();
        for (int i = 1; i <= 31; i++) {
            insertValues.put("idno", String.valueOf(i));
            insertValues.put("name", "Name of User-" + String.valueOf(i));
            insertValues.put("info", "Info of User-" + String.valueOf(i));
            mSampleDbOpenHelper.getDb().insert(CommonData.TABLE_NAME, null, insertValues);
        }
    }
    
    //後始末
    @Override
    protected void onPause() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mSampleDbOpenHelper.closeDatabase();
        super.onDestroy();
    } 

    //-----------------------------------------------------------------------------
    // メニュー関連
    //-----------------------------------------------------------------------------
    private static final int INSERT_MENUITEM_ID = Menu.FIRST + 1;
    private static final int SEARCH_MENUITEM_ID = Menu.FIRST + 2;
    private static final int EDIT_MENUITEM_ID   = Menu.FIRST + 3;
    private static final int DELETE_MENUITEM_ID = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.add(0, INSERT_MENUITEM_ID, 0, R.string.INSERT_MENUITEM);
        item.setIcon(android.R.drawable.ic_menu_add);
        item = menu.add(0, SEARCH_MENUITEM_ID, 0, R.string.SEARCH_MENUITEM);
        item.setIcon(android.R.drawable.ic_menu_search);
        return true;
    }
 
    /**
     * メニュー選択時
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {           
        switch (item.getItemId()) {
        case INSERT_MENUITEM_ID:
            //編集画面を新規追加モードで呼び出す
            startAddActivity();
            break;
        case SEARCH_MENUITEM_ID:
            //検索画面を新規追加モードで呼び出す
        	 startSearchActivity();
            break;
        case EDIT_MENUITEM_ID:
            //編集画面を編集モードで呼び出す
            startEditActivity();
            break;
        case DELETE_MENUITEM_ID:
            //削除確認画面を呼び出す
            startDeleteActivity();
            break;
        default:
            break;
        }

        return super.onMenuItemSelected(featureId, item);
    }
    //-----------------------------------------------------------------------------
    //  子画面の呼び出し
    //-----------------------------------------------------------------------------
    //新規画面の呼び出し
    private void startAddActivity() {
        Intent intentAdd = new Intent(this, EditActivity.class);
        intentAdd.putExtra(CommonData.EXTRA_REQUEST_MODE, CommonData.REQUEST_NEW);
        startActivityForResult(intentAdd, CommonData.REQUEST_NEW);
    }

    private Intent newIntent(Context context, Class<?> cls,
    		String idno, String name, String info) {
        Intent intent = new Intent(context, cls);

        //データをIntentに設定する。
        intent.putExtra(CommonData.EXTRA_ITEM_IDNO, idno);
        intent.putExtra(CommonData.EXTRA_ITEM_NAME, name);
        intent.putExtra(CommonData.EXTRA_ITEM_INFO, info);            

        return intent;
    }
    
    //検索開始画面の呼び出し
    private void startSearchActivity() {
        Intent intentSearch = newIntent(this, SearchActivity.class, 
        							mItemIdNoSearch, mItemNameSearch, mItemInfoSearch);
           
        startActivityForResult(intentSearch, CommonData.REQUEST_SEARCH);
    }
    
    //編集画面の呼び出し
    private void startEditActivity() {
        Intent intentEdit = newIntent(this, EditActivity.class, 
        									mItemIdNo, mItemName, mItemInfo);
        intentEdit.putExtra(CommonData.EXTRA_REQUEST_MODE, CommonData.REQUEST_EDIT);

        startActivityForResult(intentEdit, CommonData.REQUEST_EDIT);
    }

    //削除画面の呼び出し
    private void startDeleteActivity() {
        Intent intentDelete = newIntent(this, DeleteActivity.class, 
        									mItemIdNo, mItemName, mItemInfo);

        startActivityForResult(intentDelete, CommonData.REQUEST_DELETE);
    }

    /* 編集・削除・検索画面から戻った時の処理 */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        //キャンセルの場合は何もしない
        if (resultCode == Activity.RESULT_CANCELED) {
            showDbData();            //ユーザー情報の表示
            return;
        }

        //リクエストコード毎に処理を分ける
        switch(requestCode) {
        case CommonData.REQUEST_NEW:
            addUserData(data.getStringExtra(CommonData.EXTRA_ITEM_IDNO), 
                        data.getStringExtra(CommonData.EXTRA_ITEM_NAME), 
                        data.getStringExtra(CommonData.EXTRA_ITEM_INFO));
            break;
        case CommonData.REQUEST_SEARCH:
        	  searchUserData(data.getStringExtra(CommonData.EXTRA_ITEM_IDNO), 
                      data.getStringExtra(CommonData.EXTRA_ITEM_NAME), 
                      data.getStringExtra(CommonData.EXTRA_ITEM_INFO));
            break;
        case CommonData.REQUEST_EDIT:
            editUserData(data.getStringExtra(CommonData.EXTRA_ITEM_IDNO), 
                         data.getStringExtra(CommonData.EXTRA_ITEM_NAME), 
                         data.getStringExtra(CommonData.EXTRA_ITEM_INFO));
            break;
        case CommonData.REQUEST_DELETE:
            deleteUserData(data.getStringExtra(CommonData.EXTRA_ITEM_IDNO));
            break;
        default:
            break;
        }
    }
    
    //データを画面に反映する
    public void updateCursor(Cursor cur) {
        //アダプターの作成
        String  cols2[]  =   {"idno","name","info"};
        int     views[] = {R.id.dlc_IdNo, R.id.dlc_Name, R.id.dlc_Info};

        //リストビューの取得
        ListView lv = (ListView)findViewById(R.id.DataList);

        //以前のカーソルがあったら破棄
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }

        mCursor = cur;

        //画面表示用データの設定
        CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.data_list_contents, mCursor, cols2, views);
        lv.setAdapter(adapter);
    }

   //---------------------------------------
    //DBに対する処理
    //---------------------------------------
    //DBデータ表示処理
    public void showDbData() {
        //画面表示処理（非同期タスク）
        DataSearchTask task = new DataSearchTask(mSampleDbOpenHelper.getDb(), this);
        task.execute(mItemIdNoSearch, mItemNameSearch, mItemInfoSearch);
    }
    
    //追加処理
    private void addUserData(String idno, String name, String info) {
        //データ追加処理
        DataInsertTask task = new DataInsertTask(mSampleDbOpenHelper.getDb(), this);
        task.execute(idno, name, info);        
    }
    
    //検索処理
    private void searchUserData(String idno, String name, String info) {
        mItemIdNoSearch   = idno; 

        mItemNameSearch = name;
        mItemInfoSearch = info;

        //データ検索処理
        DataSearchTask task = new DataSearchTask(mSampleDbOpenHelper.getDb(), this);
        task.execute(idno, name, info);
        }
    
    //更新処理
    private void editUserData(String idno, String name, String info) {
        DataUpdateTask task = new DataUpdateTask(mSampleDbOpenHelper.getDb(), this);
        task.execute(idno, name, info);
    }

    //削除処理
    private void deleteUserData(String idno) {     
        DataDeleteTask task = new DataDeleteTask(mSampleDbOpenHelper.getDb(), this);
        task.execute(idno);
    }
}