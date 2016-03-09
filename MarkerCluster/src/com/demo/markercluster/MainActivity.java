package com.demo.markercluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.model.MyMarkerCluster;
import com.model.Person;

public class MainActivity extends FragmentActivity implements
		OnCameraChangeListener, LocationSource, AMapLocationListener,
		OnMapLoadedListener, OnMarkerClickListener {

	private String TAG = MainActivity.class.getName().toString();

	private AMap aMap;
	private MapView mapView;
	private UiSettings mUiSettings;
	private ArrayList<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();// 所有的marker
	private ArrayList<MarkerOptions> markerOptionsListInView = new ArrayList<MarkerOptions>();// 视野内的marker
	private int height;// 屏幕高度(px)
	private int width;// 屏幕宽度(px)

	private LocationSource.OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;

	private float NowZoom = 12.0f;

	private Marker locMarker;

	private ArrayList<Marker> markers;

	List<HashMap<String, Person>> mapList = null;
	private HashMap<String, Person> myHasMap = new HashMap<String, Person>();

	Handler timeHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				// 更新markers
				resetMarks();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		markers = new ArrayList<Marker>();

		mapList = new ArrayList<HashMap<String, Person>>();

		init();

	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
			mUiSettings = aMap.getUiSettings();
			mUiSettings.setTiltGesturesEnabled(false);// 禁用倾斜手势。
			mUiSettings.setRotateGesturesEnabled(false);// 禁用旋转手势。

			aMap.setOnMarkerClickListener(this);

			aMap.setLocationSource(MainActivity.this);// 设置定位监听
			aMap.getUiSettings().setZoomControlsEnabled(false);// 隐藏放大缩小按钮
			aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

			// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
			aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
			aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
			// aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
			// aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
			// aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式

		}
	}

	private void setUpMap() {
		aMap.setOnCameraChangeListener(this);
		addMarkers();// 聚合测试点添加
	}

	/**
	 * 模拟添加多个marker
	 */
	private void addMarkers() {
		myHasMap.clear();
		for (int i = 0; i < 200; i++) {
			HashMap<String, Person> maHashMap = new HashMap<String, Person>();

			Person person = new Person("gxw" + i, i);
			maHashMap.put("Marker" + i, person);

			// myHasMap.put("Marker" + i, person);
			LatLng latLng = new LatLng(Math.random() * 6 + 35,
					Math.random() * 6 + 112);
			markerOptionsList.add(new MarkerOptions()
					.position(latLng)
					.title("Marker" + i)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

			myHasMap.put("Marker" + i, person);

			mapList.add(maHashMap);
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
		// TODO Auto-generated method stub
		this.NowZoom = cameraPosition.zoom;
		timeHandler.sendEmptyMessage(0);// 更新界面marker
	}

	/**
	 * 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker
	 */
	private void resetMarks() {

		System.out.println("markerOptionsList.size():"
				+ markerOptionsList.size());
		// 开始刷新车辆
		Projection projection = aMap.getProjection();
		Point p = null;
		markerOptionsListInView.clear();
		// 获取在当前视野内的marker;提高效率
		for (MarkerOptions mp : markerOptionsList) {
			p = projection.toScreenLocation(mp.getPosition());
			if (p.x < 0 || p.y < 0 || p.x > width || p.y > height) {
				// 不添加到计算的列表中
			} else {
				markerOptionsListInView.add(mp);
			}
		}
		// 自定义的聚合类MyMarkerCluster
		ArrayList<MyMarkerCluster> clustersMarker = new ArrayList<MyMarkerCluster>();
		for (MarkerOptions mp : markerOptionsListInView) {
			if (clustersMarker.size() == 0) {
				clustersMarker.add(new MyMarkerCluster(MainActivity.this, mp,
						projection, 60));// 100根据自己需求调整
			} else {
				boolean isIn = false;
				for (MyMarkerCluster cluster : clustersMarker) {
					if (cluster.getBounds().contains(mp.getPosition())) {
						cluster.addMarker(mp);
						isIn = true;
						break;
					}
				}
				if (!isIn) {
					clustersMarker.add(new MyMarkerCluster(MainActivity.this,
							mp, projection, 60));
				}
			}
		}
		// 设置聚合点的位置和icon
		for (MyMarkerCluster mmc : clustersMarker) {
			mmc.setpositionAndIcon();
		}

		// 清除所有
		// aMap.clear();

		if (markers.size() != 0) {
			for (Marker marker : markers) {

				marker.remove();

			}
		}
		markers.clear();

		// 重新添加
		for (MyMarkerCluster cluster : clustersMarker) {

			Marker addMarker = aMap.addMarker(cluster.getOptions());
			markers.add(addMarker);
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		// TODO Auto-generated method stub

		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mlocationClient.setLocationListener(MainActivity.this);
			// 设置为高精度定位模式
			mLocationOption
					.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
			// 设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			mLocationOption.setInterval(10000);
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mlocationClient.startLocation();
		}

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub

		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				double lat = amapLocation.getLatitude();
				double lon = amapLocation.getLongitude();

				Log.i(TAG, lat + "  " + lon);

				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点

				// mListener.onLocationChanged(aLocation);//这行代码就是显示系统默认图标，现在注释掉
				// LatLng latLng = new LatLng(amapLocation.getLatitude(),
				// amapLocation.getLongitude());
				// 定位成功后把地图移动到当前可视区域内
				// if(locMarker!=null) locMarker.destroy();
				// if(circle!=null) circle.remove();
				// aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng
				// 10));
				// // 自定义定位成功后的小圆点
				// marker=aMap.addMarker(new MarkerOptions().position(latLng)
				// .anchor(0.5f 0.5f)// 锚点设置为中心
				// .icon(BitmapDescriptorFactory
				// .fromResource(R.drawable.marker_gps_no_sharing)));
				// // 自定义定位成功后绘制圆形
				// circle=aMap.addCircle(new
				// CircleOptions().center(latLng).radius(50000)
				// .fillColor(Color.BLUE).strokeColor(Color.BLACK)
				// .strokeWidth(3f));

				// if (isFirst) {//第一次显示当前位置
				//
				// Log.i(TAG,"------------------------first location-------------------------");
				// myLatLng = new LatLng(lat, lon);
				// handler.sendEmptyMessageDelayed(MsgWhat.A_LOC_SUCCESS, 0);
				// isFirst = false;
				// }

			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
			}
		}
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub

		HashMap localHashMap;
		do {
			int i;
			do {
				do {
					do {
						Log.i(TAG, " ----------1-------- " + marker.getTitle());
						if (!marker.getTitle().equals("-1"))
							break;
						return true;
					} while (this.NowZoom > 13.0F);
					Log.i(TAG, " ----------2-------- " + this.NowZoom + "");
					if ((this.NowZoom >= 10.0F) && (this.NowZoom <= 13.0F)) {
						this.NowZoom += 14.0F - this.NowZoom;
						Log.i(TAG, " ----------3-------- " + this.NowZoom + "");

						String title = marker.getTitle();

						Log.i(TAG, "marker title" + title);

						Person person = myHasMap.get(title);
						Log.i(TAG, person.toString());

						MapUtils.moveToPosition(this.aMap,
								Double.valueOf(marker.getPosition().latitude),
								Double.valueOf(marker.getPosition().longitude),
								this.NowZoom);
					}
					if ((this.NowZoom >= 7.0F) && (this.NowZoom < 10.0F)) {
						this.NowZoom += 10.0F - this.NowZoom;
						Log.i(TAG, " ----------4-------- " + this.NowZoom + "");
						MapUtils.moveToPosition(this.aMap,
								Double.valueOf(marker.getPosition().latitude),
								Double.valueOf(marker.getPosition().longitude),
								this.NowZoom);
					}
					if ((this.NowZoom >= 5.0F) && (this.NowZoom < 7.0F)) {
						this.NowZoom += 7.0F - this.NowZoom;
						Log.i(TAG, " ----------5-------- " + this.NowZoom + "");
						MapUtils.moveToPosition(this.aMap,
								Double.valueOf(marker.getPosition().latitude),
								Double.valueOf(marker.getPosition().longitude),
								this.NowZoom);
					}
					if (this.NowZoom < 5.0F) {
						this.NowZoom += 5.0F - this.NowZoom;
						Log.i(TAG, " ----------6-------- " + this.NowZoom + "");
						MapUtils.moveToPosition(this.aMap,
								Double.valueOf(marker.getPosition().latitude),
								Double.valueOf(marker.getPosition().longitude),
								this.NowZoom);
					}
					// Log.i(TAG, " ----------7-------- " + this.NowZoom + "");
				} while (this.mapList.size() <= 0);
				i = Integer.parseInt(marker.getTitle().substring(6, 8));
			} while (i < 0);
			MapUtils.moveToPosition(this.aMap,
					Double.valueOf(marker.getPosition().latitude),
					Double.valueOf(marker.getPosition().longitude),
					this.NowZoom);

			localHashMap = (HashMap) this.mapList.get(i);

			Log.i(TAG, "localHashMap:"
					+ localHashMap.get("Marker" + i).toString());

			return true;
			// Log.i("点击图钉2", localHashMap.toString());
		} while (localHashMap == null);
		// OnShowView(getActivity(), localHashMap);

		// return false;
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mapView.onDestroy();
		if (null != mlocationClient) {
			mlocationClient.onDestroy();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
