package com.jayrun.travelmate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class ChangePwdActivity extends Activity implements OnClickListener {
	private Button back;
	private Button submit;
	private EditText oldPwd;
	private EditText newPwd1;
	private EditText newPwd2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pwd);
		init();
	}

	private void init() {
		back = (Button) findViewById(R.id.change_pwd_back);
		back.setOnClickListener(this);
		submit = (Button) findViewById(R.id.change_password_ok);
		submit.setOnClickListener(this);
		oldPwd = (EditText) findViewById(R.id.change_pwd_old);
		newPwd1 = (EditText) findViewById(R.id.change_pwd_new1);
		newPwd2 = (EditText) findViewById(R.id.change_pwd_new2);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.change_pwd_back:
			finish();
			break;
		case R.id.change_password_ok:
			if (oldPwd.getText().toString().isEmpty()) {
				oldPwd.setText("");
				Toast.makeText(ChangePwdActivity.this, "原密码不能为空！",
						Toast.LENGTH_SHORT).show();
			} else if (newPwd1.getText().toString().isEmpty()) {
				newPwd1.setText("");
				Toast.makeText(ChangePwdActivity.this, "新密码不能为空！",
						Toast.LENGTH_SHORT).show();
			} else if (newPwd1.getText().toString().length() < 6) {
				Toast.makeText(ChangePwdActivity.this, "新密码不能少于6位！",
						Toast.LENGTH_SHORT).show();

			} else if (!newPwd1.getText().toString()
					.equals(newPwd2.getText().toString())) {
				newPwd2.setText("");
				Toast.makeText(ChangePwdActivity.this, "两次输入的密码不一致！",
						Toast.LENGTH_SHORT).show();
			} else {
				BmobUser.updateCurrentUserPassword(oldPwd.getText().toString(),
						newPwd1.getText().toString(), new UpdateListener() {
							//
							// public void onSuccess() {
							// Toast.makeText(ChangePwdActivity.this,
							// "密码修改成功", Toast.LENGTH_LONG).show();
							// finish();
							// }
							//
							// public void onFailure(int arg0, String arg1) {
							// if (arg0 == 210) {
							// Toast.makeText(ChangePwdActivity.this,
							// "原密码不正确", Toast.LENGTH_LONG).show();
							// } else {
							// Toast.makeText(ChangePwdActivity.this,
							// "密码修改失败" + arg1, Toast.LENGTH_LONG)
							// .show();
							// }
							// // Log.e("===密码修改失败===", "错误码：" + arg0 + "错误内容："
							// // + arg1);
							// }

							@Override
							public void done(BmobException e) {
								if (e == null) {
									Toast.makeText(ChangePwdActivity.this,
											"密码修改成功", Toast.LENGTH_LONG).show();
									finish();
								} else {
									if (e.getErrorCode() == 210) {
										Toast.makeText(ChangePwdActivity.this,
												"原密码不正确", Toast.LENGTH_LONG)
												.show();
									} else {
										Toast.makeText(ChangePwdActivity.this,
												"密码修改失败" + e.getErrorCode(),
												Toast.LENGTH_LONG).show();
									}
								}

							}
						});
			}
			break;
		}
	}

}
