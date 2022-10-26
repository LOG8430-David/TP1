package org.schabi.newpipe.local.subscription.item

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.schabi.newpipe.R
import org.schabi.newpipe.databinding.FeedItemCarouselBinding
import org.schabi.newpipe.local.subscription.decoration.FeedGroupCarouselDecoration

class FeedGroupCarouselItem(
    context: Context,
    private val carouselAdapter: GroupAdapter<GroupieViewHolder<FeedItemCarouselBinding>>,
    private var listView: Int,
    private var isGridLayout: Boolean
) : BindableItem<FeedItemCarouselBinding>() {
    private val feedGroupCarouselDecoration = FeedGroupCarouselDecoration(context)

    private var linearLayoutManager: LinearLayoutManager? = null
    private var listState: Parcelable? = null

    override fun getLayout() = R.layout.feed_item_carousel

    fun onSaveInstanceState(): Parcelable? {
        listState = linearLayoutManager?.onSaveInstanceState()
        return listState
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        linearLayoutManager?.onRestoreInstanceState(state)
        listState = state
    }

    override fun initializeViewBinding(view: View): FeedItemCarouselBinding {
        val viewHolder = FeedItemCarouselBinding.bind(view)

        linearLayoutManager = LinearLayoutManager(view.context, listView, false)

        viewHolder.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = carouselAdapter
            addItemDecoration(feedGroupCarouselDecoration)
        }
        if (isGridLayout) viewHolder.recyclerView.setLayoutManager(GridLayoutManager(view.context, 3))
        return viewHolder
    }

    override fun bind(viewBinding: FeedItemCarouselBinding, position: Int) {
        viewBinding.recyclerView.apply { adapter = carouselAdapter }
        linearLayoutManager?.onRestoreInstanceState(listState)
    }

    override fun unbind(viewHolder: GroupieViewHolder<FeedItemCarouselBinding>) {
        super.unbind(viewHolder)

        listState = linearLayoutManager?.onSaveInstanceState()
    }
}
