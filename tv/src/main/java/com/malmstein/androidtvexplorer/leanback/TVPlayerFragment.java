package com.malmstein.androidtvexplorer.leanback;

import android.app.Fragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.malmstein.androidtvexplorer.R;
import com.malmstein.androidtvexplorer.video.Video;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TVPlayerFragment extends Fragment {

    private static final int HIDE_CONTROLLER_TIME = 5000;
    private static final int CLOSE_VIDEO_TIME = 3000;
    private static final int SCRUB_SEGMENT_DIVISOR = 30;
    private static final int MIN_SCRUB_TIME = 3000;
    private static final int SEEKBAR_DELAY_TIME = 100;
    private static final int SEEKBAR_INTERVAL_TIME = 1000;

    private VideoView videoView;
    private TextView startText;
    private TextView endText;
    private SeekBar videoSeekbar;
    private ImageView playPause;
    private ProgressBar loadingView;
    private View controllerView;

    private int videoDuration;
    private Video selectedVideo;

    private final Handler mHandler = new Handler();
    private Timer seekbarTimer;
    private Timer controllersTimer;
    private boolean areControllerVisible;

    private PlaybackState mPlaybackState;

    public static enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = View.inflate(getActivity(), R.layout.fragment_player, null);

        bindUI(root);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadVideoData();
        setMediaPlayerCallbacks();
        playVideo();
    }

    public void onKeyDown(int keyCode) {
        int currentPos;

        int delta = (videoDuration / SCRUB_SEGMENT_DIVISOR);
        if (delta < MIN_SCRUB_TIME) {
            delta = MIN_SCRUB_TIME;
        }

        if (!areControllerVisible) {
            updateControllersVisibility(true);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                currentPos = videoView.getCurrentPosition();
                currentPos -= delta;
                if (currentPos > 0) {
                    play(currentPos);
                }
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                currentPos = videoView.getCurrentPosition();
                currentPos += delta;
                if (currentPos < videoDuration) {
                    play(currentPos);
                }
        }
    }

    private void loadVideoData() {
        selectedVideo = (Video) getActivity().getIntent().getSerializableExtra(getString(R.string.video));
    }

    private void bindUI(View root) {
        videoView = (VideoView) root.findViewById(R.id.player_video_view);
        startText = (TextView) root.findViewById(R.id.startText);
        endText = (TextView) root.findViewById(R.id.endText);
        videoSeekbar = (SeekBar) root.findViewById(R.id.seekBar);
        playPause = (ImageView) root.findViewById(R.id.playpause);
        loadingView = (ProgressBar) root.findViewById(R.id.progressBar);
        controllerView = root.findViewById(R.id.controllers);

        videoView.setOnClickListener(mPlayPauseHandler);
    }

    private void setMediaPlayerCallbacks() {

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                videoView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                controllersTimer = new Timer();
                controllersTimer.schedule(new BackToDetailTask(), CLOSE_VIDEO_TIME);
                return false;
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                videoDuration = mp.getDuration();
                endText.setText(formatTimeSignature(videoDuration));
                videoSeekbar.setMax(videoDuration);
                restartSeekBarTimer();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopSeekBarTimer();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(PlaybackState.IDLE);
                controllersTimer = new Timer();
                controllersTimer.schedule(new BackToDetailTask(), HIDE_CONTROLLER_TIME);
            }
        });
    }

    private void playVideo() {
        videoView.setVideoPath(selectedVideo.getVideoUrl());
        mPlaybackState = PlaybackState.PLAYING;
        updatePlayButton(mPlaybackState);
        videoView.start();
        videoView.requestFocus();
        startControllersTimer();
        videoView.invalidate();
    }

    private void updateSeekbar(int position, int duration) {
        videoSeekbar.setProgress(position);
        videoSeekbar.setMax(duration);
        startText.setText(formatTimeSignature(videoDuration));
    }

    private void updateControllersVisibility(boolean show) {
        controllerView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void updatePlayButton(PlaybackState state) {
        switch (state) {
            case PLAYING:
                loadingView.setVisibility(View.INVISIBLE);
                playPause.setVisibility(View.VISIBLE);
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_playcontrol_normal));
                break;
            case PAUSED:
            case IDLE:
                loadingView.setVisibility(View.INVISIBLE);
                playPause.setVisibility(View.VISIBLE);
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_playcontrol_normal));
                break;
            case BUFFERING:
                playPause.setVisibility(View.INVISIBLE);
                loadingView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void play() {
        videoView.start();
        mPlaybackState = PlaybackState.PLAYING;
        updatePlayButton(mPlaybackState);
        startControllersTimer();
    }

    private void play(int position) {
        videoView.seekTo(position);
        play();
        restartSeekBarTimer();
    }

    private void pause() {
        videoView.pause();
        mPlaybackState = PlaybackState.PAUSED;
        updatePlayButton(mPlaybackState);
        stopControllersTimer();
    }

    private void startControllersTimer() {
        if (null != controllersTimer) {
            controllersTimer.cancel();
        }
        controllersTimer = new Timer();
        controllersTimer.schedule(new HideControllersTask(), HIDE_CONTROLLER_TIME);
    }

    private void stopControllersTimer() {
        if (null != controllersTimer) {
            controllersTimer.cancel();
        }
    }

    private void restartSeekBarTimer() {
        stopSeekBarTimer();
        seekbarTimer = new Timer();
        seekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), SEEKBAR_DELAY_TIME, SEEKBAR_INTERVAL_TIME);
    }

    private void stopSeekBarTimer() {
        if (null != seekbarTimer) {
            seekbarTimer.cancel();
        }
    }

    View.OnClickListener mPlayPauseHandler = new View.OnClickListener() {
        public void onClick(View v) {

            if (!areControllerVisible) {
                updateControllersVisibility(true);
            }

            if (mPlaybackState == PlaybackState.PAUSED) {
                play();
            } else {
                pause();
            }
        }
    };

    private class HideControllersTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControllersVisibility(false);
                    areControllerVisible = false;
                }
            });

        }
    }

    private class BackToDetailTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(getResources().getString(R.string.video), selectedVideo);
                    startActivity(intent);
                }
            });

        }
    }

    private class UpdateSeekbarTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    int currentPos = 0;
                    currentPos = videoView.getCurrentPosition();
                    updateSeekbar(currentPos, videoDuration);
                }
            });
        }
    }

    private String formatTimeSignature(int timeSignature) {
        return String.format(Locale.US,
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeSignature),
                TimeUnit.MILLISECONDS.toSeconds(timeSignature)
                        -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                .toMinutes(timeSignature)));
    }
}
