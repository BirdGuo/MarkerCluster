package com.demo.markercluster;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

/**
 * Created by GXW on 2016/3/7 0007.
 * email:603004002@qq.com
 */
public class MapUtils {

    public static Marker addMarker(AMap paramAMap, LatLng paramLatLng, BitmapDescriptor paramBitmapDescriptor, String paramString1, String paramString2) {
        MarkerOptions localMarkerOptions = new MarkerOptions();
        localMarkerOptions.position(paramLatLng);
        localMarkerOptions.anchor(0.5F, 0.5F);
        localMarkerOptions.icon(paramBitmapDescriptor);
        localMarkerOptions.draggable(false);
        localMarkerOptions.title(paramString1);
        localMarkerOptions.snippet(paramString2);
        return paramAMap.addMarker(localMarkerOptions);
    }

    public static void animateCamera(AMap paramAMap, Double paramDouble1, Double paramDouble2, float paramFloat, int paramInt) {
        paramAMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(paramDouble1.doubleValue(), paramDouble2.doubleValue()), paramFloat, 0.0F, 0.0F)), paramInt, null);
    }

    public static Bitmap getViewBitmap(View paramView) {
//        paramView.measure(View.MeasureSpec.makeMeasureSpec(0,0), View.MeasureSpec.makeMeasureSpec(0,0));
        paramView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        paramView.layout(0, 0, paramView.getMeasuredWidth(), paramView.getMeasuredHeight());
        paramView.buildDrawingCache();
        return paramView.getDrawingCache();
    }

    public static void moveCamera(AMap paramAMap, Double paramDouble1, Double paramDouble2, float paramFloat) {
        paramAMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(paramDouble1.doubleValue(), paramDouble2.doubleValue()), paramFloat, 0.0F, 0.0F)));
    }

    public static void moveToCurrentPosition(AMap paramAMap) {
        if ((paramAMap != null) && (paramAMap.getMyLocation() != null))
            animateCamera(paramAMap, Double.valueOf(paramAMap.getMyLocation().getLatitude()), Double.valueOf(paramAMap.getMyLocation().getLongitude()), 12.0F, 500);
    }

    public static void moveToCurrentPositionWithZoom(AMap paramAMap, float paramFloat) {
        if ((paramAMap != null) && (paramAMap.getMyLocation() != null))
            animateCamera(paramAMap, Double.valueOf(paramAMap.getMyLocation().getLatitude()), Double.valueOf(paramAMap.getMyLocation().getLongitude()), paramFloat, 500);
    }

    public static void moveToPosition(AMap paramAMap, Double paramDouble1, Double paramDouble2, float paramFloat) {
        animateCamera(paramAMap, paramDouble1, paramDouble2, paramFloat, 500);
    }

    public static void moveToPosition(AMap paramAMap, Double paramDouble1, Double paramDouble2, float paramFloat, int paramInt) {
        animateCamera(paramAMap, paramDouble1, paramDouble2, paramFloat, paramInt);
    }

//    public Drawable getDrawAble(int clusterNum, Context context, int clusterRadius) {
//        int radius = ScreenUtil.dp2px(context, clusterRadius);
//        if (clusterNum == 1) {
//            return context.getResources().getDrawable(
//                    R.mipmap.ic_charge_point);
//        } else if (clusterNum < 5) {
//            BitmapDrawable drawable = new BitmapDrawable(drawCircle(radius,
//                    Color.argb(159, 210, 154, 6)));
//            return drawable;
//        } else if (clusterNum < 10) {
//            BitmapDrawable drawable = new BitmapDrawable(drawCircle(radius,
//                    Color.argb(199, 217, 114, 0)));
//            return drawable;
//        } else {
//            BitmapDrawable drawable = new BitmapDrawable(drawCircle(radius,
//                    Color.argb(235, 215, 66, 2)));
//            return drawable;
//        }
//    }

    public static Bitmap drawCircle(int radius, int color) {
        Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        RectF rectF = new RectF(0, 0, radius * 2, radius * 2);
        paint.setColor(color);
        canvas.drawArc(rectF, 0, 360, true, paint);
        return bitmap;
    }

}
