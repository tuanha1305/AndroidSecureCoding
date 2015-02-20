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

public class DataValidator {
    //入力値をチェックする
    //数字チェック
    public static boolean validateNo(String idno) {
        //null、空文字はOK
        if (idno == null || idno.length() == 0) {
           return true;
        }

        //数字であることを確認する
        try {
            if (!idno.matches("[1-9][0-9]*")) {
                //数字以外の時はエラー
                return false;
            }
        } catch (NullPointerException e) {
            //エラーを検出した
            return false;
        }

        return true;
    }

    // 文字列の長さを調べる
    public static boolean validateLength(String str, int max_length) {
       //null、空文字はOK
       if (str == null || str.length() == 0) {
               return true;
       }

       //文字列の長さがMAX以下であることを調べる
       try {
           if (str.length() > max_length) {
               //MAXより長い時はエラー
               return false;
           }
       } catch (NullPointerException e) {
           //バグ
           return false;
       }

       return true;
    }

   // 入力値チェック
    public static boolean validateData(String idno, String name, String info) {
       if (!validateNo(idno)) {
           return false;
        }
       if (!validateLength(name, CommonData.TEXT_DATA_LENGTH_MAX)) {
           return false;
        }
       if (!validateLength(info, CommonData.TEXT_DATA_LENGTH_MAX)) {
           return false;
        }    
       return true;
    }
}
