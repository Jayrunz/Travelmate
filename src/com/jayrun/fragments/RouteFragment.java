package com.jayrun.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.AMap.OnMapLongClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.maps2d.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.jayrun.travelmate.R;
import com.jayrun.travelmate.WalkRouteDetailActivity;
import com.jayrun.utils.AMapUtil;
import com.jayrun.utils.Constants;

public class RouteFragment extends Fragment implements OnMarkerClickListener,
		InfoWindowAdapter, OnRouteSearchListener, OnMapLongClickListener,
		LocationSource, AMapLocationListener, OnClickListener,
		OnMapClickListener {
	private final int ROUTE_TYPE_WALK = 3;

	private AMap aMap;
	private LatLonPoint myLocation;
	private LatLonPoint targetLocation;
	private RouteSearch routeSearch;
	private WalkRouteResult walkRouteResult;
	private UiSettings mapSettings;
	private OnLocationChangedListener locationChangedListener;
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationClientOption = null;
	// 初始化景区位置的查询
	private Query locationQuery;
	private PoiSearch locationSearch;
	private PoiResult locationPoiResult;
	private List<PoiItem> locationPoiItems;
	// 搜周边的查询
	private Query aroundSearchQuery;
	private PoiSearch aroundSearch;
	private PoiResult aroundSearchResult;
	private List<PoiItem> aroundPoiItems;
	private myPoiOverlay aroundPoiOverlay;
	private Marker clickedMark;

	private String scenicName;
	private String city;

	private ImageView search;
	private ImageView clearSearText;
	private EditText searchText;
	private MapView mapView;
	private ImageView refresh;
	private TextView routeTime;
	private RelativeLayout bottomLayout;
	private LinearLayout mapLoadingPro;

	private InputMethodManager inputManager;

	// private ProgressDialog progressDialog = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_route, null);
		inputManager = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		search = (ImageView) view.findViewById(R.id.map_search_begin);
		search.setOnClickListener(this);
		clearSearText = (ImageView) view
				.findViewById(R.id.map_clear_search_text);
		clearSearText.setOnClickListener(this);
		searchText = (EditText) view.findViewById(R.id.map_search_text);
		searchText.addTextChangedListener(searchTextWatcher);
		searchText.setOnKeyListener(keyListener);
		mapView = (MapView) view.findViewById(R.id.route_map);
		mapView.onCreate(savedInstanceState);
		refresh = (ImageView) view.findViewById(R.id.refresh_map);
		routeTime = (TextView) view.findViewById(R.id.firstline);
		bottomLayout = (RelativeLayout) view.findViewById(R.id.bottom_layout);
		mapLoadingPro = (LinearLayout) view.findViewById(R.id.map_loading_lin);
		init();
		return view;
	}

	private TextWatcher searchTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void afterTextChanged(Editable arg0) {
		}

		@Override
		public void onTextChanged(CharSequence text, int arg1, int arg2,
				int arg3) {
			if (text.toString().length() > 0) {
				clearSearText.setVisibility(View.VISIBLE);
			} else {
				clearSearText.setVisibility(View.GONE);
			}
		}

	};
	private OnKeyListener keyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				if (Constants.removeBlankAtBegin(
						searchText.getText().toString()).isEmpty()) {
					Toast.makeText(getActivity(), "搜索内容不能为空",
							Toast.LENGTH_SHORT).show();
					searchText.setText("");
				} else {
					refreshMap();
					serachAround(searchText.getText().toString());

				}
			}
			return false;
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.map_search_begin:
			if (Constants.removeBlankAtBegin(searchText.getText().toString())
					.isEmpty()) {
				Toast.makeText(getActivity(), "搜索内容不能为空", Toast.LENGTH_SHORT)
						.show();
				searchText.setText("");
			} else {
				refreshMap();
				serachAround(searchText.getText().toString());
				if (inputManager.isActive()) {
					((InputMethodManager) getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(getActivity()
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
			break;

		case R.id.map_clear_search_text:
			searchText.setText("");
			break;
		}
	}

	//搜索附近POI
	private void serachAround(String keywords) {
		locationClient.startLocation();
		mapLoadingPro.setVisibility(View.VISIBLE);
		aroundSearchQuery = new Query(keywords, "", city);
		aroundSearchQuery.setPageSize(20);
		aroundSearchQuery.setPageNum(0);
		aroundSearch = new PoiSearch(getActivity(), aroundSearchQuery);
		aroundSearch.setOnPoiSearchListener(aroundPoiSearchListener);
		// 搜索范围为以景区为中心方圆4000米
		if (myLocation == null) {
			Toast.makeText(getActivity(), "定位失败，请稍后再试！", Toast.LENGTH_SHORT)
					.show();
		} else {
			aroundSearch.setBound(new SearchBound(myLocation, 4000, true));
			aroundSearch.searchPOIAsyn();
		}
	}

	private OnPoiSearchListener aroundPoiSearchListener = new OnPoiSearchListener() {

		@Override
		public void onPoiSearched(PoiResult aroundResult, int code) {
			mapLoadingPro.setVisibility(View.GONE);
			if (code == 1000) {
				if (aroundResult != null && aroundResult.getQuery() != null) {
					aroundSearchResult = aroundResult;
					aroundPoiItems = aroundSearchResult.getPois();
					if (aroundPoiItems != null && aroundPoiItems.size() > 0) {
						if (clickedMark != null) {
							if (clickedMark.isInfoWindowShown()) {
								clickedMark.hideInfoWindow();
							}
						}
						if (aroundPoiOverlay != null) {
							aroundPoiOverlay.removeFromMap();
						}
						aMap.clear();
						aroundPoiOverlay = new myPoiOverlay(aMap,
								aroundPoiItems);
						aroundPoiOverlay.addToMap();
						aroundPoiOverlay.zoomToSpan();
					} else {
						Toast.makeText(getActivity(), "对不起，附近未搜到相关数据",
								Toast.LENGTH_LONG).show();
					}

				} else {
					Toast.makeText(getActivity(), "对不起，附近未搜到相关数据",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "对不起，附近未搜到相关数据",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onPoiItemSearched(PoiItem arg0, int arg1) {

		}
	};

	private void loacationSearchQuery() {
		mapLoadingPro.setVisibility(View.VISIBLE);
		locationQuery = new Query(scenicName, "", city);
		locationQuery.setPageSize(5);
		locationQuery.setPageNum(0);
		locationSearch = new PoiSearch(getActivity(), locationQuery);
		locationSearch.setOnPoiSearchListener(locationPoiSearchListener);
		locationSearch.searchPOIAsyn();
	}

	private OnPoiSearchListener locationPoiSearchListener = new OnPoiSearchListener() {

		@Override
		public void onPoiSearched(PoiResult result, int code) {
			mapLoadingPro.setVisibility(View.GONE);
			if (code == 1000) {
				if (result != null && result.getQuery() != null) {
					// if (result.getQuery() == scenicLocationQuery) {
					locationPoiResult = result;
					locationPoiItems = locationPoiResult.getPois();
					if (locationPoiItems != null && locationPoiItems.size() > 0) {
						// aMap.clear();
						PoiOverlay poiOverlay = new PoiOverlay(aMap,
								locationPoiItems);
						// poiOverlay.removeFromMap();
						// poiOverlay.addToMap();
						poiOverlay.zoomToSpan();
						// poiOverlay.removeFromMap();
					} else {
						Toast.makeText(getActivity(), "获取位置失败，请稍后重试",
								Toast.LENGTH_LONG).show();
					}

				} else {
					Toast.makeText(getActivity(), "获取位置失败，请稍后重试",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "加载地图失败，请稍后重试", Toast.LENGTH_LONG)
						.show();
			}

		}

		@Override
		public void onPoiItemSearched(PoiItem arg0, int arg1) {

		}
	};

	@Override
	public void onMapLongClick(LatLng clickPoint) {
		targetLocation = new LatLonPoint(clickPoint.latitude,
				clickPoint.longitude);
		locationClient.startLocation();
		try {

			if (AMapUtils.calculateLineDistance(
					AMapUtil.convertToLatLng(targetLocation),
					AMapUtil.convertToLatLng(myLocation)) > 4000) {
				Toast.makeText(getActivity(), "̫太远了，走不到哦", Toast.LENGTH_SHORT)
						.show();
			} else {
				aMap.setLocationSource(this);
				mapSettings.setMyLocationButtonEnabled(true);
				aMap.setMyLocationEnabled(true);
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				RelativeLayout layout = (RelativeLayout) inflater.inflate(
						R.layout.dialog_reminder, null);
				TextView textView = (TextView) layout.findViewById(R.id.remind);
				textView.setText("您确定到这里去？");
				Button dialogOK = (Button) layout.findViewById(R.id.dia_ok);
				Button dialogCancle = (Button) layout
						.findViewById(R.id.dia_cancle);
				final Dialog dialog = new Dialog(getActivity());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.getWindow().setBackgroundDrawableResource(
						R.drawable.dialog_bg_alpha0);
				dialog.show();
				dialog.getWindow().setContentView(layout);
				dialogOK.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						searchWalkRouteResult();
						dialog.dismiss();
						aMap.setMyLocationEnabled(false);
					}
				});
				dialogCancle.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
						aMap.setMyLocationEnabled(false);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	//规划路径
	public void searchWalkRouteResult() {
		if (myLocation == null) {
			Toast.makeText(getActivity(), "定位失败,请稍后重试", Toast.LENGTH_LONG)
					.show();
		}
		if (targetLocation == null) {
			Toast.makeText(getActivity(), "位置获取失败", Toast.LENGTH_LONG).show();
		}
		mapLoadingPro.setVisibility(View.VISIBLE);
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				myLocation, targetLocation);
		WalkRouteQuery query = new WalkRouteQuery(fromAndTo, ROUTE_TYPE_WALK);
		routeSearch.calculateWalkRouteAsyn(query);
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
		mapLoadingPro.setVisibility(View.GONE);
		refresh.setVisibility(View.VISIBLE);
		aMap.clear();
		if (errorCode == 1000) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
					walkRouteResult = result;

					final WalkPath walkPath = walkRouteResult.getPaths().get(0);

					WalkRouteOverlay routeOverlay = new WalkRouteOverlay(
							getActivity(), aMap, walkPath,
							walkRouteResult.getStartPos(),
							walkRouteResult.getTargetPos());
					routeOverlay.removeFromMap();
					routeOverlay.addToMap();
					routeOverlay.zoomToSpan();

					bottomLayout.setVisibility(View.VISIBLE);
					int dis = (int) walkPath.getDistance();
					int dur = (int) walkPath.getDuration();
					String des = "预计" + AMapUtil.getFriendlyTime(dur) + "("
							+ AMapUtil.getFriendlyLength(dis) + ")";
					routeTime.setText(des);
					bottomLayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(getActivity(),
									WalkRouteDetailActivity.class);
							intent.putExtra("walk_path", walkPath);
							intent.putExtra("walk_result", walkRouteResult);
							startActivity(intent);
						}
					});
				} else if (result.getPaths().size() == 0) {
					bottomLayout.setVisibility(View.GONE);
					Toast.makeText(getActivity(), "未查询 到路径", Toast.LENGTH_LONG)
							.show();
				} else if (result != null && result.getPaths() == null) {
					bottomLayout.setVisibility(View.GONE);
					Toast.makeText(getActivity(), "路径查询失败", Toast.LENGTH_LONG)
							.show();
				}

			} else {
				bottomLayout.setVisibility(View.GONE);
				Toast.makeText(getActivity(), "路径查询失败", Toast.LENGTH_LONG)
						.show();
			}
		} else {
			bottomLayout.setVisibility(View.GONE);
			Toast.makeText(getActivity(), errorCode + "", Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		myLocation = new LatLonPoint(aMapLocation.getLatitude(),
				aMapLocation.getLongitude());
		if (locationChangedListener != null && aMapLocation != null) {
			if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
				locationChangedListener.onLocationChanged(aMapLocation);
			} else {
				// Toast.makeText(
				// getActivity(),
				// "��λʧ��" + aMapLocation.getErrorCode() + ":"
				// + aMapLocation.getErrorInfo(),
				// Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public View getInfoWindow(final Marker marker) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.window_around_search, null);
		TextView aroundTitle = (TextView) view
				.findViewById(R.id.around_info_title);
		TextView aroundSnippet = (TextView) view
				.findViewById(R.id.around_info_snippet);
		Button getWay = (Button) view.findViewById(R.id.get_way_there);
		aroundTitle.setText(marker.getTitle());
		aroundSnippet.setText(marker.getSnippet());
		getWay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				targetLocation = AMapUtil.convertToLatLonPoint(marker
						.getPosition());
				if (AMapUtils.calculateLineDistance(
						AMapUtil.convertToLatLng(targetLocation),
						AMapUtil.convertToLatLng(myLocation)) > 4000) {
					Toast.makeText(getActivity(), "̫太远了，走不到哦",
							Toast.LENGTH_SHORT).show();
				} else {
					searchWalkRouteResult();
				}
			}
		});
		return view;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		marker.showInfoWindow();
		if (marker.getObject() != null) {
			try {
				if (clickedMark == null) {
					clickedMark = marker;
				} else {
					// 将之前被点击的marker置为原来的状态
					resetClickedMarker();
					clickedMark = marker;
				}
				// clickedMark = marker;
				clickedMark
						.setIcon(BitmapDescriptorFactory
								.fromBitmap(BitmapFactory.decodeResource(
										getResources(),
										R.drawable.poi_marker_pressed)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			resetClickedMarker();
		}
		return true;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		if (clickedMark != null) {
			if (clickedMark.isInfoWindowShown()) {
				resetClickedMarker();
				clickedMark.hideInfoWindow();
			}
		}
	}

	public void init() {

		if (aMap == null) {
			aMap = mapView.getMap();
		}
		if (getArguments().containsKey("scenicName")) {
			scenicName = getArguments().getString("scenicName");
		}
		if (getArguments().containsKey("city")) {
			city = getArguments().getString("city");
		}
		aMap.setOnMapClickListener(this);
		aMap.setOnMapLongClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		routeSearch = new RouteSearch(getActivity());
		routeSearch.setRouteSearchListener(this);
		mapSettings = aMap.getUiSettings();
		mapSettings.setZoomControlsEnabled(false);
		locationClient = new AMapLocationClient(getActivity());
		locationClientOption = new AMapLocationClientOption();
		locationClientOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		locationClientOption.setOnceLocation(true);
		locationClient.setLocationOption(locationClientOption);
		locationClient.setLocationListener(this);
		locationClient.startLocation();
		refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				refreshMap();
			}
		});
		loacationSearchQuery();
	}

	private void refreshMap() {
		aMap.clear();
		if (bottomLayout.getVisibility() == View.VISIBLE) {
			bottomLayout.setVisibility(View.GONE);
		}
		if (refresh.getVisibility() == View.VISIBLE) {
			refresh.setVisibility(View.GONE);
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

	@Override
	public View getInfoContents(Marker arg0) {
		return null;
	}

	@Override
	public void onBusRouteSearched(BusRouteResult arg0, int arg1) {

	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	// 将之前被点击的marker置为原来的状态
	private void resetClickedMarker() {
		int index = aroundPoiOverlay.getPoiIndex(clickedMark);
		if (index < 10) {
			clickedMark.setIcon(BitmapDescriptorFactory
					.fromBitmap(BitmapFactory.decodeResource(getResources(),
							markers[index])));
		} else {
			clickedMark.setIcon(BitmapDescriptorFactory
					.fromBitmap(BitmapFactory.decodeResource(getResources(),
							R.drawable.marker_other)));
		}
		// clickedMark = null;

	}

	private int[] markers = { R.drawable.poi_marker_1, R.drawable.poi_marker_2,
			R.drawable.poi_marker_3, R.drawable.poi_marker_4,
			R.drawable.poi_marker_5, R.drawable.poi_marker_6,
			R.drawable.poi_marker_7, R.drawable.poi_marker_8,
			R.drawable.poi_marker_9, R.drawable.poi_marker_10 };

	private class myPoiOverlay {
		private AMap mamap;
		private List<PoiItem> mPois;
		private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();

		public myPoiOverlay(AMap amap, List<PoiItem> pois) {
			mamap = amap;
			mPois = pois;
		}

		public void addToMap() {
			for (int i = 0; i < mPois.size(); i++) {
				Marker marker = mamap.addMarker(getMarkerOptions(i));
				PoiItem item = mPois.get(i);
				marker.setObject(item);
				mPoiMarks.add(marker);
			}
		}

		public void removeFromMap() {
			for (Marker mark : mPoiMarks) {
				mark.remove();
			}
		}

		public void zoomToSpan() {
			if (mPois != null && mPois.size() > 0) {
				if (mamap == null)
					return;
				LatLngBounds bounds = getLatLngBounds();
				mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
						100));
			}
		}

		private LatLngBounds getLatLngBounds() {
			LatLngBounds.Builder b = LatLngBounds.builder();
			for (int i = 0; i < mPois.size(); i++) {
				b.include(new LatLng(mPois.get(i).getLatLonPoint()
						.getLatitude(), mPois.get(i).getLatLonPoint()
						.getLongitude()));
			}
			return b.build();
		}

		private MarkerOptions getMarkerOptions(int index) {
			return new MarkerOptions()
					.position(
							new LatLng(mPois.get(index).getLatLonPoint()
									.getLatitude(), mPois.get(index)
									.getLatLonPoint().getLongitude()))
					.title(getTitle(index)).snippet(getSnippet(index))
					.icon(getBitmapDescriptor(index));
		}

		protected String getTitle(int index) {
			return mPois.get(index).getTitle();
		}

		protected String getSnippet(int index) {
			return mPois.get(index).getSnippet();
		}

		/**
		 * 从marker中得到poi在list的位置。
		 * 
		 * @param marker
		 *            一个标记的对象。
		 * @return 返回该marker对应的poi在list的位置。
		 * @since V2.1.0
		 */
		public int getPoiIndex(Marker marker) {
			for (int i = 0; i < mPoiMarks.size(); i++) {
				if (mPoiMarks.get(i).equals(marker)) {
					return i;
				}
			}
			return -1;
		}

		public PoiItem getPoiItem(int index) {
			if (index < 0 || index >= mPois.size()) {
				return null;
			}
			return mPois.get(index);
		}

		protected BitmapDescriptor getBitmapDescriptor(int arg0) {
			if (arg0 < 10) {
				BitmapDescriptor icon = BitmapDescriptorFactory
						.fromBitmap(BitmapFactory.decodeResource(
								getResources(), markers[arg0]));
				return icon;
			} else {
				BitmapDescriptor icon = BitmapDescriptorFactory
						.fromBitmap(BitmapFactory.decodeResource(
								getResources(), R.drawable.marker_other));
				return icon;
			}
		}

	}
}
