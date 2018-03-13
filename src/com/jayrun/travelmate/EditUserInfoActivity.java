package com.jayrun.travelmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import com.jayrun.beans.User;
import com.jayrun.utils.Constants;

public class EditUserInfoActivity extends Activity implements OnClickListener {
	private TextView back;
	private TextView editOrSubmit;
	private EditText nickName;
	private EditText signature;
	private TextView sex;
	private EditText age;
	private EditText email;
	private EditText work;
	private EditText interest;
	private EditText school;
	private EditText address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_user_info);
		init();
		initCurrentUserInfo();
	}

	private void init() {
		back = (TextView) findViewById(R.id.edit_info_back);
		back.setOnClickListener(this);
		editOrSubmit = (TextView) findViewById(R.id.edit_info_ok);
		editOrSubmit.setOnClickListener(this);
		nickName = (EditText) findViewById(R.id.edit_nick_name);
		signature = (EditText) findViewById(R.id.edit_signature);
		sex = (TextView) findViewById(R.id.edit_sex);
		age = (EditText) findViewById(R.id.edit_age);
		email = (EditText) findViewById(R.id.edit_email);
		work = (EditText) findViewById(R.id.edit_work);
		interest = (EditText) findViewById(R.id.edit_interest);
		school = (EditText) findViewById(R.id.edit_school);
		address = (EditText) findViewById(R.id.edit_address);
	}

	private void initCurrentUserInfo() {
		User user = BmobUser.getCurrentUser(User.class);
		if (user == null) {
			Toast.makeText(EditUserInfoActivity.this, "无用户信息,请重新登陆后再试",
					Toast.LENGTH_LONG).show();
			finish();
		} else {
			nickName.setText(user.getNickName());
			signature.setText(user.getSignature());
			sex.setText(user.getSex());
			age.setText(user.getAge() + "");
			email.setText(user.getEmail());
			work.setText(user.getWork());
			interest.setText(user.getInterest());
			school.setText(user.getSchool());
			address.setText(user.getAddress());
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.edit_info_back:
			finish();
			break;
		case R.id.edit_info_ok:
			if (editOrSubmit.getText().toString().equals("完成")) {
				submitInfo();
			} else if (editOrSubmit.getText().toString().equals("编辑")) {
				changeToEditable();
			}
			break;
		default:
			break;
		}
	}

	private void submitInfo() {
		if (Constants.getNoBlankString(nickName.getText().toString()).isEmpty()) {
			Toast.makeText(EditUserInfoActivity.this, "昵称不能为空哦！",
					Toast.LENGTH_SHORT).show();
			nickName.setText("");
		} else {
			User newUser = new User();
			newUser.setNickName(nickName.getText().toString());
			newUser.setSignature(signature.getText().toString());
			newUser.setSex(sex.getText().toString());
			newUser.setAge(Integer.parseInt(age.getText().toString()));
			if (!Constants.getNoBlankString(email.getText().toString())
					.isEmpty()) {
				newUser.setEmail(email.getText().toString());
			} else {
				newUser.setEmail(null);
			}
			newUser.setWork(work.getText().toString());
			newUser.setInterest(interest.getText().toString());
			newUser.setSchool(school.getText().toString());
			newUser.setAddress(address.getText().toString());
			BmobUser currentUser = BmobUser.getCurrentUser();
			newUser.update(currentUser.getObjectId(), new UpdateListener() {

				// @Override
				// public void onSuccess() {
				// Toast.makeText(EditUserInfoActivity.this, "完成",
				// Toast.LENGTH_SHORT).show();
				// Intent intent = new Intent(
				// MainActivity.LOGIN_ACTION);
				// sendBroadcast(intent);
				// finish();
				//
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// if (arg0 == 301) {
				// Toast.makeText(EditUserInfoActivity.this,
				// "提交失败，邮箱不可用", Toast.LENGTH_LONG).show();
				// } else {
				// Toast.makeText(EditUserInfoActivity.this,
				// "提交失败" + arg0 + arg1, Toast.LENGTH_LONG)
				// .show();
				// }
				// // Log.e("提交失败", "错误码：" + arg0 + "内容：" + arg1);
				//
				// }

				@Override
				public void done(BmobException e) {
					if (e == null) {
						Toast.makeText(EditUserInfoActivity.this, "完成",
								Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(MainActivity.LOGIN_ACTION);
						sendBroadcast(intent);
						finish();
					} else {
						if (e.getErrorCode() == 301) {
							Toast.makeText(EditUserInfoActivity.this,
									"提交失败，邮箱不可用", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(EditUserInfoActivity.this,
									"提交失败" + e.getErrorCode() + e.getMessage(),
									Toast.LENGTH_LONG).show();
						}
					}
				}
			});
		}
	}

	private void changeToEditable() {
		editOrSubmit.setText("完成");
		nickName.setInputType(InputType.TYPE_CLASS_TEXT);
		signature.setInputType(InputType.TYPE_CLASS_TEXT);
		age.setInputType(InputType.TYPE_CLASS_NUMBER);
		email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		work.setInputType(InputType.TYPE_CLASS_TEXT);
		interest.setInputType(InputType.TYPE_CLASS_TEXT);
		school.setInputType(InputType.TYPE_CLASS_TEXT);
		address.setInputType(InputType.TYPE_CLASS_TEXT);
		sex.setClickable(true);
		sex.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (sex.getText().toString().equals("男")) {
					sex.setText("女");
				} else {
					sex.setText("男");
				}
			}
		});
	}
}
