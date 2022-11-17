/**************************************************************************
 * This file is part of the Nunchuk software (https://nunchuk.io/)        *							          *
 * Copyright (C) 2022 Nunchuk								              *
 *                                                                        *
 * This program is free software; you can redistribute it and/or          *
 * modify it under the terms of the GNU General Public License            *
 * as published by the Free Software Foundation; either version 3         *
 * of the License, or (at your option) any later version.                 *
 *                                                                        *
 * This program is distributed in the hope that it will be useful,        *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 * GNU General Public License for more details.                           *
 *                                                                        *
 * You should have received a copy of the GNU General Public License      *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 *                                                                        *
 **************************************************************************/

package com.nunchuk.android.widget.swipe;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.nunchuk.android.widget.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SwipeLayout extends FrameLayout {
    @Deprecated
    public static final int EMPTY_LAYOUT = -1;
    private static final int DRAG_LEFT = 1;
    private static final int DRAG_RIGHT = 2;
    private static final int DRAG_TOP = 4;
    private static final int DRAG_BOTTOM = 8;
    private static final DragEdge DefaultDragEdge = DragEdge.Right;

    private final int mTouchSlop;

    private DragEdge mCurrentDragEdge = DefaultDragEdge;
    private final ViewDragHelper mDragHelper;

    private int mDragDistance;
    private final LinkedHashMap<DragEdge, View> mDragEdges = new LinkedHashMap<DragEdge, View>();
    private ShowMode mShowMode;

    private final float[] mEdgeSwipesOffset = new float[4];

    private final List<SwipeListener> mSwipeListeners = new ArrayList<SwipeListener>();
    private final List<SwipeDenier> mSwipeDeniers = new ArrayList<SwipeDenier>();
    private final Map<View, ArrayList<OnRevealListener>> mRevealListeners = new HashMap<View, ArrayList<OnRevealListener>>();
    private final Map<View, Boolean> mShowEntirely = new HashMap<View, Boolean>();

    private DoubleClickListener mDoubleClickListener;

    private boolean mSwipeEnabled = true;
    private final boolean[] mSwipesEnabled = new boolean[]{true, true, true, true};
    private boolean mClickToClose;

    public enum DragEdge {
        Left,
        Top,
        Right,
        Bottom
    }

    public enum ShowMode {
        LayDown,
        PullOut
    }

    public SwipeLayout(final Context context) {
        this(context, null);
    }

    public SwipeLayout(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mDragHelper = ViewDragHelper.create(this, mDragHelperCallback);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        final int dragEdgeChoices = a.getInt(R.styleable.SwipeLayout_drag_edge, DRAG_RIGHT);
        mEdgeSwipesOffset[DragEdge.Left.ordinal()] = a.getDimension(R.styleable.SwipeLayout_leftEdgeSwipeOffset, 0);
        mEdgeSwipesOffset[DragEdge.Right.ordinal()] = a.getDimension(R.styleable.SwipeLayout_rightEdgeSwipeOffset, 0);
        mEdgeSwipesOffset[DragEdge.Top.ordinal()] = a.getDimension(R.styleable.SwipeLayout_topEdgeSwipeOffset, 0);
        mEdgeSwipesOffset[DragEdge.Bottom.ordinal()] = a.getDimension(R.styleable.SwipeLayout_bottomEdgeSwipeOffset, 0);
        setClickToClose(a.getBoolean(R.styleable.SwipeLayout_clickToClose, mClickToClose));

        if ((dragEdgeChoices & DRAG_LEFT) == DRAG_LEFT) {
            mDragEdges.put(DragEdge.Left, null);
        }
        if ((dragEdgeChoices & DRAG_TOP) == DRAG_TOP) {
            mDragEdges.put(DragEdge.Top, null);
        }
        if ((dragEdgeChoices & DRAG_RIGHT) == DRAG_RIGHT) {
            mDragEdges.put(DragEdge.Right, null);
        }
        if ((dragEdgeChoices & DRAG_BOTTOM) == DRAG_BOTTOM) {
            mDragEdges.put(DragEdge.Bottom, null);
        }
        final int ordinal = a.getInt(R.styleable.SwipeLayout_show_mode, ShowMode.PullOut.ordinal());
        mShowMode = ShowMode.values()[ordinal];
        a.recycle();

    }

    public interface SwipeListener {
        void onStartOpen(SwipeLayout layout);

        void onOpen(SwipeLayout layout);

        void onStartClose(SwipeLayout layout);

        void onClose(SwipeLayout layout);

        void onUpdate(SwipeLayout layout, int leftOffset, int topOffset);

        void onHandRelease(SwipeLayout layout, float xvel, float yvel);
    }

    public void addSwipeListener(final SwipeListener l) {
        mSwipeListeners.add(l);
    }

    public void removeSwipeListener(final SwipeListener l) {
        mSwipeListeners.remove(l);
    }

    public interface SwipeDenier {
        /*
         * Called in onInterceptTouchEvent Determines if this swipe event should
         * be denied Implement this interface if you are using views with swipe
         * gestures As a child of SwipeLayout
         *
         * @return true deny false allow
         */
        boolean shouldDenySwipe(MotionEvent ev);
    }

    public void addSwipeDenier(final SwipeDenier denier) {
        mSwipeDeniers.add(denier);
    }

    public void removeSwipeDenier(final SwipeDenier denier) {
        mSwipeDeniers.remove(denier);
    }

    public void removeAllSwipeDeniers() {
        mSwipeDeniers.clear();
    }

    public interface OnRevealListener {
        void onReveal(View child, DragEdge edge, float fraction, int distance);
    }

    public void addRevealListener(final int childId, final OnRevealListener l) {
        final View child = findViewById(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child does not belong to SwipeListener.");
        }

        if (!mShowEntirely.containsKey(child)) {
            mShowEntirely.put(child, false);
        }
        if (mRevealListeners.get(child) == null) {
            mRevealListeners.put(child, new ArrayList<OnRevealListener>());
        }

        mRevealListeners.get(child).add(l);
    }

    public void addRevealListener(final int[] childIds, final OnRevealListener l) {
        for (final int i : childIds) {
            addRevealListener(i, l);
        }
    }

    public void removeRevealListener(final int childId, final OnRevealListener l) {
        final View child = findViewById(childId);

        if (child == null) {
            return;
        }

        mShowEntirely.remove(child);
        if (mRevealListeners.containsKey(child)) {
            mRevealListeners.get(child).remove(l);
        }
    }

    public void removeAllRevealListeners(final int childId) {
        final View child = findViewById(childId);
        if (child != null) {
            mRevealListeners.remove(child);
            mShowEntirely.remove(child);
        }
    }

    private final ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {

        @Override
        public int clampViewPositionHorizontal(final View child, final int left, final int dx) {
            if (child == getSurfaceView()) {
                switch (mCurrentDragEdge) {
                    case Top:
                    case Bottom:
                        return getPaddingLeft();
                    case Left:
                        if (left < getPaddingLeft()) {
                            return getPaddingLeft();
                        }
                        if (left > getPaddingLeft() + mDragDistance) {
                            return getPaddingLeft() + mDragDistance;
                        }
                        break;
                    case Right:
                        if (left > getPaddingLeft()) {
                            return getPaddingLeft();
                        }
                        if (left < getPaddingLeft() - mDragDistance) {
                            return getPaddingLeft() - mDragDistance;
                        }
                        break;
                }
            } else if (getCurrentBottomView() == child) {

                switch (mCurrentDragEdge) {
                    case Top:
                    case Bottom:
                        return getPaddingLeft();
                    case Left:
                        if (mShowMode == ShowMode.PullOut) {
                            if (left > getPaddingLeft()) {
                                return getPaddingLeft();
                            }
                        }
                        break;
                    case Right:
                        if (mShowMode == ShowMode.PullOut) {
                            if (left < getMeasuredWidth() - mDragDistance) {
                                return getMeasuredWidth() - mDragDistance;
                            }
                        }
                        break;
                }
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(final View child, final int top, final int dy) {
            if (child == getSurfaceView()) {
                switch (mCurrentDragEdge) {
                    case Left:
                    case Right:
                        return getPaddingTop();
                    case Top:
                        if (top < getPaddingTop()) {
                            return getPaddingTop();
                        }
                        if (top > getPaddingTop() + mDragDistance) {
                            return getPaddingTop() + mDragDistance;
                        }
                        break;
                    case Bottom:
                        if (top < getPaddingTop() - mDragDistance) {
                            return getPaddingTop() - mDragDistance;
                        }
                        if (top > getPaddingTop()) {
                            return getPaddingTop();
                        }
                }
            } else {
                final View surfaceView = getSurfaceView();
                final int surfaceViewTop = surfaceView == null ? 0 : surfaceView.getTop();
                switch (mCurrentDragEdge) {
                    case Left:
                    case Right:
                        return getPaddingTop();
                    case Top:
                        if (mShowMode == ShowMode.PullOut) {
                            if (top > getPaddingTop()) {
                                return getPaddingTop();
                            }
                        } else {
                            if (surfaceViewTop + dy < getPaddingTop()) {
                                return getPaddingTop();
                            }
                            if (surfaceViewTop + dy > getPaddingTop() + mDragDistance) {
                                return getPaddingTop() + mDragDistance;
                            }
                        }
                        break;
                    case Bottom:
                        if (mShowMode == ShowMode.PullOut) {
                            if (top < getMeasuredHeight() - mDragDistance) {
                                return getMeasuredHeight() - mDragDistance;
                            }
                        } else {
                            if (surfaceViewTop + dy >= getPaddingTop()) {
                                return getPaddingTop();
                            }
                            if (surfaceViewTop + dy <= getPaddingTop() - mDragDistance) {
                                return getPaddingTop() - mDragDistance;
                            }
                        }
                }
            }
            return top;
        }

        @Override
        public boolean tryCaptureView(final View child, final int pointerId) {
            final boolean result = child == getSurfaceView() || getBottomViews().contains(child);
            if (result) {
                isCloseBeforeDrag = getOpenStatus() == Status.Close;
            }
            return result;
        }

        @Override
        public int getViewHorizontalDragRange(final View child) {
            return mDragDistance;
        }

        @Override
        public int getViewVerticalDragRange(final View child) {
            return mDragDistance;
        }

        boolean isCloseBeforeDrag = true;

        @Override
        public void onViewReleased(final View releasedChild, final float xvel, final float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            for (final SwipeListener l : mSwipeListeners) {
                l.onHandRelease(SwipeLayout.this, xvel, yvel);
            }
            processHandRelease(xvel, yvel, isCloseBeforeDrag);

            invalidate();
        }

        @Override
        public void onViewPositionChanged(final View changedView, final int left, final int top, final int dx, final int dy) {
            final View surfaceView = getSurfaceView();
            if (surfaceView == null) {
                return;
            }
            final View currentBottomView = getCurrentBottomView();
            final int evLeft = surfaceView.getLeft();
            final int evRight = surfaceView.getRight();
            final int evTop = surfaceView.getTop();
            final int evBottom = surfaceView.getBottom();
            if (changedView == surfaceView) {

                if (mShowMode == ShowMode.PullOut && currentBottomView != null) {
                    if (mCurrentDragEdge == DragEdge.Left || mCurrentDragEdge == DragEdge.Right) {
                        currentBottomView.offsetLeftAndRight(dx);
                    } else {
                        currentBottomView.offsetTopAndBottom(dy);
                    }
                }

            } else if (getBottomViews().contains(changedView)) {

                if (mShowMode == ShowMode.PullOut) {
                    surfaceView.offsetLeftAndRight(dx);
                    surfaceView.offsetTopAndBottom(dy);
                } else {
                    final Rect rect = computeBottomLayDown(mCurrentDragEdge);
                    if (currentBottomView != null) {
                        currentBottomView.layout(rect.left, rect.top, rect.right, rect.bottom);
                    }

                    int newLeft = surfaceView.getLeft() + dx, newTop = surfaceView.getTop() + dy;

                    if (mCurrentDragEdge == DragEdge.Left && newLeft < getPaddingLeft()) {
                        newLeft = getPaddingLeft();
                    } else if (mCurrentDragEdge == DragEdge.Right && newLeft > getPaddingLeft()) {
                        newLeft = getPaddingLeft();
                    } else if (mCurrentDragEdge == DragEdge.Top && newTop < getPaddingTop()) {
                        newTop = getPaddingTop();
                    } else if (mCurrentDragEdge == DragEdge.Bottom && newTop > getPaddingTop()) {
                        newTop = getPaddingTop();
                    }

                    surfaceView.layout(newLeft, newTop, newLeft + getMeasuredWidth(), newTop + getMeasuredHeight());
                }
            }

            dispatchRevealEvent(evLeft, evTop, evRight, evBottom);

            dispatchSwipeEvent(evLeft, evTop, dx, dy);

            invalidate();
        }
    };

    /**
     * the dispatchRevealEvent method may not always get accurate position, it
     * makes the view may not always get the event when the view is totally
     * show( fraction = 1), so , we need to calculate every time.
     */
    protected boolean isViewTotallyFirstShowed(final View child, final Rect relativePosition, final DragEdge edge, final int surfaceLeft,
                                               final int surfaceTop, final int surfaceRight, final int surfaceBottom) {
        if (mShowEntirely.get(child)) {
            return false;
        }
        final int childLeft = relativePosition.left;
        final int childRight = relativePosition.right;
        final int childTop = relativePosition.top;
        final int childBottom = relativePosition.bottom;
        boolean r = false;
        if (getShowMode() == ShowMode.LayDown) {
            if ((edge == DragEdge.Right && surfaceRight <= childLeft)
                    || (edge == DragEdge.Left && surfaceLeft >= childRight)
                    || (edge == DragEdge.Top && surfaceTop >= childBottom)
                    || (edge == DragEdge.Bottom && surfaceBottom <= childTop)) {
                r = true;
            }
        } else if (getShowMode() == ShowMode.PullOut) {
            if ((edge == DragEdge.Right && childRight <= getWidth())
                    || (edge == DragEdge.Left && childLeft >= getPaddingLeft())
                    || (edge == DragEdge.Top && childTop >= getPaddingTop())
                    || (edge == DragEdge.Bottom && childBottom <= getHeight())) {
                r = true;
            }
        }
        return r;
    }

    protected boolean isViewShowing(final View child, final Rect relativePosition, final DragEdge availableEdge, final int surfaceLeft,
                                    final int surfaceTop, final int surfaceRight, final int surfaceBottom) {
        final int childLeft = relativePosition.left;
        final int childRight = relativePosition.right;
        final int childTop = relativePosition.top;
        final int childBottom = relativePosition.bottom;
        if (getShowMode() == ShowMode.LayDown) {
            switch (availableEdge) {
                case Right:
                    if (surfaceRight > childLeft && surfaceRight <= childRight) {
                        return true;
                    }
                    break;
                case Left:
                    if (surfaceLeft < childRight && surfaceLeft >= childLeft) {
                        return true;
                    }
                    break;
                case Top:
                    if (surfaceTop >= childTop && surfaceTop < childBottom) {
                        return true;
                    }
                    break;
                case Bottom:
                    if (surfaceBottom > childTop && surfaceBottom <= childBottom) {
                        return true;
                    }
                    break;
            }
        } else if (getShowMode() == ShowMode.PullOut) {
            switch (availableEdge) {
                case Right:
                    if (childLeft <= getWidth() && childRight > getWidth()) {
                        return true;
                    }
                    break;
                case Left:
                    if (childRight >= getPaddingLeft() && childLeft < getPaddingLeft()) {
                        return true;
                    }
                    break;
                case Top:
                    if (childTop < getPaddingTop() && childBottom >= getPaddingTop()) {
                        return true;
                    }
                    break;
                case Bottom:
                    if (childTop < getHeight() && childTop >= getPaddingTop()) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    protected Rect getRelativePosition(final View child) {
        View t = child;
        final Rect r = new Rect(t.getLeft(), t.getTop(), 0, 0);
        while (t.getParent() != null && t != getRootView()) {
            t = (View) t.getParent();
            if (t == this) {
                break;
            }
            r.left += t.getLeft();
            r.top += t.getTop();
        }
        r.right = r.left + child.getMeasuredWidth();
        r.bottom = r.top + child.getMeasuredHeight();
        return r;
    }

    private int mEventCounter;

    protected void dispatchSwipeEvent(final int surfaceLeft, final int surfaceTop, final int dx, final int dy) {
        final DragEdge edge = getDragEdge();
        boolean open = true;
        if (edge == DragEdge.Left) {
            if (dx < 0) {
                open = false;
            }
        } else if (edge == DragEdge.Right) {
            if (dx > 0) {
                open = false;
            }
        } else if (edge == DragEdge.Top) {
            if (dy < 0) {
                open = false;
            }
        } else if (edge == DragEdge.Bottom) {
            if (dy > 0) {
                open = false;
            }
        }

        dispatchSwipeEvent(surfaceLeft, surfaceTop, open);
    }

    protected void dispatchSwipeEvent(final int surfaceLeft, final int surfaceTop, final boolean open) {
        safeBottomView();
        final Status status = getOpenStatus();

        if (!mSwipeListeners.isEmpty()) {
            mEventCounter++;
            for (final SwipeListener l : mSwipeListeners) {
                if (mEventCounter == 1) {
                    if (open) {
                        l.onStartOpen(this);
                    } else {
                        l.onStartClose(this);
                    }
                }
                l.onUpdate(SwipeLayout.this, surfaceLeft - getPaddingLeft(), surfaceTop - getPaddingTop());
            }

            if (status == Status.Close) {
                for (final SwipeListener l : mSwipeListeners) {
                    l.onClose(SwipeLayout.this);
                }
                mEventCounter = 0;
            }

            if (status == Status.Open) {
                final View currentBottomView = getCurrentBottomView();
                if (currentBottomView != null) {
                    currentBottomView.setEnabled(true);
                }
                for (final SwipeListener l : mSwipeListeners) {
                    l.onOpen(SwipeLayout.this);
                }
                mEventCounter = 0;
            }
        }
    }

    /**
     * prevent bottom view get any touch event. Especially in LayDown mode.
     */
    private void safeBottomView() {
        final Status status = getOpenStatus();
        final List<View> bottoms = getBottomViews();

        if (status == Status.Close) {
            for (final View bottom : bottoms) {
                if (bottom != null && bottom.getVisibility() != INVISIBLE) {
                    bottom.setVisibility(INVISIBLE);
                }
            }
        } else {
            final View currentBottomView = getCurrentBottomView();
            if (currentBottomView != null && currentBottomView.getVisibility() != VISIBLE) {
                currentBottomView.setVisibility(VISIBLE);
            }
        }
    }

    protected void dispatchRevealEvent(final int surfaceLeft, final int surfaceTop, final int surfaceRight,
                                       final int surfaceBottom) {
        if (mRevealListeners.isEmpty()) {
            return;
        }
        for (final Map.Entry<View, ArrayList<OnRevealListener>> entry : mRevealListeners.entrySet()) {
            final View child = entry.getKey();
            final Rect rect = getRelativePosition(child);
            if (isViewShowing(child, rect, mCurrentDragEdge, surfaceLeft, surfaceTop,
                    surfaceRight, surfaceBottom)) {
                mShowEntirely.put(child, false);
                int distance = 0;
                float fraction = 0f;
                if (getShowMode() == ShowMode.LayDown) {
                    switch (mCurrentDragEdge) {
                        case Left:
                            distance = rect.left - surfaceLeft;
                            fraction = distance / (float) child.getWidth();
                            break;
                        case Right:
                            distance = rect.right - surfaceRight;
                            fraction = distance / (float) child.getWidth();
                            break;
                        case Top:
                            distance = rect.top - surfaceTop;
                            fraction = distance / (float) child.getHeight();
                            break;
                        case Bottom:
                            distance = rect.bottom - surfaceBottom;
                            fraction = distance / (float) child.getHeight();
                            break;
                    }
                } else if (getShowMode() == ShowMode.PullOut) {
                    switch (mCurrentDragEdge) {
                        case Left:
                            distance = rect.right - getPaddingLeft();
                            fraction = distance / (float) child.getWidth();
                            break;
                        case Right:
                            distance = rect.left - getWidth();
                            fraction = distance / (float) child.getWidth();
                            break;
                        case Top:
                            distance = rect.bottom - getPaddingTop();
                            fraction = distance / (float) child.getHeight();
                            break;
                        case Bottom:
                            distance = rect.top - getHeight();
                            fraction = distance / (float) child.getHeight();
                            break;
                    }
                }

                for (final OnRevealListener l : entry.getValue()) {
                    l.onReveal(child, mCurrentDragEdge, Math.abs(fraction), distance);
                    if (Math.abs(fraction) == 1) {
                        mShowEntirely.put(child, true);
                    }
                }
            }

            if (isViewTotallyFirstShowed(child, rect, mCurrentDragEdge, surfaceLeft, surfaceTop,
                    surfaceRight, surfaceBottom)) {
                mShowEntirely.put(child, true);
                for (final OnRevealListener l : entry.getValue()) {
                    if (mCurrentDragEdge == DragEdge.Left
                            || mCurrentDragEdge == DragEdge.Right) {
                        l.onReveal(child, mCurrentDragEdge, 1, child.getWidth());
                    } else {
                        l.onReveal(child, mCurrentDragEdge, 1, child.getHeight());
                    }
                }
            }

        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * {@link OnLayoutChangeListener} added in API 11. I need
     * to support it from API 8.
     */
    public interface OnLayout {
        void onLayout(SwipeLayout v);
    }

    private List<OnLayout> mOnLayoutListeners;

    public void addOnLayoutListener(final OnLayout l) {
        if (mOnLayoutListeners == null) {
            mOnLayoutListeners = new ArrayList<OnLayout>();
        }
        mOnLayoutListeners.add(l);
    }

    public void removeOnLayoutListener(final OnLayout l) {
        if (mOnLayoutListeners != null) {
            mOnLayoutListeners.remove(l);
        }
    }

    public void addDrag(final DragEdge dragEdge, final View child) {
        addDrag(dragEdge, child, null);
    }

    public void addDrag(final DragEdge dragEdge, final View child, ViewGroup.LayoutParams params) {
        if (params == null) {
            params = generateDefaultLayoutParams();
        }
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        int gravity = -1;
        switch (dragEdge) {
            case Left:
                gravity = Gravity.LEFT;
                break;
            case Right:
                gravity = Gravity.RIGHT;
                break;
            case Top:
                gravity = Gravity.TOP;
                break;
            case Bottom:
                gravity = Gravity.BOTTOM;
                break;
        }
        if (params instanceof LayoutParams) {
            ((LayoutParams) params).gravity = gravity;
        }
        addView(child, 0, params);
    }

    @Override
    public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
        int gravity = Gravity.NO_GRAVITY;
        try {
            gravity = (Integer) params.getClass().getField("gravity").get(params);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (gravity > 0) {
            gravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));

            if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
                mDragEdges.put(DragEdge.Left, child);
            }
            if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
                mDragEdges.put(DragEdge.Right, child);
            }
            if ((gravity & Gravity.TOP) == Gravity.TOP) {
                mDragEdges.put(DragEdge.Top, child);
            }
            if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
                mDragEdges.put(DragEdge.Bottom, child);
            }
        } else {
            for (final Map.Entry<DragEdge, View> entry : mDragEdges.entrySet()) {
                if (entry.getValue() == null) {
                    //means used the drag_edge attr, the no gravity child should be use set
                    mDragEdges.put(entry.getKey(), child);
                    break;
                }
            }
        }
        if (child == null || child.getParent() == this) {
            return;
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        updateBottomViews();

        if (mOnLayoutListeners != null) {
            for (int i = 0; i < mOnLayoutListeners.size(); i++) {
                mOnLayoutListeners.get(i).onLayout(this);
            }
        }
    }

    void layoutPullOut() {
        Rect rect = computeSurfaceLayoutArea(false);
        final View surfaceView = getSurfaceView();
        if (surfaceView != null) {
            surfaceView.layout(rect.left, rect.top, rect.right, rect.bottom);
            bringChildToFront(surfaceView);
        }
        rect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
        final View currentBottomView = getCurrentBottomView();
        if (currentBottomView != null) {
            currentBottomView.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    void layoutLayDown() {
        Rect rect = computeSurfaceLayoutArea(false);
        final View surfaceView = getSurfaceView();
        if (surfaceView != null) {
            surfaceView.layout(rect.left, rect.top, rect.right, rect.bottom);
            bringChildToFront(surfaceView);
        }
        rect = computeBottomLayoutAreaViaSurface(ShowMode.LayDown, rect);
        final View currentBottomView = getCurrentBottomView();
        if (currentBottomView != null) {
            currentBottomView.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    private boolean mIsBeingDragged;

    private void checkCanDrag(final MotionEvent ev) {
        if (mIsBeingDragged) {
            return;
        }
        if (getOpenStatus() == Status.Middle) {
            mIsBeingDragged = true;
            return;
        }
        final Status status = getOpenStatus();
        final float distanceX = ev.getRawX() - sX;
        final float distanceY = ev.getRawY() - sY;
        float angle = Math.abs(distanceY / distanceX);
        angle = (float) Math.toDegrees(Math.atan(angle));
        if (getOpenStatus() == Status.Close) {
            final DragEdge dragEdge;
            if (angle < 45) {
                if (distanceX > 0 && isLeftSwipeEnabled()) {
                    dragEdge = DragEdge.Left;
                } else if (distanceX < 0 && isRightSwipeEnabled()) {
                    dragEdge = DragEdge.Right;
                } else {
                    return;
                }

            } else {
                if (distanceY > 0 && isTopSwipeEnabled()) {
                    dragEdge = DragEdge.Top;
                } else if (distanceY < 0 && isBottomSwipeEnabled()) {
                    dragEdge = DragEdge.Bottom;
                } else {
                    return;
                }
            }
            setCurrentDragEdge(dragEdge);
        }

        boolean doNothing = false;
        if (mCurrentDragEdge == DragEdge.Right) {
            boolean suitable = (status == Status.Open && distanceX > mTouchSlop)
                    || (status == Status.Close && distanceX < -mTouchSlop);
            suitable = suitable || (status == Status.Middle);

            if (angle > 30 || !suitable) {
                doNothing = true;
            }
        }

        if (mCurrentDragEdge == DragEdge.Left) {
            boolean suitable = (status == Status.Open && distanceX < -mTouchSlop)
                    || (status == Status.Close && distanceX > mTouchSlop);
            suitable = suitable || status == Status.Middle;

            if (angle > 30 || !suitable) {
                doNothing = true;
            }
        }

        if (mCurrentDragEdge == DragEdge.Top) {
            boolean suitable = (status == Status.Open && distanceY < -mTouchSlop)
                    || (status == Status.Close && distanceY > mTouchSlop);
            suitable = suitable || status == Status.Middle;

            if (angle < 60 || !suitable) {
                doNothing = true;
            }
        }

        if (mCurrentDragEdge == DragEdge.Bottom) {
            boolean suitable = (status == Status.Open && distanceY > mTouchSlop)
                    || (status == Status.Close && distanceY < -mTouchSlop);
            suitable = suitable || status == Status.Middle;

            if (angle < 60 || !suitable) {
                doNothing = true;
            }
        }
        mIsBeingDragged = !doNothing;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        if (!isSwipeEnabled()) {
            return false;
        }
        if (mClickToClose && getOpenStatus() == Status.Open && isTouchOnSurface(ev)) {
            return true;
        }
        for (final SwipeDenier denier : mSwipeDeniers) {
            if (denier != null && denier.shouldDenySwipe(ev)) {
                return false;
            }
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragHelper.processTouchEvent(ev);
                mIsBeingDragged = false;
                sX = ev.getRawX();
                sY = ev.getRawY();
                //if the swipe is in middle state(scrolling), should intercept the touch
                if (getOpenStatus() == Status.Middle) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final boolean beforeCheck = mIsBeingDragged;
                checkCanDrag(ev);
                if (mIsBeingDragged) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (!beforeCheck && mIsBeingDragged) {
                    //let children has one chance to catch the touch, and request the swipe not intercept
                    //useful when swipeLayout wrap a swipeLayout or other gestural layout
                    return false;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                mDragHelper.processTouchEvent(ev);
                break;
            default://handle other action, such as ACTION_POINTER_DOWN/UP
                mDragHelper.processTouchEvent(ev);
        }
        return mIsBeingDragged;
    }

    private float sX = -1, sY = -1;

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!isSwipeEnabled()) {
            return super.onTouchEvent(event);
        }

        final int action = event.getActionMasked();
        gestureDetector.onTouchEvent(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragHelper.processTouchEvent(event);
                sX = event.getRawX();
                sY = event.getRawY();


            case MotionEvent.ACTION_MOVE: {
                //the drag state and the direction are already judged at onInterceptTouchEvent
                checkCanDrag(event);
                if (mIsBeingDragged) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mDragHelper.processTouchEvent(event);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mDragHelper.processTouchEvent(event);
                break;

            default://handle other action, such as ACTION_POINTER_DOWN/UP
                mDragHelper.processTouchEvent(event);
        }

        return super.onTouchEvent(event) || mIsBeingDragged || action == MotionEvent.ACTION_DOWN;
    }

    public boolean isClickToClose() {
        return mClickToClose;
    }

    public void setClickToClose(final boolean mClickToClose) {
        this.mClickToClose = mClickToClose;
    }

    public void setSwipeEnabled(final boolean enabled) {
        mSwipeEnabled = enabled;
    }

    public boolean isSwipeEnabled() {
        return mSwipeEnabled;
    }

    public boolean isLeftSwipeEnabled() {
        final View bottomView = mDragEdges.get(DragEdge.Left);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Left.ordinal()];
    }

    public void setLeftSwipeEnabled(final boolean leftSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Left.ordinal()] = leftSwipeEnabled;
    }

    public boolean isRightSwipeEnabled() {
        final View bottomView = mDragEdges.get(DragEdge.Right);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Right.ordinal()];
    }

    public void setRightSwipeEnabled(final boolean rightSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Right.ordinal()] = rightSwipeEnabled;
    }

    public boolean isTopSwipeEnabled() {
        final View bottomView = mDragEdges.get(DragEdge.Top);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Top.ordinal()];
    }

    public void setTopSwipeEnabled(final boolean topSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Top.ordinal()] = topSwipeEnabled;
    }

    public boolean isBottomSwipeEnabled() {
        final View bottomView = mDragEdges.get(DragEdge.Bottom);
        return bottomView != null && bottomView.getParent() == this
                && bottomView != getSurfaceView() && mSwipesEnabled[DragEdge.Bottom.ordinal()];
    }

    public void setBottomSwipeEnabled(final boolean bottomSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Bottom.ordinal()] = bottomSwipeEnabled;
    }

    private boolean insideAdapterView() {
        return getAdapterView() != null;
    }

    private AdapterView getAdapterView() {
        final ViewParent t = getParent();
        if (t instanceof AdapterView) {
            return (AdapterView) t;
        }
        return null;
    }

    private void performAdapterViewItemClick() {
        if (getOpenStatus() != Status.Close) {
            return;
        }
        final ViewParent t = getParent();
        if (t instanceof AdapterView) {
            final AdapterView view = (AdapterView) t;
            final int p = view.getPositionForView(SwipeLayout.this);
            if (p != AdapterView.INVALID_POSITION) {
                view.performItemClick(view.getChildAt(p - view.getFirstVisiblePosition()), p, view
                        .getAdapter().getItemId(p));
            }
        }
    }

    private boolean performAdapterViewItemLongClick() {
        if (getOpenStatus() != Status.Close) {
            return false;
        }
        final ViewParent t = getParent();
        if (t instanceof AdapterView) {
            final AdapterView view = (AdapterView) t;
            final int p = view.getPositionForView(SwipeLayout.this);
            if (p == AdapterView.INVALID_POSITION) {
                return false;
            }
            final long vId = view.getItemIdAtPosition(p);
            boolean handled = false;
            try {
                final Method m = AbsListView.class.getDeclaredMethod("performLongPress", View.class, int.class, long.class);
                m.setAccessible(true);
                handled = (boolean) m.invoke(view, SwipeLayout.this, p, vId);

            } catch (final Exception e) {
                e.printStackTrace();

                if (view.getOnItemLongClickListener() != null) {
                    handled = view.getOnItemLongClickListener().onItemLongClick(view, SwipeLayout.this, p, vId);
                }
                if (handled) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
            return handled;
        }
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (insideAdapterView()) {
            if (clickListener == null) {
                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        performAdapterViewItemClick();
                    }
                });
            }
            if (longClickListener == null) {
                setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        performAdapterViewItemLongClick();
                        return true;
                    }
                });
            }
        }
    }

    OnClickListener clickListener;

    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(l);
        clickListener = l;
    }

    OnLongClickListener longClickListener;

    @Override
    public void setOnLongClickListener(final OnLongClickListener l) {
        super.setOnLongClickListener(l);
        longClickListener = l;
    }

    private Rect hitSurfaceRect;

    private boolean isTouchOnSurface(final MotionEvent ev) {
        final View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return false;
        }
        if (hitSurfaceRect == null) {
            hitSurfaceRect = new Rect();
        }
        surfaceView.getHitRect(hitSurfaceRect);
        return hitSurfaceRect.contains((int) ev.getX(), (int) ev.getY());
    }

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new SwipeDetector());

    class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            if (mClickToClose && isTouchOnSurface(e)) {
                close();
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (mDoubleClickListener != null) {
                final View target;
                final View bottom = getCurrentBottomView();
                final View surface = getSurfaceView();
                if (bottom != null && e.getX() > bottom.getLeft() && e.getX() < bottom.getRight()
                        && e.getY() > bottom.getTop() && e.getY() < bottom.getBottom()) {
                    target = bottom;
                } else {
                    target = surface;
                }
                mDoubleClickListener.onDoubleClick(SwipeLayout.this, target == surface);
            }
            return true;
        }
    }

    /**
     * set the drag distance, it will force set the bottom view's width or
     * height via this value.
     *
     * @param max max distance in dp unit
     */
    public void setDragDistance(int max) {
        if (max < 0) {
            max = 0;
        }
        mDragDistance = dp2px(max);
        requestLayout();
    }

    public void setShowMode(final ShowMode mode) {
        mShowMode = mode;
        requestLayout();
    }

    public DragEdge getDragEdge() {
        return mCurrentDragEdge;
    }

    public int getDragDistance() {
        return mDragDistance;
    }

    public ShowMode getShowMode() {
        return mShowMode;
    }

    /**
     * return null if there is no surface view(no children)
     */
    public View getSurfaceView() {
        if (getChildCount() == 0) {
            return null;
        }
        return getChildAt(getChildCount() - 1);
    }

    /**
     * return null if there is no bottom view
     */
    @Nullable
    public View getCurrentBottomView() {
        final List<View> bottoms = getBottomViews();
        if (mCurrentDragEdge.ordinal() < bottoms.size()) {
            return bottoms.get(mCurrentDragEdge.ordinal());
        }
        return null;
    }

    /**
     * @return all bottomViews: left, top, right, bottom (may null if the edge is not set)
     */
    public List<View> getBottomViews() {
        final ArrayList<View> bottoms = new ArrayList<View>();
        for (final DragEdge dragEdge : DragEdge.values()) {
            bottoms.add(mDragEdges.get(dragEdge));
        }
        return bottoms;
    }

    public enum Status {
        Middle,
        Open,
        Close
    }

    public Status getOpenStatus() {
        final View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return Status.Close;
        }
        final int surfaceLeft = surfaceView.getLeft();
        final int surfaceTop = surfaceView.getTop();
        if (surfaceLeft == getPaddingLeft() && surfaceTop == getPaddingTop() && !mIsBeingDragged) {
            return Status.Close;
        }

        if (surfaceLeft == (getPaddingLeft() - mDragDistance) || surfaceLeft == (getPaddingLeft() + mDragDistance)
                || surfaceTop == (getPaddingTop() - mDragDistance) || surfaceTop == (getPaddingTop() + mDragDistance)) {
            return Status.Open;
        }

        return Status.Middle;
    }


    /**
     * Process the surface release event.
     *
     * @param xvel                 xVelocity
     * @param yvel                 yVelocity
     * @param isCloseBeforeDragged the open state before drag
     */
    protected void processHandRelease(final float xvel, final float yvel, final boolean isCloseBeforeDragged) {
        final float minVelocity = mDragHelper.getMinVelocity();
        final View surfaceView = getSurfaceView();
        final DragEdge currentDragEdge = mCurrentDragEdge;
        if (currentDragEdge == null || surfaceView == null) {
            return;
        }
        final float willOpenPercent = (isCloseBeforeDragged ? .25f : .75f);
        if (currentDragEdge == DragEdge.Left) {
            if (xvel > minVelocity) {
                open();
            } else if (xvel < -minVelocity) {
                close();
            } else {
                final float openPercent = 1f * getSurfaceView().getLeft() / mDragDistance;
                if (openPercent > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            }
        } else if (currentDragEdge == DragEdge.Right) {
            if (xvel > minVelocity) {
                close();
            } else if (xvel < -minVelocity) {
                open();
            } else {
                final float openPercent = 1f * (-getSurfaceView().getLeft()) / mDragDistance;
                if (openPercent > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            }
        } else if (currentDragEdge == DragEdge.Top) {
            if (yvel > minVelocity) {
                open();
            } else if (yvel < -minVelocity) {
                close();
            } else {
                final float openPercent = 1f * getSurfaceView().getTop() / mDragDistance;
                if (openPercent > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            }
        } else if (currentDragEdge == DragEdge.Bottom) {
            if (yvel > minVelocity) {
                close();
            } else if (yvel < -minVelocity) {
                open();
            } else {
                final float openPercent = 1f * (-getSurfaceView().getTop()) / mDragDistance;
                if (openPercent > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            }
        }
    }

    /**
     * smoothly open surface.
     */
    public void open() {
        open(true, true);
    }

    public void open(final boolean smooth) {
        open(smooth, true);
    }

    public void open(final boolean smooth, final boolean notify) {
        final View surface = getSurfaceView();
        final View bottom = getCurrentBottomView();
        if (surface == null) {
            return;
        }
        final int dx;
        final int dy;
        final Rect rect = computeSurfaceLayoutArea(true);
        if (smooth) {
            mDragHelper.smoothSlideViewTo(surface, rect.left, rect.top);
        } else {
            dx = rect.left - surface.getLeft();
            dy = rect.top - surface.getTop();
            surface.layout(rect.left, rect.top, rect.right, rect.bottom);
            if (getShowMode() == ShowMode.PullOut) {
                final Rect bRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
                if (bottom != null) {
                    bottom.layout(bRect.left, bRect.top, bRect.right, bRect.bottom);
                }
            }
            if (notify) {
                dispatchRevealEvent(rect.left, rect.top, rect.right, rect.bottom);
                dispatchSwipeEvent(rect.left, rect.top, dx, dy);
            } else {
                safeBottomView();
            }
        }
        invalidate();
    }

    public void open(final DragEdge edge) {
        setCurrentDragEdge(edge);
        open(true, true);
    }

    public void open(final boolean smooth, final DragEdge edge) {
        setCurrentDragEdge(edge);
        open(smooth, true);
    }

    public void open(final boolean smooth, final boolean notify, final DragEdge edge) {
        setCurrentDragEdge(edge);
        open(smooth, notify);
    }

    /**
     * smoothly close surface.
     */
    public void close() {
        close(true, true);
    }

    public void close(final boolean smooth) {
        close(smooth, true);
    }

    /**
     * close surface
     *
     * @param smooth smoothly or not.
     * @param notify if notify all the listeners.
     */
    public void close(final boolean smooth, final boolean notify) {
        final View surface = getSurfaceView();
        if (surface == null) {
            return;
        }
        final int dx;
        final int dy;
        if (smooth) {
            mDragHelper.smoothSlideViewTo(getSurfaceView(), getPaddingLeft(), getPaddingTop());
        } else {
            final Rect rect = computeSurfaceLayoutArea(false);
            dx = rect.left - surface.getLeft();
            dy = rect.top - surface.getTop();
            surface.layout(rect.left, rect.top, rect.right, rect.bottom);
            if (notify) {
                dispatchRevealEvent(rect.left, rect.top, rect.right, rect.bottom);
                dispatchSwipeEvent(rect.left, rect.top, dx, dy);
            } else {
                safeBottomView();
            }
        }
        invalidate();
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(final boolean smooth) {
        if (getOpenStatus() == Status.Open) {
            close(smooth);
        } else if (getOpenStatus() == Status.Close) {
            open(smooth);
        }
    }


    /**
     * a helper function to compute the Rect area that surface will hold in.
     *
     * @param open open status or close status.
     */
    private Rect computeSurfaceLayoutArea(final boolean open) {
        int l = getPaddingLeft(), t = getPaddingTop();
        if (open) {
            if (mCurrentDragEdge == DragEdge.Left) {
                l = getPaddingLeft() + mDragDistance;
            } else if (mCurrentDragEdge == DragEdge.Right) {
                l = getPaddingLeft() - mDragDistance;
            } else if (mCurrentDragEdge == DragEdge.Top) {
                t = getPaddingTop() + mDragDistance;
            } else {
                t = getPaddingTop() - mDragDistance;
            }
        }
        return new Rect(l, t, l + getMeasuredWidth(), t + getMeasuredHeight());
    }

    private Rect computeBottomLayoutAreaViaSurface(final ShowMode mode, final Rect surfaceArea) {
        final Rect rect = surfaceArea;
        final View bottomView = getCurrentBottomView();

        int bl = rect.left, bt = rect.top, br = rect.right, bb = rect.bottom;
        if (mode == ShowMode.PullOut) {
            if (mCurrentDragEdge == DragEdge.Left) {
                bl = rect.left - mDragDistance;
            } else if (mCurrentDragEdge == DragEdge.Right) {
                bl = rect.right;
            } else if (mCurrentDragEdge == DragEdge.Top) {
                bt = rect.top - mDragDistance;
            } else {
                bt = rect.bottom;
            }

            if (mCurrentDragEdge == DragEdge.Left || mCurrentDragEdge == DragEdge.Right) {
                bb = rect.bottom;
                br = bl + (bottomView == null ? 0 : bottomView.getMeasuredWidth());
            } else {
                bb = bt + (bottomView == null ? 0 : bottomView.getMeasuredHeight());
                br = rect.right;
            }
        } else if (mode == ShowMode.LayDown) {
            if (mCurrentDragEdge == DragEdge.Left) {
                br = bl + mDragDistance;
            } else if (mCurrentDragEdge == DragEdge.Right) {
                bl = br - mDragDistance;
            } else if (mCurrentDragEdge == DragEdge.Top) {
                bb = bt + mDragDistance;
            } else {
                bt = bb - mDragDistance;
            }

        }
        return new Rect(bl, bt, br, bb);

    }

    private Rect computeBottomLayDown(final DragEdge dragEdge) {
        int bl = getPaddingLeft(), bt = getPaddingTop();
        final int br;
        final int bb;
        if (dragEdge == DragEdge.Right) {
            bl = getMeasuredWidth() - mDragDistance;
        } else if (dragEdge == DragEdge.Bottom) {
            bt = getMeasuredHeight() - mDragDistance;
        }
        if (dragEdge == DragEdge.Left || dragEdge == DragEdge.Right) {
            br = bl + mDragDistance;
            bb = bt + getMeasuredHeight();
        } else {
            br = bl + getMeasuredWidth();
            bb = bt + mDragDistance;
        }
        return new Rect(bl, bt, br, bb);
    }

    public void setOnDoubleClickListener(final DoubleClickListener doubleClickListener) {
        mDoubleClickListener = doubleClickListener;
    }

    public interface DoubleClickListener {
        void onDoubleClick(SwipeLayout layout, boolean surface);
    }

    private int dp2px(final float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }


    /**
     * Deprecated, use {@link #addDrag(DragEdge, View)}
     */
    @Deprecated
    public void setDragEdge(final DragEdge dragEdge) {
        if (getChildCount() >= 2) {
            mDragEdges.put(dragEdge, getChildAt(getChildCount() - 2));
        }
        setCurrentDragEdge(dragEdge);
    }

    @Override
    public void onViewRemoved(final View child) {
        for (final Map.Entry<DragEdge, View> entry : new HashMap<DragEdge, View>(mDragEdges).entrySet()) {
            if (entry.getValue() == child) {
                mDragEdges.remove(entry.getKey());
            }
        }
    }

    public Map<DragEdge, View> getDragEdgeMap() {
        return mDragEdges;
    }

    /**
     * Deprecated, use {@link #getDragEdgeMap()}
     */
    @Deprecated
    public List<DragEdge> getDragEdges() {
        return new ArrayList<DragEdge>(mDragEdges.keySet());
    }

    /**
     * Deprecated, use {@link #addDrag(DragEdge, View)}
     */
    @Deprecated
    public void setDragEdges(final List<DragEdge> dragEdges) {
        for (int i = 0, size = Math.min(dragEdges.size(), getChildCount() - 1); i < size; i++) {
            final DragEdge dragEdge = dragEdges.get(i);
            mDragEdges.put(dragEdge, getChildAt(i));
        }
        if (dragEdges.size() == 0 || dragEdges.contains(DefaultDragEdge)) {
            setCurrentDragEdge(DefaultDragEdge);
        } else {
            setCurrentDragEdge(dragEdges.get(0));
        }
    }

    /**
     * Deprecated, use {@link #addDrag(DragEdge, View)}
     */
    @Deprecated
    public void setDragEdges(final DragEdge... mDragEdges) {
        setDragEdges(Arrays.asList(mDragEdges));
    }

    /**
     * Deprecated, use {@link #addDrag(DragEdge, View)}
     * When using multiple drag edges it's a good idea to pass the ids of the views that
     * you're using for the left, right, top bottom views (-1 if you're not using a particular view)
     */
    @Deprecated
    public void setBottomViewIds(final int leftId, final int rightId, final int topId, final int bottomId) {
        addDrag(DragEdge.Left, findViewById(leftId));
        addDrag(DragEdge.Right, findViewById(rightId));
        addDrag(DragEdge.Top, findViewById(topId));
        addDrag(DragEdge.Bottom, findViewById(bottomId));
    }

    private float getCurrentOffset() {
        if (mCurrentDragEdge == null) {
            return 0;
        }
        return mEdgeSwipesOffset[mCurrentDragEdge.ordinal()];
    }

    private void setCurrentDragEdge(final DragEdge dragEdge) {
        if (mCurrentDragEdge != dragEdge) {
            mCurrentDragEdge = dragEdge;
            updateBottomViews();
        }
    }

    private void updateBottomViews() {
        final View currentBottomView = getCurrentBottomView();
        if (currentBottomView != null) {
            if (mCurrentDragEdge == DragEdge.Left || mCurrentDragEdge == DragEdge.Right) {
                mDragDistance = currentBottomView.getMeasuredWidth() - dp2px(getCurrentOffset());
            } else {
                mDragDistance = currentBottomView.getMeasuredHeight() - dp2px(getCurrentOffset());
            }
        }

        if (mShowMode == ShowMode.PullOut) {
            layoutPullOut();
        } else if (mShowMode == ShowMode.LayDown) {
            layoutLayDown();
        }

        safeBottomView();
    }
}
