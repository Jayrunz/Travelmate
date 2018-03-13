package com.jayrun.travelmate;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.jayrun.beans.User;
import com.jayrun.utils.Constants;

public class RegisterActivity extends Activity implements OnClickListener {
	private final int STATE_SEND_BFORE = 0;
	private final int STATE_SEND_ING = 1;
	private int SEND_STATE;
	private Button back;
	private Button submit;
	private Button getsecurityCode;
	private EditText phoneNumber;
	private EditText nickName;
	private EditText passWord;

	private EditText securityCode;
	private Handler getsecurityCodeHandler;
	private Timer timer;
	private int nextSendTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		init();
	}

	private void init() {
		nextSendTime = 60;
		SEND_STATE = STATE_SEND_BFORE;
		back = (Button) findViewById(R.id.register_back);
		back.setOnClickListener(this);
		submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(this);
		getsecurityCode = (Button) findViewById(R.id.get_security);
		getsecurityCode.setOnClickListener(this);
		phoneNumber = (EditText) findViewById(R.id.reg_phone_number);
		nickName = (EditText) findViewById(R.id.reg_nickName);
		passWord = (EditText) findViewById(R.id.reg_password);
		securityCode = (EditText) findViewById(R.id.reg_security_code);
		phoneNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence phoneText, int arg1,
					int arg2, int count) {
				if (phoneText.length() == 11 && SEND_STATE == STATE_SEND_BFORE) {
					getsecurityCode.setEnabled(true);
				} else {
					getsecurityCode.setEnabled(false);
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
						getsecurityCode.setText("获取(" + nextSendTime + ")");
						nextSendTime--;
					} else {
						getsecurityCode.setText("获取");
						SEND_STATE = STATE_SEND_BFORE;
						getsecurityCode.setEnabled(true);
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
		case R.id.register_back:
			this.finish();
			break;
		case R.id.get_security:
			securityCode.setText("");
			getsecurityCode.setEnabled(false);
			SEND_STATE = STATE_SEND_ING;
			BmobSMS.requestSMSCode(phoneNumber.getText().toString(),
					"registerMS", new QueryListener<Integer>() {

						@Override
						public void done(Integer SmsId, BmobException exception) {

							if (exception == null) {
								Toast.makeText(RegisterActivity.this,
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
								getsecurityCode.setEnabled(true);
								Toast.makeText(
										RegisterActivity.this,
										"获取验证码失败，请稍后重试"
												+ exception.getMessage(),
										Toast.LENGTH_SHORT).show();
							}

						}
					}
			// new RequestSMSCodeListener() {
			// @Override
			// public void done(Integer SmsId, BmobException exception) {
			// if (exception == null) {
			// Toast.makeText(RegisterActivity.this, "验证码已发送，请稍等",
			// Toast.LENGTH_SHORT).show();
			// timer = new Timer();
			// timer.schedule(new TimerTask() {
			//
			// @Override
			// public void run() {
			// getsecurityCodeHandler.sendEmptyMessage(0x32);
			// }
			// }, 0, 1000);
			// } else {
			// getsecurityCode.setEnabled(true);
			// Toast.makeText(RegisterActivity.this,
			// "获取验证码失败，请稍后重试" + exception.getMessage(),
			// Toast.LENGTH_SHORT).show();
			// }
			// }
			// }

			);
			break;
		case R.id.submit:
			if (Constants.removeBlankAtBegin(nickName.getText().toString())
					.isEmpty()) {
				nickName.setText("");
				Toast.makeText(RegisterActivity.this, "昵称不能为空哦",
						Toast.LENGTH_SHORT).show();
			} else if (passWord.getText().length() < 6) {
				Toast.makeText(RegisterActivity.this, "请输不少于6位的密码",
						Toast.LENGTH_SHORT).show();
			}
			// else {
			// // 进行邮箱验证
			//
			// File file = new File(Environment.getExternalStorageDirectory()
			// .toString() + "/travelmate/img");
			// if (!file.exists()) {
			// file.mkdirs();
			// }
			// File file2 = new File(file, "defaulthead.jpg");
			// try {
			// assetsDataToSD(file2.toString());
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// String path[] = { file2.toString() };
			// Bmob.uploadBatch(RegisterActivity.this, path,
			// new UploadBatchListener() {
			//
			// @Override
			// public void onSuccess(List<BmobFile> files,
			// List<String> arg1) {
			// User user = new User();
			// user.setNickName(nickName.getText().toString());
			// user.setPassword(passWord.getText().toString());
			// user.setUsername(email.getText().toString());
			// user.setEmail(email.getText().toString());
			// user.setUserHead(files.get(0));
			// user.signUp(RegisterActivity.this,
			// new SaveListener() {
			//
			// @Override
			// public void onSuccess() {
			// Toast.makeText(
			// RegisterActivity.this,
			// "注册成功!您将收到一封邮件，验证后即可登录",
			// Toast.LENGTH_LONG)
			// .show();
			// setResult(2001);
			// RegisterActivity.this.finish();
			// }
			//
			// @Override
			// public void onFailure(int arg0,
			// String arg1) {
			// Log.e("===注册失败signUp===",
			// "注册失败" + "错误码：" + arg0
			// + "错误内容" + arg1);
			// Toast.makeText(
			// RegisterActivity.this,
			// "注册失败" + "错误码：" + arg0
			// + "错误内容" + arg1,
			// Toast.LENGTH_LONG)
			// .show();
			// if (arg0 == 301) {
			// Toast.makeText(
			// RegisterActivity.this,
			// "注册失败,您的邮箱",
			// Toast.LENGTH_LONG)
			// .show();
			// }
			// }
			// });
			//
			// }
			//
			// @Override
			// public void onProgress(int arg0, int arg1,
			// int arg2, int arg3) {
			//
			// }
			//
			// @Override
			// public void onError(int arg0, String arg1) {
			// Toast.makeText(RegisterActivity.this,
			// "注册失败" + arg1, Toast.LENGTH_LONG)
			// .show();
			//
			// }
			// });
			// }
			else if (securityCode.getText().length() < 6) {
				Toast.makeText(RegisterActivity.this, "请输6位数字验证码",
						Toast.LENGTH_SHORT).show();
			} else {
				// 验证验证码正确性
				submit.setEnabled(false);
				BmobSMS.verifySmsCode(phoneNumber.getText().toString(),
						securityCode.getText().toString(),
						new UpdateListener() {

							@Override
							public void done(BmobException exception) {
								// 验证码正确
								if (exception == null) {
									// File file = new File(Environment
									// .getExternalStorageDirectory().toString()
									// + "/travelmate/img");
									// if (!file.exists()) {
									// file.mkdirs();
									// }
									// File file2 = new File(file,
									// "defaulthead.jpg");
									// try {
									// assetsDataToSD(file2.toString());
									// } catch (IOException e) {
									// e.printStackTrace();
									// }
									User user = new User();
									user.setUsername(phoneNumber.getText()
											.toString());
									user.setPassword(passWord.getText()
											.toString());
									user.setMobilePhoneNumber(phoneNumber
											.getText().toString());
									user.setNickName(nickName.getText()
											.toString());
									user.setMobilePhoneNumberVerified(true);
									BmobFile headFile = new BmobFile(
											"defaultHead", "default",
											Constants.DEFAULT_HEAD_URL);
									user.setUserHead(headFile);
									user.signUp(new SaveListener<User>() {
										public void done(User user,
												BmobException e) {
											if (e == null) {
												// 注册后本地会保存用户信息
												submit.setEnabled(true);
												if (user != null) {
													RegisterActivity.this
															.setResult(2000);
													RegisterActivity.this
															.finish();
													Intent intent = new Intent(
															MainActivity.LOGIN_ACTION);
													sendBroadcast(intent);
													// Log.e("===signUpSuccess==",
													// user.getMobilePhoneNumber()
													// + user.getUsername());

												} else {
													RegisterActivity.this
															.setResult(2001);
													RegisterActivity.this
															.finish();
													// Log.e("==signUpSuccess==",
													// "没有用户信息");
												}
											} else {

												submit.setEnabled(true);
												if (e.getErrorCode() == 202) {
													Toast.makeText(
															RegisterActivity.this,
															"你的手机号已经被使用，不可重复注册",
															Toast.LENGTH_LONG)
															.show();
												} else {
													Toast.makeText(
															RegisterActivity.this,
															"注册失败，请稍后重试"
																	+ e.getMessage(),
															Toast.LENGTH_LONG)
															.show();
												}
												// Log.e("注册失败", "codeNumber" +
												// arg0
												// + "错误内容：" + arg1);

											}

										};
									});

								} else {
									// 验证码不正确
									submit.setEnabled(true);
									Toast.makeText(RegisterActivity.this,
											"验证码不正确", Toast.LENGTH_LONG).show();
								}

							}
						}

				// new VerifySMSCodeListener() {
				// @Override
				// public void done(BmobException exception) {
				// // 验证码正确
				// if (exception == null) {
				// // File file = new File(Environment
				// // .getExternalStorageDirectory().toString()
				// // + "/travelmate/img");
				// // if (!file.exists()) {
				// // file.mkdirs();
				// // }
				// // File file2 = new File(file, "defaulthead.jpg");
				// // try {
				// // assetsDataToSD(file2.toString());
				// // } catch (IOException e) {
				// // e.printStackTrace();
				// // }
				// User user = new User();
				// user.setUsername(phoneNumber.getText().toString());
				// user.setPassword(passWord.getText().toString());
				// user.setMobilePhoneNumber(phoneNumber.getText()
				// .toString());
				// user.setNickName(nickName.getText().toString());
				// user.setMobilePhoneNumberVerified(true);
				// BmobFile headFile = new BmobFile("defaultHead",
				// "default", Constants.DEFAULT_HEAD_URL);
				// user.setUserHead(headFile);
				// user.signUp(RegisterActivity.this,
				// new SaveListener() {
				//
				// @Override
				// public void onSuccess() {
				// // 注册后本地会保存用户信息
				// submit.setEnabled(true);
				// BmobUser user = BmobUser
				// .getCurrentUser(RegisterActivity.this);
				// if (user != null) {
				// RegisterActivity.this
				// .setResult(2000);
				// RegisterActivity.this.finish();
				// Intent intent = new Intent(
				// MainActivity.LOGIN_ACTION);
				// sendBroadcast(intent);
				// // Log.e("===signUpSuccess==",
				// // user.getMobilePhoneNumber()
				// // + user.getUsername());
				//
				// } else {
				// RegisterActivity.this
				// .setResult(2001);
				// RegisterActivity.this.finish();
				// // Log.e("==signUpSuccess==",
				// // "没有用户信息");
				// }
				// }
				//
				// @Override
				// public void onFailure(int arg0,
				// String arg1) {
				// submit.setEnabled(true);
				// if (arg0 == 202) {
				// Toast.makeText(
				// RegisterActivity.this,
				// "你的手机号已经被使用，不可重复注册",
				// Toast.LENGTH_LONG)
				// .show();
				// } else {
				// Toast.makeText(
				// RegisterActivity.this,
				// "注册失败，请稍后重试" + arg1,
				// Toast.LENGTH_LONG)
				// .show();
				// }
				// // Log.e("注册失败", "codeNumber" + arg0
				// // + "错误内容：" + arg1);
				//
				// }
				// });
				//
				// } else {
				// // 验证码不正确
				// submit.setEnabled(true);
				// Toast.makeText(RegisterActivity.this, "验证码不正确",
				// Toast.LENGTH_LONG).show();
				// }
				// }
				// }
				);
			}
			break;
		default:
			break;
		}
	}

	// private void assetsDataToSD(String fileName) throws IOException {
	// InputStream myInput;
	// OutputStream myOutput = new FileOutputStream(fileName);
	// myInput = this.getAssets().open("default_head_donkey.jpg");
	// byte[] buffer = new byte[1024];
	// int length = myInput.read(buffer);
	// while (length > 0) {
	// myOutput.write(buffer, 0, length);
	// length = myInput.read(buffer);
	// }
	//
	// myOutput.flush();
	// myInput.close();
	// myOutput.close();
	// }
}
