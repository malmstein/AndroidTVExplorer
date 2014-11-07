package com.malmstein.androidtvexplorer.leanback.recommendations;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.util.Log;

import com.malmstein.androidtvexplorer.R;
import com.malmstein.androidtvexplorer.leanback.VideoDetailsActivity;
import com.malmstein.androidtvexplorer.video.Video;
import com.malmstein.androidtvexplorer.video.VideoProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateRecommendationsService extends IntentService {

    private static final String TAG = "UpdateRecommendationsService";
    private static final int MAX_RECOMMENDATIONS = 3;

    public UpdateRecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Updating recommendation cards");
        HashMap<String, List<Video>> recommendations = VideoProvider.getMovieList();

        int count = 0;

        try {
            RecommendationBuilder builder = new RecommendationBuilder()
                    .setContext(getApplicationContext())
                    .setSmallIcon(R.drawable.videos_by_google_icon);

            for (Map.Entry<String, List<Video>> entry : recommendations.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    Video video = entry.getValue().get(i);
                    Log.d(TAG, "Recommendation - " + video.getTitle());

                    builder.setBackground(video.getCardImageUrl())
                            .setId(count + 1)
                            .setPriority(MAX_RECOMMENDATIONS - count)
                            .setTitle(video.getTitle())
                            .setDescription(getString(R.string.popular_header))
                            .setImage(video.getCardImageUrl())
                            .setIntent(buildPendingIntent(video))
                            .build();

                    if (++count >= MAX_RECOMMENDATIONS) {
                        break;
                    }
                }
                if (++count >= MAX_RECOMMENDATIONS) {
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to update recommendation", e);
        }
    }

    private PendingIntent buildPendingIntent(Video video) {
        Intent detailsIntent = new Intent(this, VideoDetailsActivity.class);
        detailsIntent.putExtra("Movie", video);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(VideoDetailsActivity.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailsIntent.setAction(Long.toString(video.getId()));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}
