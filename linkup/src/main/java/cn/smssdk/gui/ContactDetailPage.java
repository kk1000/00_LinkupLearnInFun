/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */
package cn.smssdk.gui;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.smssdk.framework.FakeActivity;
import static cn.smssdk.framework.utils.R.*;

/**联系人详细信息页面*/
public class ContactDetailPage extends FakeActivity implements OnClickListener{

	private String phoneName = "";
	private ArrayList<String> phoneList = new ArrayList<String>();

	@Override
	public void onCreate() {
		int resId = getLayoutRes(activity, "smssdk_contact_detail_page");
		if (resId > 0) {
			activity.setContentView(resId);
			resId = getIdRes(activity, "ll_back");
			activity.findViewById(resId).setOnClickListener(this);
			resId = getIdRes(activity, "tv_title");
			TextView tvTitle = (TextView) activity.findViewById(resId);
			resId = getStringRes(activity, "smssdk_contacts_detail");
			tvTitle.setText(resId);

			resId = getIdRes(activity, "tv_contact_name");
			TextView tvContactName = (TextView) activity.findViewById(resId);
			tvContactName.setText(phoneName);

			resId = getIdRes(activity, "tv_contact_phones");
			TextView tvPhonesList = (TextView) activity.findViewById(resId);
			StringBuilder phones = new StringBuilder();
			for(String phone : phoneList){
				phones.append("\n");
				phones.append(phone);
			}
			if(phones.length() > 0){
				phones.deleteCharAt(0);
				tvPhonesList.setText(phones.toString());
			}

			resId = getIdRes(activity, "tv_invite_hint");
			TextView tvInviteHint = (TextView) activity.findViewById(resId);
			resId = getStringRes(activity, "smssdk_not_invite");
			String hint = getContext().getResources().getString(resId, phoneName);
			tvInviteHint.setText(Html.fromHtml(hint));

			resId = getIdRes(activity, "btn_invite");
			activity.findViewById(resId).setOnClickListener(this);
		}
	}

	@Override
	public void onResume(){
	  	super.onResume();
	}

	@Override
	public void onPause() {
	   	super.onPause();
	}

	/**
	 * 设置联系人对象
	 * @param HashMap<String, Object> contact
	 */
	@SuppressWarnings("unchecked")
	public void setContact(HashMap<String, Object> contact){
		if(contact.containsKey("displayname")){
			phoneName = String.valueOf(contact.get("displayname"));
		} else if (contact.containsKey("phones")) {
			ArrayList<HashMap<String, Object>> phones
					= (ArrayList<HashMap<String, Object>>) contact.get("phones");
			if (phones != null && phones.size() > 0) {
				phoneName = (String) phones.get(0).get("phone");
			}
		}
		ArrayList<HashMap<String, Object>> phones = (ArrayList<HashMap<String, Object>>) contact.get("phones");
		if (phones != null && phones.size() > 0) {
			for (HashMap<String, Object> phone : phones) {
				String pn = (String) phone.get("phone");
				phoneList.add(pn);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		int id_ll_back = getIdRes(activity, "ll_back");
		int id_btn_invite = getIdRes(activity, "btn_invite");
		if (id == id_ll_back) {
			finish();
		} else if (id == id_btn_invite) {
			//发送短信，如果有多个号码，就弹出对话框，让用户自己选择
			if(phoneList.size()>1){
				showDialog();
				return;
			}else{
				String phone = phoneList.size() > 0 ? phoneList.get(0) : "";
				sendMsg(phone);
			}
		}
	}

	/**
	 * 发送消息
	 * @param String phone
	 */
	private void sendMsg(String phone){
		Uri smsToUri = Uri.parse("smsto:"+phone);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		int resId = getStringRes(activity, "smssdk_invite_content");
		if (resId > 0) {
			intent.putExtra("sms_body", activity.getString(resId));
		}
		startActivity(intent);
	}

	/**有多个电话号码时，弹出的选择对话框*/
	private void showDialog() {
		String[] phones = new String[phoneList.size()];
		phones = phoneList.toArray(phones);
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		int resId = getStringRes(activity, "smssdk_invite_content");
		if (resId > 0) {
			builder.setTitle(resId);
		}
		builder.setCancelable(true);
		resId = getStringRes(activity, "smssdk_cancel");
		if (resId > 0) {
			builder.setNegativeButton(resId, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}

			});
		}
		builder.setItems(phones, new DialogInterface.OnClickListener() {
		@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				sendMsg(phoneList.get(which));
			}
		});
		builder.create().show();
	}


}
