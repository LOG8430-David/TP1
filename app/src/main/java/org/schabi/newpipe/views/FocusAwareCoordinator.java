/*
 * Copyright (C) Eltex ltd 2019 <eltex@eltex-co.ru>
 * FocusAwareCoordinator.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public final class FocusAwareCoordinator extends CoordinatorLayout {
    private final Rect childFocus = new Rect();

    public FocusAwareCoordinator(@NonNull Context context) {
        super(context);
    }

    public FocusAwareCoordinator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusAwareCoordinator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (!isInTouchMode()) {
            if (child instanceof ViewGroup) {
                focused.getHitRect(childFocus);

                ((ViewGroup) child).offsetDescendantRectToMyCoords((View) focused.getParent(), childFocus);
            } else {
                child.getFocusedRect(childFocus);
            }

            requestChildRectangleOnScreen(child, childFocus, false);
        }

        super.requestChildFocus(child, focused);
    }
}
