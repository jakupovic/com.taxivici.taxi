package com.venitaxi.taxi;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.Toast;

import com.amelie.driving.Route;
import com.amelie.driving.DrivingDirections.Mode;
import com.amelie.driving.impl.RouteImpl;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class TaxiItemizedOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	Route route;
	Mode mode;
	
	public TaxiItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	@Override
	public int size() {
	  return mOverlays.size();
	}
	public TaxiItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(defaultMarker);
		  mContext = context;
	}
	
	@Override
	protected boolean onTap(int index) {
	    OverlayItem item = mOverlays.get(index);
	 /*
	    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	    
	    dialog.setTitle(item.getTitle());
	    dialog.setMessage(item.getSnippet());
	    dialog.setPositiveButton("Yes", new OnClickListener() {
	       
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        }
	    });
	    dialog.show();
	    
	    */
	    return true;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if (route != null) {
			drawRoute(route.getGeoPoints(), canvas, mapView);
			/*if (startIcon != null && middleIcon != null && endIcon != null) {*/
			//drawPlacemarks(route.getPlacemarks(), canvas, mapView);
			//}
		}
	}

	private void drawRoute(List geoPoints, Canvas canvas, MapView mapView) {
		for (int i = 0; i < geoPoints.size() - 1; i++) {
			// Convert the starting geographical point coordinates to screen
			// coordinates.
			//
			GeoPoint startGeoPoint = (GeoPoint) geoPoints.get(i);
			Point startScreenPoint = new Point();
			mapView.getProjection().toPixels(startGeoPoint, startScreenPoint);

			// Convert the ending geographical point coordinates to screen
			// coordinates.
			//
			GeoPoint endGeoPoint = (GeoPoint) geoPoints.get(i + 1);
			Point endScreenPoint = new Point();
			mapView.getProjection().toPixels(endGeoPoint, endScreenPoint);

			// Draw a joining line between the starting and the ending points.
			//
			Paint paint = new Paint();
			paint.setStrokeWidth(5);
			paint.setAntiAlias(true);
			if ((mode != null) && (mode == Mode.DRIVING)) {
				paint.setARGB(64, 0, 0, 255);
			} else {
				paint.setARGB(96, 0, 0, 0);
				paint
						.setPathEffect(new DashPathEffect(new float[] { 8, 4 },
								1));
			}
			canvas.drawLine(startScreenPoint.x, startScreenPoint.y,
					endScreenPoint.x, endScreenPoint.y, paint);
		}
	}

	/*private void drawPlacemarks(List placemarks, Canvas canvas, MapView mapView){
		for (int i = 0; i < placemarks.size(); i++)
		{
			// Convert the placemark geographical location to screen coordinates.
			//
			GeoPoint geoPoint = placemarks.get(i).getLocation();
			Point screenPoint = new Point();
			mapView.getProjection().toPixels(geoPoint, screenPoint);

			// Select the placemark to use (start, middle or end points) and draw its bitmap
			// on the screen.
			//
			Bitmap place;
			canvas.drawBitmap(bitmap, matrix, paint)
			if (i == 0) {
				// Start placemark.
				//
				//place = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_focused);
				canvas.drawBitmap(place, i, i, null);
				//R.drawable.android_focused
			}
			else if (i == placemarks.size() - 1) {
				// End placemark.
				//
				place = BitmapFactory.decodeResource(context.getResources(), endIcon.iconId);
				canvas.drawBitmap(…);
			}
			else {
				// Middle placemark.
				//
				place = BitmapFactory.decodeResource(context.getResources(), middleIcon.iconId);
				canvas.drawBitmap(…);
			}
		}
	}
  }*/
}
