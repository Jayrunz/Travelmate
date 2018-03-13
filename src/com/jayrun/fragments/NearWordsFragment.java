package com.jayrun.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.jayrun.adapters.WordsDetailsListAdapter;
import com.jayrun.adapters.WordsDetailsListAdapter.LikeButtonCallBack;
import com.jayrun.beans.User;
import com.jayrun.beans.Words;
import com.jayrun.travelmate.PainterActivity;
import com.jayrun.travelmate.R;
import com.jayrun.utils.Constants;
import com.jayrun.widgets.ArcMenu;
import com.jayrun.widgets.ArcMenu.OnMenuItemClickListener;
import com.jayrun.widgets.AutoListView;
import com.jayrun.widgets.AutoListView.OnLoadListener;
import com.jayrun.widgets.AutoListView.OnRefreshListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class NearWordsFragment extends Fragment implements LocationSource,
		AMapLocationListener, OnRefreshListener, OnLoadListener,
		OnItemClickListener, LikeButtonCallBack {
	public static final String DELETE_ACTION = "com.example.travelmate.deleteword";
	private static final int REFRESH = 0;
	private static final int LOADMORE = 1;
	private static int skip = 0;
	private static int pageSize = 10;

	private boolean isFirstLoad = true;
	private String locationInfo = "";

	private int wordsCount;
	private int wordsFlag = 0;
	private int markFlag;
	private int markCount;
	private Handler getWordsHandler;

	private AMap aMap;
	private MapView mapView;
	private LinearLayout mapLoadingPro;
	private ArcMenu showArcMenu;

	private UiSettings mapSettings;
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationClientOption = null;
	private OnLocationChangedListener locationChangedListener;

	private LatLng myLatLng;

	private Dialog wordsDetailDialog;
	private AutoListView wordsListView;
	private WordsDetailsListAdapter wordsAdapter;
	private List<Words> wordsList = new ArrayList<Words>();
	private List<Words> markList = new ArrayList<Words>();
	private int bubbleColor = 0;
	private MarkDeleteReceiver markDeleteReceiver;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_leave_words, null);
		mapView = (MapView) view.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		showArcMenu = (ArcMenu) view.findViewById(R.id.show_arc_menu);
		showArcMenu.setOnMenuItemClickListener(menuItemClickListener);
		mapLoadingPro = (LinearLayout) view.findViewById(R.id.map_loading_lin);
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheOnDisc(true)
				.cacheInMemory(true).displayer(new RoundedBitmapDisplayer(0))
				.build();
		init();
		// addExistingMark();
		return view;
	}

	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			mapSettings = aMap.getUiSettings();
		}
		mapSettings.setZoomControlsEnabled(false);
		locationClient = new AMapLocationClient(getActivity());
		locationClientOption = new AMapLocationClientOption();
		locationClientOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		locationClientOption.setOnceLocation(true);
		locationClient.setLocationOption(locationClientOption);
		locationClient.setLocationListener(this);
		locationClient.startLocation();
		// 注册删除mark的广播接收
		markDeleteReceiver = new MarkDeleteReceiver();
		IntentFilter filter = new IntentFilter(DELETE_ACTION);
		getActivity().registerReceiver(markDeleteReceiver, filter);
		aMap.setLocationSource(this);
		aMap.setMyLocationEnabled(true);
		mapSettings.setMyLocationButtonEnabled(true);
		mapLoadingPro.setVisibility(View.VISIBLE);

	}

	// 卫星菜单点击事件
	private OnMenuItemClickListener menuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public void onItemClick(View view, int position) {
			switch (position) {
			case 0:
				BmobUser user = BmobUser.getCurrentUser();
				if (user == null) {
					Toast.makeText(getActivity(), "登录后才能留言哦",
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(getActivity(),
							PainterActivity.class);
					intent.putExtra("scenicId", "");
					startActivity(intent);
				}
				break;
			case 1:
				showAddWordDialog();
				break;
			case 2:
				showWordDetailDialog();
				break;
			case 3:
				aMap.clear();
				addExistingMark();
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
		// Log.e("onResume", "���¿�ʼ");
	}

	@Override
	public void onStop() {
		// Log.e("onStop", "ֹͣ");
		super.onStop();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();

	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		if (locationChangedListener != null && aMapLocation != null) {
			if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
				locationInfo = aMapLocation.getCity()
						+ aMapLocation.getDistrict();
				locationChangedListener.onLocationChanged(aMapLocation);
				myLatLng = new LatLng(aMapLocation.getLatitude(),
						aMapLocation.getLongitude());
				locationChangedListener.onLocationChanged(aMapLocation);
				if (isFirstLoad) {
					addExistingMark();
					isFirstLoad = false;
					mapLoadingPro.setVisibility(View.GONE);
					aMap.moveCamera(CameraUpdateFactory
							.newCameraPosition(new CameraPosition(myLatLng, 16,
									0, 0)));
				}
			} else {
				Toast.makeText(getActivity(), "获取位置失败，请稍后重试", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	@Override
	public void activate(OnLocationChangedListener changedListener) {
		locationChangedListener = changedListener;
		if (locationClient == null) {
			locationClient = new AMapLocationClient(getActivity());
			if (locationClientOption == null) {
				locationClientOption = new AMapLocationClientOption();
			}
			locationClient.setLocationListener(this);
			locationClientOption
					.setLocationMode(AMapLocationMode.Hight_Accuracy);
			locationClientOption.setOnceLocation(true);
			locationClient.setLocationOption(locationClientOption);
			locationClient.startLocation();
		}

	}

	@Override
	public void deactivate() {
		locationChangedListener = null;
		if (locationClient != null) {
			locationClient.stopLocation();
			locationClient.onDestroy();
		}
		locationClient = null;
	}

	private void showAddWordDialog() {
		try {
			BmobUser user = BmobUser.getCurrentUser();
			if (user == null) {
				Toast.makeText(getActivity(), "登录后才能留言哦", Toast.LENGTH_SHORT)
						.show();
			} else {
				aMap.setLocationSource(this);
				mapSettings.setMyLocationButtonEnabled(true);
				aMap.setMyLocationEnabled(true);
				locationClient.startLocation();
				LayoutInflater inflater = getActivity().getLayoutInflater();
				RelativeLayout layout = (RelativeLayout) inflater.inflate(
						R.layout.dialog_add_words, null);
				final EditText words = (EditText) layout
						.findViewById(R.id.words);
				Button dialogOk = (Button) layout.findViewById(R.id.dia_ok);
				Button dialogCancle = (Button) layout
						.findViewById(R.id.dia_cancle);
				RadioGroup bubbleGroup = (RadioGroup) layout
						.findViewById(R.id.bubble_color_group);
				bubbleGroup.setOnCheckedChangeListener(checkedChangeListener);
				final Dialog addWordDialog = new Dialog(getActivity());
				addWordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				addWordDialog.getWindow().setBackgroundDrawableResource(
						R.drawable.dialog_bg_alpha0);
				addWordDialog.setCanceledOnTouchOutside(false);
				addWordDialog.show();
				addWordDialog.getWindow().setContentView(layout);
				dialogOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						String string = Constants.removeBlankAtBegin(words
								.getText().toString());
						if (string.isEmpty()) {
							Toast.makeText(getActivity(), "留言不能为空",
									Toast.LENGTH_SHORT).show();
							words.setText("");
						} else {
							User user = BmobUser.getCurrentUser(User.class);
							if (user != null) {
								addWordsToDataBase(user, words.getText()
										.toString(), myLatLng);
								addWordDialog.dismiss();
								mapSettings.setMyLocationButtonEnabled(false);
								aMap.setMyLocationEnabled(false);
							}

						}
					}

				});
				dialogCancle.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						addWordDialog.dismiss();
						mapSettings.setMyLocationButtonEnabled(false);
						aMap.setMyLocationEnabled(false);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showWordDetailDialog() {
		skip = 0;
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.dialog_words_details, null);
		wordsListView = (AutoListView) layout
				.findViewById(R.id.words_details_list);
		ImageView close = (ImageView) layout.findViewById(R.id.details_close);
		wordsAdapter = new WordsDetailsListAdapter(getActivity(), this);
		wordsListView.setAdapter(wordsAdapter);
		wordsListView.setOnLoadListener(this);
		wordsListView.setOnRefreshListener(this);
		wordsListView.setOnItemClickListener(this);
		loadwords(REFRESH);
		wordsDetailDialog = new Dialog(getActivity());
		wordsDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		wordsDetailDialog.show();
		wordsDetailDialog.setCanceledOnTouchOutside(false);
		wordsDetailDialog.getWindow().setContentView(layout);
		wordsDetailDialog.getWindow().setBackgroundDrawableResource(
				R.drawable.dialog_bg_alpha0);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				wordsDetailDialog.dismiss();
			}
		});
	}

	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup arg0, int id) {
			switch (id) {
			case R.id.bubble_yellow:
				bubbleColor = 0;
				break;
			case R.id.bubble_green:
				bubbleColor = 1;
				break;
			case R.id.bubble_purple:
				bubbleColor = 2;
				break;
			case R.id.bubble_blue:
				bubbleColor = 3;
				break;
			case R.id.bubble_black:
				bubbleColor = 4;
				break;
			}
		}
	};

	// 把mark添加到地图
	public void addMarkerToMap(Words words, final boolean isSingel) {
		LatLng position = new LatLng(words.getLocation().getLatitude(), words
				.getLocation().getLongitude());
		final MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(position);
		markerOptions.draggable(true);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View view = inflater.inflate(R.layout.layout_bubble_view, null);
		int id = R.drawable.bubble_yellow_alpha;
		switch (words.getBubbleColor()) {
		case 0:
			id = R.drawable.bubble_yellow_alpha;
			break;
		case 1:
			id = R.drawable.bubble_green_alpha;
			break;
		case 2:
			id = R.drawable.bubble_purple_alpha;
			break;
		case 3:
			id = R.drawable.bubble_blue_alpha;
			break;
		case 4:
			id = R.drawable.bubble_black_alpha;
			break;
		case 5:
			id = R.drawable.bubble_white_alpha;
			break;
		default:
			break;
		}
		TextView bubbleContent = (TextView) view
				.findViewById(R.id.bubble_content);
		final ImageView bubbleGraffiti = (ImageView) view
				.findViewById(R.id.bubble_graffiti);
		ImageView bubbleBg = (ImageView) view.findViewById(R.id.bubble_bg);
		bubbleBg.setImageResource(id);
		if (words.getIsText()) {
			bubbleContent.setVisibility(View.VISIBLE);
			bubbleGraffiti.setVisibility(View.GONE);
			bubbleContent.setText(words.getContent());
			markerOptions.icon(BitmapDescriptorFactory.fromView(view));
			aMap.addMarker(markerOptions);
			if (!isSingel) {
				addMarkHandler.sendEmptyMessage(0x11);
			}
		} else {
			bubbleContent.setVisibility(View.GONE);
			bubbleGraffiti.setVisibility(View.VISIBLE);
			imageLoader.loadImage(words.getGraffiti().getFileUrl(), options,
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String imageUri, View view) {

						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {

						}

						@Override
						public void onLoadingComplete(String imageUri,
								View arg, Bitmap loadedImage) {
							bubbleGraffiti.setImageBitmap(loadedImage);
							markerOptions.icon(BitmapDescriptorFactory
									.fromView(view));
							aMap.addMarker(markerOptions);
							if (!isSingel) {
								addMarkHandler.sendEmptyMessage(0x11);
							}

						}

						@Override
						public void onLoadingCancelled(String imageUri,
								View view) {

						}
					});
		}
	}

	// 上传文字留言
	public void addWordsToDataBase(User user, final String content,
			final LatLng position) {

		final Words words = new Words();
		words.setContent(content);
		words.setLocation(new BmobGeoPoint(position.longitude,
				position.latitude));
		words.setBubbleColor(bubbleColor);
		words.setUser(user);
		words.setIsText(true);
		words.setLocationInfo(locationInfo);
		words.save(new SaveListener<String>() {

			@Override
			public void done(String arg0, BmobException e) {
				if (e == null) {
					addMarkerToMap(words, true);
				} else {
					Toast.makeText(getActivity(), "留言失败" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			}
		}
		// new SaveListener() {
		//
		// @Override
		// public void onSuccess() {
		// addMarkerToMap(words, true);
		// }
		//
		// @Override
		// public void onFailure(int arg0, String arg1) {
		// Toast.makeText(getActivity(), "留言失败" + arg1, Toast.LENGTH_SHORT)
		// .show();
		// }
		// }
		);
	}

	private Handler addMarkHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0x11) {
				markFlag++;
				if (markFlag < markCount) {
					addMarkerToMap(markList.get(markFlag), false);
				}
			}
		};
	};

	// 添加已经存在Mark
	public void addExistingMark() {
		markFlag = 0;
		aMap.clear();
		BmobQuery<Words> query = new BmobQuery<Words>();
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.addWhereWithinKilometers("location", new BmobGeoPoint(
				myLatLng.longitude, myLatLng.latitude), 5);
		query.findObjects(new FindListener<Words>() {

			@Override
			public void done(List<Words> words, BmobException e) {

				if (e == null) {
					markCount = words.size();
					markList = words;
					addMarkerToMap(markList.get(markFlag), false);
				} else {
					if (e.getErrorCode() != 9015 && e.getErrorCode() != 9009) {
						Toast.makeText(
								getActivity(),
								"查询留言失败，请尝试刷新" + e.getErrorCode()
										+ e.getMessage(), Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
			// @Override
			// public void onSuccess(final List<Words> words) {
			// markCount = words.size();
			// markList = words;
			// addMarkerToMap(markList.get(markFlag), false);
			// }
			//
			// @Override
			// public void onError(int arg0, String arg1) {
			// if (arg0 != 9015 && arg0 != 9009) {
			// Toast.makeText(getActivity(), "查询留言失败，请尝试刷新" + arg0 + arg1,
			// Toast.LENGTH_SHORT).show();
			// }
			// }
		});
	}

	@Override
	public void onLoad() {
		skip += pageSize;
		loadwords(LOADMORE);
	}

	@Override
	public void onRefresh() {
		skip = 0;
		loadwords(REFRESH);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		aMap.clear();
		addMarkerToMap(wordsList.get(position - 1), true);
		wordsDetailDialog.dismiss();
	}

	public void loadwords(final int TYPE) {
		BmobQuery<Words> query = new BmobQuery<Words>();
		query.setLimit(pageSize);
		query.setSkip(skip);
		query.order("-createdAt");
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.addWhereWithinKilometers("location", new BmobGeoPoint(
				myLatLng.longitude, myLatLng.latitude), 5);
		query.include("user");
		query.findObjects(new FindListener<Words>() {

			@Override
			public void done(List<Words> results, BmobException e) {

				if (e == null) {
					onSuccess(results);
				} else {
					onError(e.getErrorCode(), e.getMessage());
				}
			}

			public void onSuccess(final List<Words> results) {
				wordsFlag = 0;
				wordsCount = results.size();
				if (results.size() > 0) {
					loadUserLikes(results.get(wordsFlag));
				} else {
					switch (TYPE) {
					case REFRESH:
						wordsListView.onRefreshComplete();
						wordsListView.setResultSize(-1);
						wordsAdapter.getWordsList().clear();
						wordsAdapter.notifyDataSetChanged();
						break;
					case LOADMORE:
						wordsListView.onLoadComplete();
						wordsListView.setResultSize(0);
						break;
					}
				}
				getWordsHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (msg.what == 0x34) {
							loadUserLikes(results.get(wordsFlag));
						} else if (msg.what == 0x35) {
							switch (TYPE) {
							case REFRESH:
								wordsListView.onRefreshComplete();
								wordsAdapter.getWordsList().clear();
								wordsAdapter.getWordsList().addAll(results);
								break;
							case LOADMORE:
								wordsListView.onLoadComplete();
								wordsAdapter.getWordsList().addAll(results);
								break;
							}
							wordsListView.setResultSize(results.size());
							wordsAdapter.notifyDataSetChanged();
							wordsList = wordsAdapter.getWordsList();
						}
					}
				};
			}

			public void onError(int arg0, String arg1) {
				if (arg0 != 9015 && arg0 != 9009) {
					Toast.makeText(getActivity(), "获取留言失败" + arg1,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void loadUserLikes(final Words words) {
		BmobQuery<User> query = new BmobQuery<User>();
		query.setLimit(999);
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.addWhereRelatedTo("likes", new BmobPointer(words));
		query.findObjects(new FindListener<User>() {

			@Override
			public void done(List<User> likeUsers, BmobException e) {

				if (e == null) {
					List<String> ids = new ArrayList<String>();
					for (int i = 0; i < likeUsers.size(); i++) {
						ids.add(likeUsers.get(i).getObjectId());
					}
					words.setLikeUsersId(ids);
					wordsFlag++;
					if (wordsFlag < wordsCount) {
						getWordsHandler.sendEmptyMessage(0x34);
					} else {
						getWordsHandler.sendEmptyMessage(0x35);
					}

				}
			}
			// @Override
			// public void onSuccess(List<User> likeUsers) {
			// List<String> ids = new ArrayList<String>();
			// for (int i = 0; i < likeUsers.size(); i++) {
			// ids.add(likeUsers.get(i).getObjectId());
			// }
			// words.setLikeUsersId(ids);
			// wordsFlag++;
			// if (wordsFlag < wordsCount) {
			// getWordsHandler.sendEmptyMessage(0x34);
			// } else {
			// getWordsHandler.sendEmptyMessage(0x35);
			// }
			// }

		});
	}

	@Override
	public void onLikeClick(View v) {
		final User currentUser = BmobUser.getCurrentUser(User.class);
		if (currentUser == null) {
			Toast.makeText(getActivity(), "登录后才能点赞哦", Toast.LENGTH_LONG).show();
		} else {
			final int position = (Integer) v.getTag();
			Words words = wordsAdapter.getWordsList().get(position);
			final BmobRelation relation = new BmobRelation();
			relation.add(currentUser);
			words.setLikes(relation);
			words.update(new UpdateListener() {
				@Override
				public void done(BmobException e) {
					if (e==null) {
						wordsAdapter.getWordsList().get(position).getLikeUsersId()
						.add(currentUser.getObjectId());
				wordsAdapter.notifyDataSetChanged();
					}else {
						Toast.makeText(getActivity(), "点赞失败，请检查网络后重试",
								Toast.LENGTH_SHORT).show();
					}

				}

				// @Override
				// public void onSuccess() {
				// wordsAdapter.getWordsList().get(position).getLikeUsersId()
				// .add(currentUser.getObjectId());
				// wordsAdapter.notifyDataSetChanged();
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// Toast.makeText(getActivity(), "点赞失败，请检查网络后重试",
				// Toast.LENGTH_SHORT).show();
				// }
			});

		}
	}

	public class MarkDeleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			addExistingMark();
		}

	}
}
