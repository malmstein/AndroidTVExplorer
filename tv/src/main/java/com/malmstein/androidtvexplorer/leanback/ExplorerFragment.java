package com.malmstein.androidtvexplorer.leanback;

import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.malmstein.androidtvexplorer.R;
import com.malmstein.androidtvexplorer.presenters.CardPresenter;
import com.malmstein.androidtvexplorer.presenters.GridItemPresenter;
import com.malmstein.androidtvexplorer.presenters.PicassoBackgroundManagerTarget;
import com.malmstein.androidtvexplorer.video.Movie;
import com.malmstein.androidtvexplorer.video.VideoItemLoader;
import com.malmstein.androidtvexplorer.video.VideoProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ExplorerFragment extends BrowseFragment implements LoaderManager.LoaderCallbacks<HashMap<String, List<Movie>>> {

    private static final String TAG = "ExplorerFragment";
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private final Handler mHandler = new Handler();
    private URI mBackgroundURI;
    private static String mVideosUrl;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        loadVideoData();

        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
    }

    private void loadVideoData() {
        VideoProvider.setContext(getActivity());
        mVideosUrl = getActivity().getResources().getString(R.string.catalog_url);
        getLoaderManager().initLoader(0, null, this);
    }

    private void prepareBackgroundManager() {

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnItemViewSelectedListener(getDefaultItemSelectedListener());
        setOnItemViewClickedListener(getDefaultItemClickedListener());
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    protected OnItemViewSelectedListener getDefaultItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder viewHolder, Object item, RowPresenter.ViewHolder viewHolder2, Row row) {
                if (item instanceof Movie) {
                    mBackgroundURI = ((Movie) item).getBackgroundImageURI();
                    startBackgroundTimer();
                }
            }
        };
    }

    protected OnItemViewClickedListener getDefaultItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object item, RowPresenter.ViewHolder viewHolder2, Row row) {
                if (item instanceof Movie) {
                    Movie movie = (Movie) item;
                    Log.d(TAG, "Item: " + item.toString());
                    Toast.makeText(getActivity(), movie.getTitle(), Toast.LENGTH_LONG);
                } else if (item instanceof String) {
                    Toast.makeText(getActivity(), (String) item, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), 300);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });

        }
    }

    @Override
    public Loader<HashMap<String, List<Movie>>> onCreateLoader(int arg0, Bundle arg1) {
        Log.d(TAG, "VideoItemLoader created ");
        return new VideoItemLoader(getActivity(), mVideosUrl);
    }

    @Override
    public void onLoadFinished(Loader<HashMap<String, List<Movie>>> arg0, HashMap<String, List<Movie>> data) {

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        int i = 0;

        for (Map.Entry<String, List<Movie>> entry : data.entrySet()) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            List<Movie> list = entry.getValue();

            for (int j = 0; j < list.size(); j++) {
                listRowAdapter.add(list.get(j));
            }
            HeaderItem header = new HeaderItem(i, entry.getKey(), null);
            i++;
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        HeaderItem gridHeader = new HeaderItem(i, getResources().getString(R.string.preferences), null);

        GridItemPresenter gridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.grid_view));
        gridRowAdapter.add(getResources().getString(R.string.send_feeback));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);
    }

    @Override
    public void onLoaderReset(Loader<HashMap<String, List<Movie>>> arg0) {
        mRowsAdapter.clear();
    }

}
