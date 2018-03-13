package com.jayrun.widgets;

import android.app.Activity;

import com.jayrun.travelmate.R;
import com.slidingmenu.lib.SlidingMenu;

public class SlideMenuView {
	private SlidingMenu slidingMenu;
	private Activity myActivity;

	public SlideMenuView(Activity myActivity) {
		this.myActivity = myActivity;
	}

	public SlidingMenu newInstance() {
		slidingMenu = new SlidingMenu(myActivity);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.SLIDING_WINDOW);
		slidingMenu.setTouchModeBehind(SlidingMenu.SLIDING_WINDOW);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.attachToActivity(myActivity, SlidingMenu.LEFT);
		slidingMenu.setMenu(R.layout.menu_main);
		slidingMenu.setFadeDegree(0.5F);
		return slidingMenu;
	}
}
