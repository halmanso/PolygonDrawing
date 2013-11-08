package com.example.librarytest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;

public class AtkMapWidget {
	private GoogleMap map = null;
	private RelativeLayout rel = null;
	private Activity active;
	private RelativeLayout rel2, rel3;
	private float dens;
	private MapFragment mapf;
	private Marker mkr1;
	private List<Marker> mkr = new ArrayList<Marker>();
	
	private Button trackpad,pls,mns;
	private int shift = 0;
	private float h, w;
	private AtkMapWidgetListener listener;
	public int index,nextIndex,prevIndex,oldNextIndex,oldPreviousIndex;


	public interface AtkMapWidgetListener {
		public void TrackPadDragListener();
		public Marker AtkPassMarker();
		public List<Marker> AtkPassMarkerList();
		public void UpdateSelectedMarker(boolean isButtonSelected,int index,int nextIndex,
				int prevIndex,int oldNextIndex,int oldPreviousIndex);
	}
	
	
	public AtkMapWidget(GoogleMap map, RelativeLayout rel, Activity active) {
		// Initialization
		this.map = map;
		this.rel = rel;
		this.active = active;
		this.listener = (AtkMapWidgetListener) active;
		this.dens = Resources.getSystem().getDisplayMetrics().density;
		
		// Create the magnifying window
		createRel();
		
		// Create buttons
		Log.d("AtkMarker", "Initialize: getWidth:" + Integer.toString(rel.getWidth()));
		this.h = rel.getHeight() / this.dens;
		this.w = rel.getWidth() / this.dens;
		this.trackpad = addbutton(this.trackpad, R.drawable.move_red, Math.round(75 * dens),
				Math.round(100 * dens), ((w / 2) - 50) * dens, (h - 75 - shift) * dens);
		this.pls = addbutton(this.pls, R.drawable.add, Math.round(50 * dens), Math.round(50 * dens),
				0, (h - 60 - shift) * dens);
		this.mns = addbutton(this.mns, R.drawable.minus, Math.round(50 * dens), Math.round(50 * dens),
				(w - 65) * dens, (h - 60 - shift) * dens);
		//add buttons to relative layout
		rel.addView(this.trackpad);
		rel.addView(this.pls);
		rel.addView(this.mns);
		//set buttons listeners
		setTrack();
		setNextMarkerSelection();
		setPreviousMarkerSelection();
	}
	
	private void setTrack(){
		trackpad.setOnTouchListener(new View.OnTouchListener() {
			Point pmkr = new Point();
			Point pmkr2 = new Point();
			float touchx;
			float touchy;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mkr1 = listener.AtkPassMarker();
					if(mkr1==null)break;
					mvScreen();
					pmkr.set(
							map.getProjection().toScreenLocation(
									mkr1.getPosition()).x,
							map.getProjection().toScreenLocation(
									mkr1.getPosition()).y);
					pmkr2.set((int) event.getX(), (int) event.getY());
					break;
				case MotionEvent.ACTION_MOVE:
					if(mkr1==null)break;
					touchx = (event.getX() - pmkr2.x) + pmkr.x;
					touchy = (event.getY() - pmkr2.y) + pmkr.y;
					mkr1.setPosition(
							map.getProjection().fromScreenLocation(
									new Point(Math.round(touchx), Math
											.round(touchy))));
					mvScreen();
					listener.TrackPadDragListener();
					
					break;
				case MotionEvent.ACTION_UP:
					if(mkr1==null)break;
					resetrl();
					listener.TrackPadDragListener();
					break;

				}
				return false;
			}
		});
	}
	private void setNextMarkerSelection(){
		pls.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(mkr.size() == 0) mkr = listener.AtkPassMarkerList();
					mkr1 = listener.AtkPassMarker();
					if(mkr == null)break;
					if(mkr.size() == 0)break;
					index = getIndex();
					oldNextIndex = getNextIndex(index);
					oldPreviousIndex = getPreviousIndex(index);
					index = getNextIndex(index);
					prevIndex = getPreviousIndex(index);
					nextIndex = getNextIndex(index);
					break;
				case MotionEvent.ACTION_UP:
					if(mkr.size()==0)break;
					listener.UpdateSelectedMarker(true,index,nextIndex,prevIndex,oldNextIndex,oldPreviousIndex);
					break;
				}
				return false;
			}
		});
	}
	private void setPreviousMarkerSelection(){
		mns.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(mkr.size() == 0)mkr = listener.AtkPassMarkerList();
					mkr1 = listener.AtkPassMarker();
					if(mkr == null)break;
					if(mkr.size() == 0)break;
					index = getIndex();
					oldNextIndex = getNextIndex(index);
					oldPreviousIndex = getPreviousIndex(index);
					index = getPreviousIndex(index);
					nextIndex = getNextIndex(index);
					prevIndex = getPreviousIndex(index);
					break;
				case MotionEvent.ACTION_UP:
					if(mkr.size() == 0)break;
					listener.UpdateSelectedMarker(true,index,nextIndex,prevIndex,oldNextIndex,oldPreviousIndex);
					break;
				}
				return false;
			}
		});
	}
	public void setSelectionIcons(){
		int index = 0;
		if(mkr.size() == 0)mkr = listener.AtkPassMarkerList();
		if(mkr.size() > 0){
			mkr1 = listener.AtkPassMarker();
			if(nextIndex >= mkr.size() && mkr.size() > 0)nextIndex = mkr.size()-1;
			if(prevIndex >= mkr.size() && mkr.size() > 0)prevIndex = mkr.size()-1;
			index = getIndex();
			listener.UpdateSelectedMarker(false,index,getNextIndex(index),getPreviousIndex(index),nextIndex,prevIndex);
			nextIndex = getNextIndex(index);
			prevIndex = getPreviousIndex(index);
		}
	}
	public void showAtkButtons(){
		trackpad.setVisibility(View.VISIBLE);
		pls.setVisibility(View.VISIBLE);
		mns.setVisibility(View.VISIBLE);
	}
	public void hideAtkButtons(){
		trackpad.setVisibility(View.INVISIBLE);
		pls.setVisibility(View.INVISIBLE);
		mns.setVisibility(View.INVISIBLE);
	}
	public void showPop(Marker mkr1){
		this.mkr1 = mkr1;
		mvScreen();
	}
	public void movePop(){
		mvScreen();
	}
	public void shiftAtkButtonsUp(int shift){
		this.shift = shift;
		trackpad.setY((h - 75 - shift) * dens);
		pls.setY((h - 60 - shift) * dens);
		mns.setY((h - 60 - shift) * dens);
	}
	public void hidePop(){
		resetrl();
	}
	public void ClearAtkMapWidget(){
		//clearing all added views
		rel.removeView(trackpad);
		rel.removeView(pls);
		rel.removeView(mns);
		rel2.removeAllViews();
		rel3.removeAllViews();
		rel.removeView(rel2);
		rel.removeView(rel3);
	}
	private void createRel() {
		//creating relativelayout for the magnifying window
		rel2 = new RelativeLayout(active.getBaseContext());
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				(int) (150 * dens), (int) (150 * dens));
		rel2.setLayoutParams(rlp);
		rel2.setPadding(2,2,2,2);
		rel2.setBackgroundColor(Color.WHITE);
		rel.addView(rel2);
		rel2.setId(100);	//this need to be corrected
		//adding the map
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
		//adding another relative layout for the image
		rel3 = new RelativeLayout(active.getBaseContext());
		RelativeLayout.LayoutParams rlp3 = new RelativeLayout.LayoutParams(
				(int) (150 * dens), (int) (150 * dens));
		rel3.setLayoutParams(rlp3);
		rel.addView(rel3);
		//adding the image
		ImageView img = new ImageView(active.getBaseContext());
		img.setBackgroundResource(R.drawable.cross_vertex_5);
		img.setX(30 * dens);
		img.setY(30 * dens);
		rel3.addView(img, (int) (90 * dens), (int) (90 * dens));
		resetrl();
	}
	private void resetrl() {
		//reset location of magnifying window
		rel2.setX(-200 * dens);
		rel2.setY(-200 * dens);
		rel3.setX(rel2.getX());
		rel3.setY(rel2.getY());
	}
	private void mvScreen() {
		//update location of magnifying window
		fixBearing();
		mapf.getMap().moveCamera(
				CameraUpdateFactory.newLatLngZoom(mkr1
						.getPosition(), map.getCameraPosition().zoom + 2));
		rel2.setX(map.getProjection().toScreenLocation(
				mkr1.getPosition()).x
				- 78 * dens);
		rel2.setY(map.getProjection().toScreenLocation(
				mkr1.getPosition()).y
				- 170 * dens);
		rel3.setX(rel2.getX());
		rel3.setY(rel2.getY());
	}
	private Button addbutton(Button btn, int dr, int height, int width,
			float x, float y) {
		//initializing buttons
		btn = new Button(active.getBaseContext());
		btn.setBackgroundResource(dr);
		btn.setHeight(height);
		btn.setWidth(width);
		btn.setX(x);
		btn.setY(y);
		return btn;
	}	
	private void fixBearing(){
		//fix angle of magnifying window's map
		if (mapf.getMap().getCameraPosition().bearing != map
				.getCameraPosition().bearing) {
			mapf.getMap().moveCamera(
					CameraUpdateFactory.newCameraPosition(map.getCameraPosition()));
		}
	}
	private int getNextIndex(int index){
		if(index == mkr.size()-1) index = 0;
		else index++;
		return index;
	}
	private int getPreviousIndex(int index){
		if(index == 0) index = mkr.size()-1;
		else index--;
		return index;
	}
	public int getIndex(){
		int index = 0;
		if(mkr != null){
			if(mkr.size() != 0){
				for(int i = 0;i<mkr.size();i++){
					if(mkr1.equals(mkr.get(i))){
						index = i;
						break;
					}
				}
			}
		}
		return index;
	}
}
