package com.monead.games.android.sequence.ui.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;

/**
 * Copyright 2011, David S. Read
 * 
 * This file is part of Sequence Hunt.
 *
 * Sequence Hunt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sequence Hunt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Sequence Hunt.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * Draw a diamond of maximum dimensions on its canvas
 * 
 * @author David Read
 *
 */
public class DiamondShape extends Shape {

	@Override
	public void draw(Canvas canvas, Paint paint) {
		Path path;
		
		path = new Path();
		path.moveTo(getWidth()/2, 0);
		path.lineTo(0, getHeight()/2);
		path.lineTo(getWidth()/2, getHeight());
		path.lineTo(getWidth(), getHeight()/2);
		path.lineTo(getWidth()/2, 0);
		canvas.drawPath(path, paint);
	}

}
