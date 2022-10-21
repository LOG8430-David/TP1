package org.schabi.newpipe.local.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.LocalItem;
import org.schabi.newpipe.local.LocalItemBuilder;
import org.schabi.newpipe.local.bookmark.BookmarkFragment;
import org.schabi.newpipe.local.history.HistoryRecordManager;

import java.time.format.DateTimeFormatter;

public abstract class PlaylistItemHolder extends LocalItemHolder {
    public final ImageView itemThumbnailView;
    final TextView itemStreamCountView;
    public final TextView itemTitleView;
    public final TextView itemUploaderView;
    public final CheckBox selectCheckBox;

    public PlaylistItemHolder(final LocalItemBuilder infoItemBuilder, final int layoutId,
                              final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);

        itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
        itemTitleView = itemView.findViewById(R.id.itemTitleView);
        itemStreamCountView = itemView.findViewById(R.id.itemStreamCountView);
        itemUploaderView = itemView.findViewById(R.id.itemUploaderView);
        selectCheckBox = itemView.findViewById(R.id.selectCheckBox);

    }

    public PlaylistItemHolder(final LocalItemBuilder infoItemBuilder, final ViewGroup parent) {
        this(infoItemBuilder, R.layout.list_playlist_mini_item, parent);
    }

    @Override
    public void updateFromItem(final LocalItem localItem,
                               final HistoryRecordManager historyRecordManager,
                               final DateTimeFormatter dateTimeFormatter) {
        if (!BookmarkFragment.isMultiSelect) {
            selectCheckBox.setChecked(false);
            selectCheckBox.setVisibility(View.INVISIBLE);
        } else {
            selectCheckBox.setVisibility(View.VISIBLE);
        }

        itemView.setOnClickListener(view -> {
            if (itemBuilder.getOnItemSelectedListener() != null) {
                itemBuilder.getOnItemSelectedListener().selected(localItem);
            }
            if (BookmarkFragment.isMultiSelect) {
                if (selectCheckBox.isChecked()) {
                    selectCheckBox.setChecked(false);
                } else {
                    selectCheckBox.setChecked(true);
                }
            } else {
                selectCheckBox.setChecked(false);
            }
        });

        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(view -> {
            if (itemBuilder.getOnItemSelectedListener() != null) {
                itemBuilder.getOnItemSelectedListener().held(localItem);
            }
            return true;
        });
    }

}
