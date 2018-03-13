package com.jayrun.travelmate;

import java.util.Timer;
import java.util.TimerTask;
import com.jayrun.travelmate.R;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgetPasswordActivity extends Activity implements OnClickListener {
	private final int STATE_SEND_BFORE = 0;
	private final int STATE_SEND_ING = 1;
	private int SEND_STATE;

	private Button back;
	private Button getSecurityCode;
	private Button submit;

	private EditText phoneNumber;
	private EditText password;
	private EditText securityCode;
	private Handler getsecurityCodeHandler;
	private Timer timer;
	private int nextSendTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forget_password);
		init();
	}

	private void init() {
		nextSendTime = 60;
		SEND_STATE = STATE_SEND_BFORE;
		back = (Button) findViewById(R.id.forget_psw_back);
		back.setOnClickListener(this);
		getSecurityCode = (Button) findViewById(R.id.forget_psw_get_security);
		getSecurityCode.setOnClickListener(this);
		submit = (Button) findViewById(R.id.change_password_ok);
		submit.setOnClickListener(this);
		phoneNumber = (EditText) findViewById(R.id.forget_psw_number);
		password = (EditText) findViewById(R.id.forget_psw_password);
		securityCode = (EditText) findViewById(R.id.forget_psw_security_code);
		phoneNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence phoneText, int arg1,
					int arg2, int count) {
				if (phoneText.length() == 11 && SEND_STATE == STATE_SEND_BFORE) {
					getSecurityCode.setEnabled(true);
				} else {
					getSecurityCode.setEnabled(false);
					submit.setEnabled(false);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable text) {

			}
		});
		securityCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence codeText, int arg1,
					int arg2, int arg3) {
				if (codeText.length() == 6
						&& phoneNumber.getText().length() == 11) {
					submit.setEnabled(true);
				} else {
					submit.setEnabled(false);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});
		getsecurityCodeHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x32) {
					if (nextSendTime > 0) {
						getSecurityCode.setText("获取(" + nextSendTime + ")");
						nextSendTime--;
					} else {
						getSecurityCode.setText("获取");
						SEND_STATE = STATE_SEND_BFORE;
						getSecurityCode.setEnabled(true);
						timer.cancel();
						timer = null;
						nextSendTime = 60;
					}

				}
			}
		};
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.forget_psw_back:
			this.finish();
			break;
		case R.id.change_password_ok:
			if (password.getText().length() < 6) {
				Toast.makeText(ForgetPasswordActivity.this, "请输不少于6位的密码",
						Toast.LENGTH_SHORT).show();
			} else if (securityCode.getText().length() < 6) {
				Toast.makeText(ForgetPasswordActivity.this, "请输6位数字验证码",
						Toast.LENGTH_SHORT).show();
			} else {
				submit.setEnabled(false);
				// 通过验证码更改密码
				BmobUser.resetPasswordBySMSCode(securityCode.getText()
						.toString(), password.getText().toString(),
						new UpdateListener() {
							@Override
							public void done(BmobException e) {
								if (e == null) {
									submit.setEnabled(true);
									setResult(4000);
									finish();
								} else {
									Toast.makeText(ForgetPasswordActivity.this,
											"密码修改失败" + e.getMessage(),
											Toast.LENGTH_LONG).show();
									submit.setEnabled(true);
								}

							}
						}
				);
			}
			break;
		case R.id.forget_psw_get_security:
			getSecurityCode.setEnabled(false);
			securityCode.setText("");
			SEND_STATE = STATE_SEND_ING;
			BmobSMS.requestSMSCode(phoneNumber.getText().toString(), "registerMS",
					new QueryListener<Integer>() {
						
						@Override
						public void done(Integer SmsId, BmobException exception) {
							if (exception == null) {
								Toast.makeText(ForgetPasswordActivity.this,
										"验证码已发送，请稍等", Toast.LENGTH_SHORT)
										.show();
								timer = new Timer();
								timer.schedule(new TimerTask() {

									@Override
									public void run() {
										getsecurityCodeHandler
												.sendEmptyMessage(0x32);
									}
								}, 0, 1000);
							} else {
								getSecurityCode.setEnabled(true);
								Toast.makeText(
										ForgetPasswordActivity.this,
										"获取验证码失败，请稍后重试"
												+ exception.getMessage(),
										Toast.LENGTH_SHORT).show();
							}
							
						}
					}
			);
			break;
		}
	}
}
