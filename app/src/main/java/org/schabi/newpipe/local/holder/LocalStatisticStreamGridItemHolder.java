package org.schabi.newpipe.local.holder;

import android.view.ViewGroup;

import org.schabi.newpipe.R;
import org.schabi.newpipe.info_list.ItemBuilder;

public class LocalStatisticStreamGridItemHolder extends LocalStatisticStreamItemHolder {
    public LocalStatisticStreamGridItemHolder(final ItemBuilder itemBuilder,
                                              final ViewGroup parent) {
        super(itemBuilder, R.layout.list_stream_grid_item, parent);
    }
}
