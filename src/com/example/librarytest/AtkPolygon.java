package com.example.librarytest;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
// This class isn't used yet
public class AtkPolygon extends FragmentActivity implements OnMapClickListener,
		OnMarkerDragListener, OnMarkerClickListener {

	private GoogleMap map = null;
	private RelativeLayout rel = null;
	private int inc = 0;
	private int selmkr = 0;
	private boolean hideT = true;
	private Polygon poly;
	private List<Marker> mkr = new ArrayList<Marker>();
	private Polyline line;
	private int endm = 1;
	private float dens;
	private Button trackpad, pls, mns, del, dis;
	private Activity active;
	private RelativeLayout rel2, rel3;
	private MapFragment mapf;
	private int shift = 0;

	public AtkPolygon(GoogleMap map, RelativeLayout rel, Activity active) {
		this.map = map;
		this.rel = rel;
		this.active = active;
	}
	public void startDrawing() {
		if (map != null && rel != null) {
			map.setOnMapClickListener(this);
			map.setOnMarkerDragListener(this);
			map.setOnMarkerClickListener(this);
			dens = Resources.getSystem().getDisplayMetrics().density;

			if (poly == null)
				poly = map.addPolygon(new PolygonOptions()
						.add(new LatLng(0, 0)).strokeWidth(2)
						.strokeColor(Color.BLACK).fillColor(0x7FB5B1E0));
			if (line == null)
				line = map.addPolyline(new PolylineOptions()
						.add(new LatLng(0, 0)).color(Color.WHITE).width(2));
			setButtons();
		}
	}
	public void redraw(Polygon poly){
		if(poly !=null){
			if(poly.getPoints().size() > 1){
			this.poly = poly;
			poly.setFillColor(0x7FB5B1E0);
			inc = poly.getPoints().size()-1;
			selmkr = inc;
			mkr.clear();
			for(int i = 0; i < inc ; i++) {
				mkr.add(i,map.addMarker(new MarkerOptions().position(poly.getPoints().get(i))
								.anchor((float) 0.5, (float) 0.5).draggable(true)
								.visible(hideT)));
				flagRed(mkr.get(i));
			}
			flagGreen(mkr.get(inc-1));
			line = map.addPolyline(new PolylineOptions()
			.add(mkr.get(0).getPosition(), mkr.get(inc-1).getPosition()).color(Color.WHITE).width(2));
			startDrawing();
			}
		}
	}
	public void stopDrawing() {
		map.setOnMapClickListener(null);
		if(active instanceof OnMapClickListener) map.setOnMapClickListener((OnMapClickListener) active);
		map.setOnMarkerDragListener(null);
		if(active instanceof OnMarkerDragListener) map.setOnMarkerDragListener((OnMarkerDragListener)active);
		map.setOnMarkerClickListener(null);
		if(active instanceof OnMarkerClickListener) map.setOnMarkerClickListener((OnMarkerClickListener)active);
		rel.removeView(trackpad);
		rel.removeView(del);
		rel.removeView(pls);
		rel.removeView(mns);
		rel.removeView(dis);
		rel.removeView(rel2);
		rel.removeView(rel3);
		for (int i = 0;i <inc; i++) mkr.get(i).remove();
		//mkr.clear();
		inc = 0;
		selmkr = 0;
		line.remove();
		poly = null;
		//poly.setFillColor(0xF1B5B1E0);
	}
	public Polygon getPolygon() {
		return poly;
	}
	public void clear(){
		map.clear();
	}
	public void shiftUp(int shift){
		this.shift = shift;
	}
	@Override
	public void onMapClick(LatLng point) {
		if (inc > 0)
			flagRed(mkr.get(selmkr - 1));
		mkr.add(selmkr,
				map.addMarker(new MarkerOptions().position(point)
						.anchor((float) 0.5, (float) 0.5).draggable(true)
						.visible(hideT)));
		selmkr++;
		inc++;
		flagGreen(mkr.get(selmkr - 1));
	}
	private void flagRed(Marker arg0) {
		BitmapDescriptor unsel = BitmapDescriptorFactory
				.fromResource(R.drawable.unselected_vertex);
		arg0.setIcon(unsel);
	}
	private void flagGreen(Marker arg0) {
		BitmapDescriptor sel = BitmapDescriptorFactory
				.fromResource(R.drawable.selected_vertex);
		arg0.setIcon(sel);
		if (inc > 0) {
			if (selmkr == inc)
				endm = 1;
			if (selmkr != inc)
				endm = selmkr + 1;
		}
		doOption();
	}
	private void doOption() {
		PolygonOptions options = new PolygonOptions();
		PolylineOptions lineOptions = new PolylineOptions();
		for (int i = 0; i < inc; i++) {
			options.add(mkr.get(i).getPosition());
		}
		lineOptions.add(mkr.get(selmkr - 1).getPosition(), mkr.get(endm - 1)
				.getPosition());
		poly.setPoints(options.getPoints());
		line.setPoints(lineOptions.getPoints());

	}
	@Override
	public boolean onMarkerClick(Marker arg0) {
		flagRed(mkr.get(selmkr - 1));
		for (int i = 0; i < inc; i++) {
			if (arg0.equals(mkr.get(i))) {
				selmkr = i + 1;
				flagGreen(arg0);
				break;
			}
		}
		return false;
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		if (arg0.equals(mkr.get(selmkr - 1))) mvScreen();
		doOption();
	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		resetrl();
		doOption();
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		if (arg0.equals(mkr.get(selmkr - 1))) mvScreen();
		doOption();
	}

	private void Plus() {
		if (inc > 1) {
			flagRed(mkr.get(selmkr - 1));
			if (selmkr == inc)
				selmkr = 1;
			else if (selmkr != inc)
				selmkr++;
			flagGreen(mkr.get(selmkr - 1));
		}
	}

	private void Minus() {
		if (inc > 1) {
			flagRed(mkr.get(selmkr - 1));
			if (selmkr == 1)
				selmkr = inc;
			else if (selmkr != 1)
				selmkr--;
			flagGreen(mkr.get(selmkr - 1));

		}
	}

	private void doDelete() {
		if (inc > 0) {
			mkr.get(selmkr - 1).remove();
			mkr.remove(selmkr - 1);
			inc--;
			selmkr--;
			if (selmkr == 0)
				selmkr = inc;
			if (selmkr > 0)
				flagGreen(mkr.get(selmkr - 1));
		}
	}

	private void doDis() {
		for (int i = 0; i < inc; i++) {
			mkr.get(i).setVisible(!hideT);
		}
		line.setVisible(!hideT);
		hideT = !hideT;
	}

	private void setButtons() {
		float h, w;
		h = rel.getHeight() / dens;
		w = rel.getWidth() / dens;
		createRel();
		del = addbutton(del,R.drawable.deletered, Math.round(50 * dens),
				Math.round(50 * dens), 60 * dens, (h - 60 - shift) * dens);
		dis = addbutton(dis, R.drawable.package_purge, Math.round(50 * dens),
				Math.round(50 * dens), (w - 125) * dens, (h - 60 - shift) * dens);
		pls = addbutton(pls, R.drawable.add, Math.round(50 * dens), Math.round(50 * dens),
				0, (h - 60 - shift) * dens);
		mns = addbutton(mns, R.drawable.minus, Math.round(50 * dens), Math.round(50 * dens),
				(w - 65) * dens, (h - 60 - shift) * dens);
		trackpad = addbutton(trackpad, R.drawable.move_red, Math.round(75 * dens),
				Math.round(100 * dens), ((w / 2) - 50) * dens, (h - 75 - shift) * dens);
		del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				doDelete();
			}
		});
		dis.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				doDis();
			}
		});
		pls.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Plus();
			}
		});
		mns.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Minus();
			}
		});
		trackpad.setOnTouchListener(new View.OnTouchListener() {
			Point pmkr = new Point();
			Point pmkr2 = new Point();
			float touchx;
			float touchy;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (inc > 0) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mvScreen();
						pmkr.set(
								map.getProjection().toScreenLocation(
										mkr.get(selmkr - 1).getPosition()).x,
								map.getProjection().toScreenLocation(
										mkr.get(selmkr - 1).getPosition()).y);
						pmkr2.set((int) event.getX(), (int) event.getY());
						break;
					case MotionEvent.ACTION_MOVE:
						touchx = (event.getX() - pmkr2.x) + pmkr.x;
						touchy = (event.getY() - pmkr2.y) + pmkr.y;
						mkr.get(selmkr - 1).setPosition(
								map.getProjection().fromScreenLocation(
										new Point(Math.round(touchx), Math
												.round(touchy))));
						mvScreen();
						doOption();
						break;
					case MotionEvent.ACTION_UP:
						resetrl();
						break;

					}
				}
				return false;
			}
		});
	}
	private void mvScreen() {
		fixBearing();
		mapf.getMap().moveCamera(
				CameraUpdateFactory.newLatLngZoom(mkr.get(selmkr - 1)
						.getPosition(), map.getCameraPosition().zoom + 2));
		rel2.setX(map.getProjection().toScreenLocation(
				mkr.get(selmkr - 1).getPosition()).x
				- 78 * dens);
		rel2.setY(map.getProjection().toScreenLocation(
				mkr.get(selmkr - 1).getPosition()).y
				- 170 * dens);
		rel3.setX(rel2.getX());
		rel3.setY(rel2.getY());
	}
	private void resetrl() {
		rel2.setX(-200 * dens);
		rel2.setY(-200 * dens);
		rel3.setX(rel2.getX());
		rel3.setY(rel2.getY());
	}
	private Button addbutton(Button btn, int dr, int height, int width,
			float x, float y) {
		btn = new Button(active.getBaseContext());
		btn.setBackgroundResource(dr);
		btn.setHeight(height);
		btn.setWidth(width);
		btn.setX(x);
		btn.setY(y);
		rel.addView(btn);
		return btn;
	}																																												
	private void createRel() {
		rel2 = new RelativeLayout(active.getBaseContext());
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				(int) (150 * dens), (int) (150 * dens));
		rel2.setLayoutParams(rlp);
		rel2.setPadding(2,2,2,2);
		rel2.setBackgroundColor(Color.WHITE);
		rel.addView(rel2);
		rel2.setId(100);	//this need to be corrected
		GoogleMapOptions options = new GoogleMapOptions();
		options.mapType(GoogleMap.MAP_TYPE_SATELLITE).compassEnabled(false)
				.rotateGesturesEnabled(false).tiltGesturesEnabled(false)
				.zoomControlsEnabled(false).camera(map.getCameraPosition());
		MapFragment.newInstance(options);
		mapf = MapFragment.newInstance(options);
		FragmentTransaction fragmentTransaction = active.getFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(100, mapf);
		fragmentTransaction.commit();
		rel3 = new RelativeLayout(active.getBaseContext());
		RelativeLayout.LayoutParams rlp3 = new RelativeLayout.LayoutParams(
				(int) (150 * dens), (int) (150 * dens));
		rel3.setLayoutParams(rlp3);
		rel.addView(rel3);
		ImageView img = new ImageView(active.getBaseContext());
		img.setBackgroundResource(R.drawable.cross_vertex_5);
		img.setX(30 * dens);
		img.setY(30 * dens);
		rel3.addView(img, (int) (90 * dens), (int) (90 * dens));
		resetrl();
	}
	private void fixBearing(){
		if (mapf.getMap().getCameraPosition().bearing != map
				.getCameraPosition().bearing) {
			mapf.getMap().moveCamera(
					CameraUpdateFactory.newCameraPosition(map.getCameraPosition()));
		}
	}
}
