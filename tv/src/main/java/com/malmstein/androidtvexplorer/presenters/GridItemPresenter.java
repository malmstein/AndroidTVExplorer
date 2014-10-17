package com.malmstein.androidtvexplorer.presenters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

public class GridItemPresenter extends Presenter {
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView view = new TextView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackgroundColor(getResources().getColor(R.color.default_background));
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((TextView) viewHolder.view).setText((String) item);
    }

    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
