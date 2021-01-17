package org.schabi.newpipe.local.holder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.schabi.newpipe.database.LocalItem;
import org.schabi.newpipe.database.playlist.PlaylistMetadataEntry;
import org.schabi.newpipe.ktx.TextViewUtils;
import org.schabi.newpipe.local.LocalItemBuilder;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.util.ImageDisplayConstants;
import org.schabi.newpipe.util.Localization;

import java.time.format.DateTimeFormatter;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class LocalPlaylistItemHolder extends PlaylistItemHolder {
    public LocalPlaylistItemHolder(final LocalItemBuilder infoItemBuilder, final ViewGroup parent) {
        super(infoItemBuilder, parent);
    }

    LocalPlaylistItemHolder(final LocalItemBuilder infoItemBuilder, final int layoutId,
                            final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);
    }

    @NonNull
    @Override
    public Disposable updateFromItem(final LocalItem localItem,
                                     final HistoryRecordManager historyRecordManager,
                                     final DateTimeFormatter dateTimeFormatter) {
        if (!(localItem instanceof PlaylistMetadataEntry)) {
            return Disposable.disposed();
        }
        final PlaylistMetadataEntry item = (PlaylistMetadataEntry) localItem;
        final CompositeDisposable compositeDisposable = new CompositeDisposable(
                TextViewUtils.computeAndSetPrecomputedText(itemTitleView, item.name),
                TextViewUtils.computeAndSetPrecomputedText(itemStreamCountView,
                        Localization.localizeStreamCountMini(itemStreamCountView.getContext(),
                                item.streamCount))
        );

        itemUploaderView.setVisibility(View.INVISIBLE);

        itemBuilder.displayImage(item.thumbnailUrl, itemThumbnailView,
                ImageDisplayConstants.DISPLAY_PLAYLIST_OPTIONS);

        compositeDisposable.add(super.updateFromItem(localItem, historyRecordManager,
                dateTimeFormatter));

        return compositeDisposable;
    }
}
