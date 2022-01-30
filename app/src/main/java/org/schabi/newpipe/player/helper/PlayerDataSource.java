package org.schabi.newpipe.player.helper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistTracker;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

import org.schabi.newpipe.extractor.services.youtube.YoutubeDashManifestCreator;

public class PlayerDataSource {

    public static final int LIVE_STREAM_EDGE_GAP_MILLIS = 10000;

    /**
     * An approximately 4.3 times greater value than the
     * {@link DefaultHlsPlaylistTracker#DEFAULT_PLAYLIST_STUCK_TARGET_DURATION_COEFFICIENT default}
     * to ensure that (very) low latency livestreams which got stuck for a moment don't crash too
     * early.
     */
    private static final double PLAYLIST_STUCK_TARGET_DURATION_COEFFICIENT = 15;
    private static final int MAXIMUM_SIZE_CACHED_GENERATED_MANIFESTS_PER_CACHE = 500;

    private final DataSource.Factory cacheDataSourceFactory;
    private final DataSource.Factory cachelessDataSourceFactory;

    public PlayerDataSource(@NonNull final Context context,
                            @NonNull final String userAgent,
                            @NonNull final TransferListener transferListener) {
        cacheDataSourceFactory = new CacheFactory(context, userAgent, transferListener);
        cachelessDataSourceFactory = new DefaultDataSourceFactory(context, userAgent,
                transferListener);
        YoutubeDashManifestCreator.setManifestsCachesMaximumSize(
                MAXIMUM_SIZE_CACHED_GENERATED_MANIFESTS_PER_CACHE);
    }

    public SsMediaSource.Factory getLiveSsMediaSourceFactory() {
        return new SsMediaSource.Factory(
                new DefaultSsChunkSource.Factory(cachelessDataSourceFactory),
                cachelessDataSourceFactory)
                .setLivePresentationDelayMs(LIVE_STREAM_EDGE_GAP_MILLIS);
    }

    public HlsMediaSource.Factory getLiveHlsMediaSourceFactory() {
        return new HlsMediaSource.Factory(cachelessDataSourceFactory)
                .setAllowChunklessPreparation(true)
                .setPlaylistTrackerFactory((dataSourceFactory, loadErrorHandlingPolicy,
                                            playlistParserFactory) ->
                        new DefaultHlsPlaylistTracker(dataSourceFactory, loadErrorHandlingPolicy,
                                playlistParserFactory,
                                PLAYLIST_STUCK_TARGET_DURATION_COEFFICIENT));
    }

    public DashMediaSource.Factory getLiveDashMediaSourceFactory() {
        return new DashMediaSource.Factory(
                getDefaultDashChunkSourceFactory(cachelessDataSourceFactory),
                cachelessDataSourceFactory);
    }

    public HlsMediaSource.Factory getHlsMediaSourceFactory(
            @Nullable final HlsPlaylistParserFactory hlsPlaylistParserFactory) {
        final HlsMediaSource.Factory factory = new HlsMediaSource.Factory(cacheDataSourceFactory);
        if (hlsPlaylistParserFactory != null) {
            factory.setPlaylistParserFactory(hlsPlaylistParserFactory);
        }
        return factory;
    }

    public DashMediaSource.Factory getDashMediaSourceFactory() {
        return new DashMediaSource.Factory(
                getDefaultDashChunkSourceFactory(cacheDataSourceFactory),
                cacheDataSourceFactory);
    }

    public ProgressiveMediaSource.Factory getProgressiveMediaSourceFactory() {
        return new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    public SingleSampleMediaSource.Factory getSingleSampleMediaSourceFactory() {
        return new SingleSampleMediaSource.Factory(cacheDataSourceFactory);
    }

    @NonNull
    private DefaultDashChunkSource.Factory getDefaultDashChunkSourceFactory(
            final DataSource.Factory dataSourceFactory) {
        return new DefaultDashChunkSource.Factory(dataSourceFactory);
    }
}
