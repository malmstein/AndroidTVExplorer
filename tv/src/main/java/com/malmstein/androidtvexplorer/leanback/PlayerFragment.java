package com.malmstein.androidtvexplorer.leanback;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.malmstein.androidtvexplorer.R;
import com.malmstein.androidtvexplorer.video.Video;

public class PlayerFragment extends Fragment {

    private VideoView videoView;
    private Video selectedVideo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = View.inflate(getActivity(), R.layout.activity_player, null);

        videoView = (VideoView) root.findViewById(R.id.player_video_view);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadVideoData();
        playVideo();
    }

    private void loadVideoData() {
        selectedVideo = (Video) getActivity().getIntent().getSerializableExtra(getString(R.string.video));
    }

    private void playVideo() {
        videoView.setVideoURI(Uri.parse(selectedVideo.getVideoUrl()));
        videoView.setMediaController(new MediaController(getActivity()));
        videoView.requestFocus();
        videoView.start();
    }
}
