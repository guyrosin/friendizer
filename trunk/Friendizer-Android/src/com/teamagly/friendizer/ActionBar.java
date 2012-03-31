/*
 * Copyright (C) 2010 Johan Nilsson <http://markupartist.com> Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.teamagly.friendizer;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActionBar extends RelativeLayout implements OnClickListener {

    private LayoutInflater mInflater;
    private RelativeLayout mBarView;
    private TextView mTitleView;
    private LinearLayout mActionsView;
    private ImageButton mHomeBtn;
    ImageButton mRefreshBtn;
    private ProgressBar mProgress;

    public ActionBar(Context context, AttributeSet attrs) {
	super(context, attrs);

	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	mBarView = (RelativeLayout) mInflater.inflate(R.layout.actionbar, null);
	addView(mBarView);

	mHomeBtn = (ImageButton) mBarView.findViewById(R.id.actionbar_home_btn);

	mTitleView = (TextView) mBarView.findViewById(R.id.actionbar_title);

	mRefreshBtn = (ImageButton) mBarView.findViewById(R.id.actionbar_refresh_btn);

	mProgress = (ProgressBar) mBarView.findViewById(R.id.actionbar_progress);

	mHomeBtn.setOnClickListener(new OnClickListener() {
	    // Go to the main Activity
	    @Override
	    public void onClick(View arg0) {
		Intent homeIntent = new Intent(getContext(), FriendizerActivity.class);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getContext().startActivity(homeIntent);
	    }
	});
    }

    public void setHomeAction(Action action) {
	mHomeBtn.setOnClickListener(this);
	mHomeBtn.setTag(action);
	mHomeBtn.setImageResource(action.getDrawable());
    }

    public void setTitle(CharSequence title) {
	mTitleView.setText(title);
    }

    public void setTitle(int resid) {
	mTitleView.setText(resid);
    }

    /**
     * @param show
     *            whether to show or hide the loading icon
     */
    public void showProgressBar(boolean show) {
	if (show)
	    setProgressBarVisibility(View.VISIBLE);
	else
	    setProgressBarVisibility(View.GONE);
    }

    /**
     * Set the enabled state of the progress bar.
     * 
     * @param One
     *            of {@link View#VISIBLE}, {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setProgressBarVisibility(int visibility) {
	mProgress.setVisibility(visibility);
    }

    /**
     * Returns the visibility status for the progress bar.
     * 
     * @param One
     *            of {@link View#VISIBLE}, {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public int getProgressBarVisibility() {
	return mProgress.getVisibility();
    }

    /**
     * Function to set a click listener for Title TextView
     * 
     * @param listener
     *            the onClickListener
     */
    public void setOnTitleClickListener(OnClickListener listener) {
	mTitleView.setOnClickListener(listener);
    }

    @Override
    public void onClick(View view) {
	final Object tag = view.getTag();
	if (tag instanceof Action) {
	    final Action action = (Action) tag;
	    action.performAction(view);
	}
    }

    /**
     * Adds a list of {@link Action}s.
     * 
     * @param actionList
     *            the actions to add
     */
    public void addActions(ActionList actionList) {
	int actions = actionList.size();
	for (int i = 0; i < actions; i++) {
	    addAction(actionList.get(i));
	}
    }

    /**
     * Adds a new {@link Action}.
     * 
     * @param action
     *            the action to add
     */
    public void addAction(Action action) {
	final int index = mActionsView.getChildCount();
	addAction(action, index);
    }

    /**
     * Adds a new {@link Action} at the specified index.
     * 
     * @param action
     *            the action to add
     * @param index
     *            the position at which to add the action
     */
    public void addAction(Action action, int index) {
	mActionsView.addView(inflateAction(action), index);
    }

    /**
     * Removes all action views from this action bar
     */
    public void removeAllActions() {
	mActionsView.removeAllViews();
    }

    /**
     * Remove a action from the action bar.
     * 
     * @param index
     *            position of action to remove
     */
    public void removeActionAt(int index) {
	mActionsView.removeViewAt(index);
    }

    /**
     * Remove a action from the action bar.
     * 
     * @param action
     *            The action to remove
     */
    public void removeAction(Action action) {
	int childCount = mActionsView.getChildCount();
	for (int i = 0; i < childCount; i++) {
	    View view = mActionsView.getChildAt(i);
	    if (view != null) {
		final Object tag = view.getTag();
		if (tag instanceof Action && tag.equals(action)) {
		    mActionsView.removeView(view);
		}
	    }
	}
    }

    public ArrayList<View> getButtons() {
	ArrayList<View> views = new ArrayList<View>();
	int childCount = mActionsView.getChildCount();
	for (int i = 0; i < childCount; i++)
	    views.add(mActionsView.getChildAt(i));
	return views;
    }

    /**
     * Returns the number of actions currently registered with the action bar.
     * 
     * @return action count
     */
    public int getActionCount() {
	return mActionsView.getChildCount();
    }

    /**
     * Inflates a {@link View} with the given {@link Action}.
     * 
     * @param action
     *            the action to inflate
     * @return a view
     */
    private View inflateAction(Action action) {
	View view = mInflater.inflate(R.layout.actionbar_item, mActionsView, false);

	ImageButton labelView = (ImageButton) view.findViewById(R.id.actionbar_item);
	labelView.setImageResource(action.getDrawable());

	view.setTag(action);
	view.setOnClickListener(this);
	return view;
    }

    /**
     * A {@link LinkedList} that holds a list of {@link Action}s.
     */
    public static class ActionList extends LinkedList<Action> {
    }

    public interface Action {
	public int getDrawable();

	public void performAction(View view);
    }

    /**
     * Definition of an action that could be performed, along with a icon to show.
     */
    public static abstract class AbstractAction implements Action {
	final private int mDrawable;

	public AbstractAction(int drawable) {
	    mDrawable = drawable;
	}

	public int getDrawable() {
	    return mDrawable;
	}

	public abstract void performAction(View view);
    }

    public static class NoAction extends AbstractAction {

	public NoAction(int drawable) {
	    super(drawable);
	}

	@Override
	public void performAction(View view) {
	}

    }

    public static class IntentAction extends AbstractAction {
	private Context mContext;
	private Intent mIntent;

	public IntentAction(Context context, Intent intent, int drawable) {
	    super(drawable);
	    mContext = context;
	    mIntent = intent;
	}

	@Override
	public void performAction(View view) {
	    try {
		mContext.startActivity(mIntent);
	    } catch (ActivityNotFoundException e) {
		Toast.makeText(mContext, mContext.getText(R.string.actionbar_activity_not_found), Toast.LENGTH_SHORT).show();
	    }
	}
    }

    /*
     * public static abstract class SearchAction extends AbstractAction { public SearchAction() {
     * super(R.drawable.actionbar_search); } }
     */
}
