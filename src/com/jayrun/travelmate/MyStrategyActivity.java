package com.jayrun.travelmate;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.bmob.v3.BmobUser;

import com.jayrun.fragments.StrategyFragment;

public class MyStrategyActivity extends Activity {
	private TextView back;
	private StrategyFragment strategyFragment;
	private Bundle arguement;
	private FragmentTransaction transaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_strategy);
		back = (TextView) findViewById(R.id.strategy_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		arguement = new Bundle();
		BmobUser user = BmobUser.getCurrentUser();
		arguement.putString("userId", user.getObjectId());
		strategyFragment = new StrategyFragment(true);
		strategyFragment.setArguments(arguement);
		transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.my_strategy_container, strategyFragment);
		transaction.commit();

	}

}
