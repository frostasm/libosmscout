/*
  This source is part of the libosmscout library
  Copyright (C) 2010  Tim Teulings

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*/

package osm.scout;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class MapPainterCanvas {
	
	private Canvas mCanvas=null;
	private Paint mPaint=null;
	
	private int mJniMapPainterIndex;
	
	public MapPainterCanvas() {
		
		mJniMapPainterIndex=jniConstructor();
		
		mPaint=new Paint();
		
		mPaint.setAntiAlias(true);
	}
	
	protected void finalize() throws Throwable {
		
		try {			
			jniDestructor(mJniMapPainterIndex);
		}
		finally {			
			super.finalize();
		}
	}
	
	public boolean drawMap(StyleConfig styleConfig, MercatorProjection projection,
			MapData mapData, Canvas canvas) {
		
		mCanvas=canvas;
		
		return jniDrawMap(mJniMapPainterIndex, styleConfig.getJniObjectIndex(),
                projection.getJniObjectIndex(), mapData.getJniObjectIndex());
	}
	
	public void drawPath(int color, float width, float[] dash,
			boolean roundedStartCap, boolean roundedEndCap,
			float[] x, float[] y) {
		
		Path path=new Path();
		
		path.moveTo(x[0], y[0]);
		
		int numPoints=x.length;
		
		for(int i=0; i<numPoints; i++) {
			
			path.lineTo(x[i], y[i]);
		}
		
		mPaint.setColor(color);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(width);
		
		if ((dash!=null) && (dash.length>=2)) {
			
			DashPathEffect dashPathEffect=new DashPathEffect(dash, 0);		
			mPaint.setPathEffect(dashPathEffect);
		}
		else {
			
			mPaint.setPathEffect(null);
		}
		
		if ((roundedStartCap) && (roundedEndCap)) {
			
			// Both start and end caps are rounded
			mPaint.setStrokeCap(Paint.Cap.ROUND);
		}
		else {
			mPaint.setStrokeCap(Paint.Cap.BUTT);
		}
		
		mCanvas.drawPath(path, mPaint);
		
		if ((roundedStartCap) && (!roundedEndCap)) {
			
			// Only the start cap is rounded
			Path startPath=new Path();
			
			startPath.moveTo(x[0], y[0]);
			startPath.lineTo(x[0], y[0]);
			
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			
			mCanvas.drawPath(startPath, mPaint);			
		}
		
		if ((!roundedStartCap) && (roundedEndCap)) {
			
			// Only the end cap is rounded
			Path endPath=new Path();
			
			endPath.moveTo(x[numPoints-1], y[numPoints-1]);
			endPath.lineTo(x[numPoints-1], y[numPoints-1]);
			
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			
			mCanvas.drawPath(endPath, mPaint);			
		}
		
		mPaint.setPathEffect(null);
	}
	
	public void drawArea(int fillColor, int borderColor, float borderWidth,
			float[] x, float[] y) {
		
		Path areaPath=new Path();
		
		areaPath.moveTo(x[0], y[0]);
		
		int numPoints=x.length;
		
		for(int i=0; i<numPoints; i++) {
			
			areaPath.lineTo(x[i], y[i]);
		}
		
		areaPath.close();
		
		// Draw area fill
		mPaint.setColor(fillColor);
		mPaint.setStyle(Paint.Style.FILL);
					
		mCanvas.drawPath(areaPath, mPaint);
		
		if (borderWidth>0.0) {
			
			// Draw area border
			mPaint.setColor(borderColor);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(borderWidth);
							
			mCanvas.drawPath(areaPath, mPaint);
		}
	}
	
	public void drawArea(int color, float x, float y, float width, float height) {
		
		RectF rect=new RectF(x, y, width, height);
		
		mPaint.setColor(color);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mCanvas.drawRect(rect, mPaint);
	}
	
	// Private native methods
	
	private native int jniConstructor();
	private native void jniDestructor(int mapPainterIndex);
	private native boolean jniDrawMap(int mapPainterIndex, int styleConfigIndex,
                           int projectionIndex, int mapDataIndex);
}