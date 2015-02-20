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

package org.jssec.android.privacypolicy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ConfirmFragment extends DialogFragment {

	private DialogListener mListener = null;

	public static interface DialogListener {
		public void onPositiveButtonClick(int type);

		public void onNegativeButtonClick(int type);
	}

	public static ConfirmFragment newInstance(int title, int sentence, int type) {
		ConfirmFragment fragment = new ConfirmFragment();
		Bundle args = new Bundle();
		args.putInt("title", title);
		args.putInt("sentence", sentence);
		args.putInt("type", type);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle args) {
		// ★ポイント1★ 初回起動時に、アプリが扱う利用者情報の送信について包括同意を得る
		// ★ポイント3★ 慎重な取り扱いが求められる利用者情報を送信する場合は、個別にユーザーの同意を得る
		final int title = getArguments().getInt("title");
		final int sentence = getArguments().getInt("sentence");
		final int type = getArguments().getInt("type");

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.fragment_comfirm, null);
		TextView linkPP = (TextView) content.findViewById(R.id.tx_link_pp);
		linkPP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ★ポイント5★ ユーザーがアプリ・プライバシーポリシーを確認できる手段を用意する
				Intent intent = new Intent();
				intent.setClass(getActivity(), WebViewAssetsActivity.class);
				startActivity(intent);
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(title);
		builder.setMessage(sentence);
		builder.setView(content);

		builder.setPositiveButton(R.string.buttonOK, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mListener != null) {
					mListener.onPositiveButtonClick(type);
				}
			}
		});
		builder.setNegativeButton(R.string.buttonNG, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mListener != null) {
					mListener.onNegativeButtonClick(type);
				}
			}
		});

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);

		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof DialogListener == false) {
			throw new ClassCastException(activity.toString() + " must implement DialogListener.");
		}
		mListener = (DialogListener) activity;
	}

	public void setDialogListener(DialogListener listener) {
		mListener = listener;
	}
}
