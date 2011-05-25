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
 * Draw an isosceles triangle of maximum dimensions on its canvas.
 * 
 * @author David Read
 * 
 */
public class TriangleShape extends Shape {

    @Override
    public final void draw(final Canvas canvas, final Paint paint) {
        Path path;

        path = new Path();
        path.moveTo(0, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth() / 2, 0);
        path.lineTo(0, getHeight());
        canvas.drawPath(path, paint);
    }

}
