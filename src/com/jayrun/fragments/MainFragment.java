package com.jayrun.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import com.jayrun.adapters.ScenicListAdapter;
import com.jayrun.beans.ScenicInfo;
import com.jayrun.travelmate.R;
import com.jayrun.travelmate.ScenicActivity;
import com.jayrun.widgets.AutoListView;
import com.jayrun.widgets.AutoListView.OnLoadListener;
import com.jayrun.widgets.AutoListView.OnRefreshListener;

public class MainFragment extends Fragment implements OnRefreshListener,
		OnLoadListener, OnItemClickListener {
	public final static String ACTION_SEARCH = "com.example.travelmate.search";
	private static final int REFRESH = 0;
	private static final int LOADMORE = 1;
	private static final int FIRSTLOAD = 2;
	public static boolean isFromSearch = false;
	private int SKIP = 0;
	private int pageSize = 20;
	private AutoListView scenicListView;
	private List<ScenicInfo> scenicInfos = new ArrayList<ScenicInfo>();
	private ScenicListAdapter scenicListAdapter;
	private String tag = "";
	private String keyWords;
	// private SearchReceiver searchReceiver;
	private View view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Log.e("zsj", "onCreate");
		SKIP = 0;
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_base, null);
		scenicListView = (AutoListView) view.findViewById(R.id.scenic_list);
		scenicListView.setPageSize(pageSize);
		init();
		// Log.e("zsj", "onCreateView");
		return view;
	}

	private void init() {
		// 注册搜索的广播监听
		tag = getArguments().getString("tag");
		keyWords = tag;
		// if (tag == "") {
		// IntentFilter filter = new IntentFilter(ACTION_SEARCH);
		// searchReceiver = new SearchReceiver();
		// getActivity().registerReceiver(searchReceiver, filter);
		// }
		scenicListAdapter = new ScenicListAdapter(getActivity());
		scenicListView.setAdapter(scenicListAdapter);
		scenicListView.setOnItemClickListener(this);
		scenicListView.setOnRefreshListener(this);
		scenicListView.setOnLoadListener(this);
		loadData(REFRESH, keyWords);
	}

	// 加载数据
	private void loadData(final int TYPE, String loadKeywords) {
		BmobQuery<ScenicInfo> query = new BmobQuery<ScenicInfo>();
		// if (!"".equals(loadKeywords)) {
		// if (isFromSearch) {
		// BmobQuery<ScenicInfo> condition1 = new BmobQuery<ScenicInfo>();
		// condition1.addWhereContains("name", loadKeywords);
		// // BmobQuery<ScenicInfo> condition2 = new
		// // BmobQuery<ScenicInfo>();
		// // condition2.addWhereContains("describtion", loadKeywords);
		// BmobQuery<ScenicInfo> condition2 = new BmobQuery<ScenicInfo>();
		// condition2.addWhereContains("province", loadKeywords);
		// BmobQuery<ScenicInfo> condition3 = new BmobQuery<ScenicInfo>();
		// condition3.addWhereContains("city", loadKeywords);
		// List<BmobQuery<ScenicInfo>> queries = new
		// ArrayList<BmobQuery<ScenicInfo>>();
		// queries.add(condition1);
		// queries.add(condition2);
		// queries.add(condition3);
		// query.or(queries);
		// } else {
		query.addWhereMatches("label", loadKeywords);
		// }
		//
		// } else {
		// query.addWhereNotEqualTo("label", "大学");
		// }
		query.order("-weight");
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.setLimit(pageSize);
		query.setSkip(SKIP);
		query.findObjects(new FindListener<ScenicInfo>() {

			@Override
			public void done(List<ScenicInfo> results, BmobException e) {

				if (e == null) {
					onSuccess(results);
				} else {
					onError(e.getErrorCode(), e.getMessage());
				}
			}

			public void onSuccess(List<ScenicInfo> results) {
				switch (TYPE) {
				case REFRESH:
					if (results.size() == 0) {
						scenicListView.onRefreshComplete();
						scenicListAdapter.getScenicinfos().clear();
						scenicListView.setResultSize(-1);
					} else {
						scenicListView.onRefreshComplete();
						scenicListAdapter.getScenicinfos().clear();
						scenicListAdapter.getScenicinfos().addAll(results);
						scenicListView.setResultSize(results.size());
					}
					break;
				case LOADMORE:
					scenicListView.onLoadComplete();
					scenicListAdapter.getScenicinfos().addAll(results);
					scenicListView.setResultSize(results.size());
					break;
				case FIRSTLOAD:
					scenicListAdapter.getScenicinfos().clear();
					scenicListAdapter.getScenicinfos().addAll(results);
					scenicListView.setResultSize(results.size());
					break;
				}
				scenicListAdapter.notifyDataSetChanged();
				scenicInfos = scenicListAdapter.getScenicinfos();
			}

			public void onError(int arg0, String arg1) {
				if (arg0 != 9015 && arg0 != 9009) {
					Toast.makeText(getActivity(), "加载失败，请检查网络",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	// private class SearchReceiver extends BroadcastReceiver {
	//
	// @Override
	// public void onReceive(Context arg0, Intent searchIntent) {
	// String searchKeyWords = searchIntent.getStringExtra("keyword");
	// isFromSearch = true;
	// SKIP = 0;
	// keyWords = searchKeyWords;
	// loadData(REFRESH, keyWords);
	// }
	//
	// }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Intent intent2 = new Intent(
				"com.jayrun.services.UpdateReadCountService");
		intent2.putExtra("scenicId", scenicInfos.get(position - 1)
				.getObjectId());
		getActivity().startService(intent2);

		Intent intent = new Intent(getActivity(), ScenicActivity.class);
		intent.putExtra("scenicId", scenicInfos.get(position - 1).getObjectId());
		intent.putExtra("scenicName", scenicInfos.get(position - 1).getName());
		intent.putExtra("city", scenicInfos.get(position - 1).getCity());
		startActivity(intent);
	}

	@Override
	public void onLoad() {
		SKIP = SKIP + pageSize;
		loadData(LOADMORE, keyWords);
	}

	@Override
	public void onRefresh() {
		keyWords = tag;
		isFromSearch = false;
		SKIP = 0;
		loadData(REFRESH, keyWords);
	}

	@Override
	public void onDestroy() {
		// Log.e("zsj", tag + "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		// Log.e("zsj", tag + "onLowMemory");
		super.onLowMemory();
	}

	@Override
	public void onResume() {
		// Log.e("zsj", tag + "onResume");
		super.onResume();
	}

	@Override
	public void onStart() {
		// Log.e("zsj", tag + "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		// Log.e("zsj", tag + "onStop");
		super.onStop();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// Log.e("zsj", tag + "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

}
