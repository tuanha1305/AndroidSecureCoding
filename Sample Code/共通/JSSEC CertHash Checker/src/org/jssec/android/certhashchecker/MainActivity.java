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

package org.jssec.android.certhashchecker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class MainActivity extends ListActivity {
	private List<String> mAppNames = new ArrayList<String>();
	private List<String> mPkgNames = new ArrayList<String>();
	private List<String> mPkgHashs = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        loadPackages();
        setupListView();
    }
    
    private void loadPackages() {
    	mAppNames.clear();
    	mPkgNames.clear();
    	
    	String mypkgname = this.getPackageName();
    	PackageManager pm = getPackageManager();
    	List<PackageInfo> pkgs = pm.getInstalledPackages(0);
    	for (PackageInfo pkg : pkgs)	{
    		String pkgname = pkg.packageName;
    		if (mypkgname.equals(pkgname)) continue;
    		if (pkgname.startsWith("com.android.")) continue;
    		if (pkgname.startsWith("com.google.")) continue;
    		String appname = pkg.applicationInfo.loadLabel(pm).toString();
    		mAppNames.add(appname);
    		mPkgNames.add(pkgname);
    		mPkgHashs.add(packageHash(pkgname));
    	}
    }
    private String packageHash(String pkgname) {
    	String hashstr = "error";
    	try {
			PackageInfo pkginfo = getPackageManager().getPackageInfo(pkgname, PackageManager.GET_SIGNATURES);
			if (pkginfo.signatures.length == 1) {
				Signature sig = pkginfo.signatures[0];
				byte[] cert = sig.toByteArray();
				byte[] sha256 = computeSha256(cert);
				hashstr = byte2hex(sha256);
			} else {
				hashstr = String.format("not single, %d signatures.", pkginfo.signatures.length);
			}
		} catch (NameNotFoundException e) {
			hashstr = String.format("exception: %s", e.getMessage());
		}
    	return hashstr;
    }
    private byte[] computeSha256(byte[] data) {
    	try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
    }
    private String byte2hex(byte[] data) {
    	if (data == null) return null;
    	final String digit = "0123456789ABCDEF";
    	StringBuilder sb = new StringBuilder();
    	int i = 4;
    	for (byte b : data) {
    		if (i == 0) { sb.append(' '); i = 4; }
    		i--;
    		int h = (b >> 4) & 15;
    		int l = b & 15;
    		sb.append(digit.charAt(h));
    		sb.append(digit.charAt(l));
    	}
    	return sb.toString();
    }
    
    private void setupListView() {
    	List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
    	for (int i=0; i<mAppNames.size(); i++) {
    		String appname = mAppNames.get(i);
    		String pkgname = mPkgNames.get(i);
    		String pkghash = mPkgHashs.get(i);
    		Map<String, String> data = new HashMap<String, String>();
    		data.put("appname", appname);
			data.put("pkgname", String.format("package: %s\nsha-256: %s", pkgname, pkghash));
    		dataList.add(data);
    	}
    	SimpleAdapter adapter = new SimpleAdapter(
    			this, dataList,
    			android.R.layout.simple_list_item_2,
    			new String[] { "appname", "pkgname" },
    			new int[] { android.R.id.text1, android.R.id.text2 });
    	setListAdapter(adapter);
    }
}