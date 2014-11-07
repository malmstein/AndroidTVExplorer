package com.malmstein.androidtvexplorer.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.malmstein.androidtvexplorer.video.Video;

public class VideoDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object object) {
        Video details = (Video) object;

        viewHolder.getTitle().setText(details.getTitle());
        viewHolder.getSubtitle().setText(details.getStudio());
        viewHolder.getBody().setText(details.getDescription());
    }
}
