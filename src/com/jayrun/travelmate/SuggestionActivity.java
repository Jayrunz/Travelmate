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
import cn.bmob.v3.listener.SaveListener;

import com.jayrun.beans.Suggestion;
import com.jayrun.utils.Constants;

public class SuggestionActivity extends Activity implements OnClickListener {
	private Button back;
	private EditText suggestionText;
	private Button submit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion);
		back = (Button) findViewById(R.id.suggestion_back);
		back.setOnClickListener(this);
		suggestionText = (EditText) findViewById(R.id.add_suggestion);
		submit = (Button) findViewById(R.id.submit_suggestion);
		submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.suggestion_back:
			finish();
			break;
		case R.id.submit_suggestion:
			if (Constants.removeBlankAtBegin(
					suggestionText.getText().toString()).isEmpty()) {
				Toast.makeText(SuggestionActivity.this, "不能提空的意见哦",
						Toast.LENGTH_SHORT).show();
			} else {
				BmobUser currentUser = BmobUser.getCurrentUser();
				Suggestion suggestion = new Suggestion();
				suggestion.setSuggestion(suggestionText.getText().toString());
				if (currentUser != null) {
					suggestion.setUserId(currentUser.getObjectId());
					suggestion.setUserPhoneNumber(currentUser
							.getMobilePhoneNumber());
					suggestion.setUserEmail(currentUser.getEmail());
				}
				suggestion.save(new SaveListener<String>() {

					@Override
					public void done(String arg0, BmobException e) {
						if (e == null) {
							Toast.makeText(SuggestionActivity.this,
									"您的意见已收到,感谢您的反馈！", Toast.LENGTH_LONG)
									.show();
							finish();
						} else {
							Toast.makeText(SuggestionActivity.this,
									"提交失败，请稍后重试" + e.getErrorCode(),
									Toast.LENGTH_LONG).show();
						}
					}
				});
			}
			break;
		default:
			break;
		}
	}

}
