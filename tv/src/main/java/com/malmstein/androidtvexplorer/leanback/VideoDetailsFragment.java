package com.malmstein.androidtvexplorer.leanback;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.malmstein.androidtvexplorer.R;
import com.malmstein.androidtvexplorer.presenters.CardPresenter;
import com.malmstein.androidtvexplorer.presenters.PicassoBackgroundManagerTarget;
import com.malmstein.androidtvexplorer.presenters.Utils;
import com.malmstein.androidtvexplorer.presenters.VideoDetailsPresenter;
import com.malmstein.androidtvexplorer.video.Video;
import com.malmstein.androidtvexplorer.video.VideoProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "DetailsFragment";

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int ACTION_WATCH_VIDEO = 1;

    private Video selectedVideo;

    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        loadVideoDetailsData();

        setOnItemViewClickedListener(getDefaultItemClickedListener());
        updateBackground(selectedVideo.getBackgroundImageURI());
    }

    private void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void loadVideoDetailsData(){
        selectedVideo = (Video) getActivity().getIntent().getSerializableExtra(getString(R.string.video));
        new DetailRowBuilderTask().execute(selectedVideo);
    }

    private class DetailRowBuilderTask extends AsyncTask<Video, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground(Video... videos) {
            selectedVideo = videos[0];

            DetailsOverviewRow row = new DetailsOverviewRow(selectedVideo);

            Bitmap poster = null;
            try {
                poster = Picasso.with(getActivity())
                        .load(selectedVideo.getCardImageUrl())
                        .resize(Utils.dpToPx(DETAIL_THUMB_WIDTH, getActivity()
                                        .getApplicationContext()),
                                Utils.dpToPx(DETAIL_THUMB_HEIGHT, getActivity()
                                        .getApplicationContext()))
                        .centerCrop()
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            row.setImageBitmap(getActivity(), poster);
            row.addAction(new Action(ACTION_WATCH_VIDEO, getResources().getString(
                    R.string.watch_video_1), getResources().getString(R.string.watch_video_2)));

            return row;
        }

        @Override
        protected void onPostExecute(DetailsOverviewRow detailRow) {

            DetailsOverviewRowPresenter dorPresenter = new DetailsOverviewRowPresenter(new VideoDetailsPresenter());

            dorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
            dorPresenter.setStyleLarge(true);
            dorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
                @Override
                public void onActionClicked(Action action) {
                    if (action.getId() == ACTION_WATCH_VIDEO) {
                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        intent.putExtra(getResources().getString(R.string.video), selectedVideo);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ClassPresenterSelector ps = new ClassPresenterSelector();
            ps.addClassPresenter(DetailsOverviewRow.class, dorPresenter);
            ps.addClassPresenter(ListRow.class, new ListRowPresenter());

            ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
            adapter.add(detailRow);

            String subcategories[] = {getString(R.string.related_movies)};
            HashMap<String, List<Video>> videos = VideoProvider.getMovieList();

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
            for (Map.Entry<String, List<Video>> entry : videos.entrySet()) {
                if (selectedVideo.getCategory().indexOf(entry.getKey()) >= 0) {
                    List<Video> list = entry.getValue();
                    for (int j = 0; j < list.size(); j++) {
                        listRowAdapter.add(list.get(j));
                    }
                }
            }
            HeaderItem header = new HeaderItem(0, subcategories[0], null);
            adapter.add(new ListRow(header, listRowAdapter));

            setAdapter(adapter);
        }

    }

    protected OnItemViewClickedListener getDefaultItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object item, RowPresenter.ViewHolder viewHolder2, Row row) {
                if (item instanceof Movie) {
                    Video video = (Video) item;
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(getResources().getString(R.string.video), video);
                    startActivity(intent);
                }
            }
        };
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }
}
