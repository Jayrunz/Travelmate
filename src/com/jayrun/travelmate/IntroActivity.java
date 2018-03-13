package com.jayrun.travelmate;

import java.util.ArrayList;
import java.util.List;
import com.jayrun.widgets.CirclePageIndicator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class IntroActivity extends FragmentActivity {

	private ViewPager pager_splash_ad;
	private CirclePageIndicator indicator;
	private List<Fragment> fragmentsIn;

	int intros[] = { R.drawable.main_intro, R.drawable.words_button_intro,
			R.drawable.leave_words_intro, R.drawable.graffiti_intro,
			R.drawable.words_bubble, R.drawable.wordsdetail_intro,
			R.drawable.strategy_intro, R.drawable.way_button_intro,
			R.drawable.near_intro };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		pager_splash_ad = (ViewPager) findViewById(R.id.pager_splash_ad);
		fragmentsIn = new ArrayList<Fragment>();
		for (int i = 0; i < intros.length; i++) {
			IntroFragment fragment = new IntroFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("ID", intros[i]);
			fragment.setArguments(bundle);
			fragmentsIn.add(fragment);

		}
		IntroImgPagerAfapter imgPagerAfapter = new IntroImgPagerAfapter(
				getSupportFragmentManager(), fragmentsIn);
		pager_splash_ad.setAdapter(imgPagerAfapter);
		indicator = (CirclePageIndicator) findViewById(R.id.viewflowindic);
		indicator.setViewPager(pager_splash_ad);
	}

	public void finishThis(View view) {
		finish();
	}

	public class IntroImgPagerAfapter extends FragmentPagerAdapter {
		private List<Fragment> fragments;

		public IntroImgPagerAfapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

	}

	private class IntroFragment extends Fragment {
		int id;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			id = getArguments().getInt("ID");
			View view = inflater.inflate(R.layout.view_intro, null);
			ImageView iv_ad = (ImageView) view.findViewById(R.id.iv_ad);
			iv_ad.setImageResource(id);
			return view;
		}
	}

}
