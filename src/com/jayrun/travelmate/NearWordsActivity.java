package com.jayrun.travelmate;

import com.jayrun.fragments.NearWordsFragment;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class NearWordsActivity extends Activity {
	private TextView back;
	private NearWordsFragment nearWordsFragment;
	private FragmentTransaction transaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_near_words);
		back = (TextView) findViewById(R.id.near_words_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		nearWordsFragment = new NearWordsFragment();
		transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.near_words_container, nearWordsFragment);
		transaction.commit();
	}

}
