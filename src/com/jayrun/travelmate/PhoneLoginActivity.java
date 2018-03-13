package com.jayrun.travelmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import com.jayrun.beans.User;
import com.jayrun.utils.Constants;

public class PhoneLoginActivity extends Activity implements OnClickListener {
	private Button back;
	private Button forgetPsw;
	private Button login;
	private EditText phoneNumber;
	private EditText passWord;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_login);
		init();
	}

	private void init() {
		phoneNumber = (EditText) findViewById(R.id.phone_login_number);
		passWord = (EditText) findViewById(R.id.phone_login_pwd);
		forgetPsw = (Button) findViewById(R.id.btn_forget_psw);
		forgetPsw.setOnClickListener(this);
		back = (Button) findViewById(R.id.phone_login_back);
		back.setOnClickListener(this);
		login = (Button) findViewById(R.id.phone_login_ok);
		login.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 4000 && resultCode == 4000) {
			Toast.makeText(PhoneLoginActivity.this, "修改密码成功，请重新登录",
					Toast.LENGTH_LONG).show();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_forget_psw:
			Intent intent = new Intent(PhoneLoginActivity.this,
					ForgetPasswordActivity.class);
			startActivityForResult(intent, 4000);
			break;
		case R.id.phone_login_back:
			this.finish();
			break;
		case R.id.phone_login_ok:
			if (Constants.removeBlankAtBegin(phoneNumber.getText().toString())
					.isEmpty()) {
				Toast.makeText(getApplicationContext(), "手机号不能为空哦",
						Toast.LENGTH_SHORT).show();
				phoneNumber.setText("");
			} else if (phoneNumber.getText().toString().isEmpty()) {
				Toast.makeText(getApplicationContext(), "密码不能为空哦",
						Toast.LENGTH_SHORT).show();
			} else {
				login.setEnabled(false);
				User user = new User();
				user.setUsername(phoneNumber.getText().toString());
				user.setPassword(passWord.getText().toString());
				user.login(new SaveListener<User>() {

					@Override
					public void done(User user, BmobException e) {
						if (e==null) {
							Toast.makeText(getApplicationContext(), "登录成功",
									Toast.LENGTH_SHORT).show();
							login.setEnabled(true);
							Intent intent = new Intent(MainActivity.LOGIN_ACTION);
							sendBroadcast(intent);
							setResult(2000);
							PhoneLoginActivity.this.finish();
						}else {
							if (e.getErrorCode() == 101) {
								Toast.makeText(getApplicationContext(),
										"用户名或密码不正确", Toast.LENGTH_SHORT).show();
								passWord.setText("");
							} else {
								Toast.makeText(getApplicationContext(),
										"登录失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
							Log.e("onFailure", "错误码：" + e.getErrorCode() + "错误内容：" + e.getMessage());

							login.setEnabled(true);
						}

						
					}
					
				}
				// new SaveListener() {
				// @Override
				// public void onSuccess() {
				// Toast.makeText(getApplicationContext(), "登录成功",
				// Toast.LENGTH_SHORT).show();
				// login.setEnabled(true);
				// Intent intent = new Intent(MainActivity.LOGIN_ACTION);
				// sendBroadcast(intent);
				// setResult(2000);
				// PhoneLoginActivity.this.finish();
				//
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// if (arg0 == 101) {
				// Toast.makeText(getApplicationContext(),
				// "用户名或密码不正确", Toast.LENGTH_SHORT).show();
				// passWord.setText("");
				// } else {
				// Toast.makeText(getApplicationContext(),
				// "登录失败" + arg1, Toast.LENGTH_SHORT).show();
				// }
				// Log.e("onFailure", "错误码：" + arg0 + "错误内容：" + arg1);
				//
				// login.setEnabled(true);
				// }
				// }
					);
			}

			break;
		}

	}
}
