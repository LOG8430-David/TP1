package org.schabi.newpipe.fragments.list.comments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.schabi.newpipe.R;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.fragments.list.BaseListInfoFragment;
import org.schabi.newpipe.ktx.ViewUtils;
import org.schabi.newpipe.util.ExtractorHelper;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class CommentsFragment extends BaseListInfoFragment<CommentsInfoItem, CommentsInfo> {
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Page replies;
    private CommentsInfoItem preComment;

    private TextView emptyStateDesc;

    private Callback replyCallback;

    public static CommentsFragment getInstance(final int serviceId, final String url,
                                               final String name,
                                               final Callback replyCallback) {
        final CommentsFragment instance = new CommentsFragment();
        instance.replyCallback = replyCallback;
        instance.setInitialData(serviceId, url, name, null, null);
        return instance;
    }

    public static CommentsFragment getInstance(final int serviceId, final String url,
                                               final String name,
                                               final CommentsInfoItem preComment,
                                               final Callback replyCallback) {
        final CommentsFragment instance = new CommentsFragment();
        instance.replyCallback = replyCallback;
        instance.setInitialData(serviceId, url, name, null, preComment);
        return instance;
    }

    public static CommentsFragment getInstance(final int serviceId, final String url,
                                               final String name,
                                               final Page replyPage,
                                               final Callback replyCallback) {
        final CommentsFragment instance = new CommentsFragment();
        instance.replyCallback = replyCallback;
        instance.setInitialData(serviceId, url, name, replyPage, null);
        return instance;
    }

    public interface Callback {
        void replyClick(CommentsInfoItem selectedItem) throws Exception;
    }

    @Override
    protected void onItemCallback(final InfoItem selectedItem) throws Exception {
        super.onItemCallback(selectedItem);
        replyCallback.replyClick((CommentsInfoItem) selectedItem);
    }

    public CommentsFragment() {
        super(UserAction.REQUESTED_COMMENTS);
    }

    protected void setInitialData(final int sid, final String u, final String title,
                                  final Page repliesPage, final CommentsInfoItem comment) {
        this.replies = repliesPage;
        this.preComment = comment;
        super.setInitialData(sid, u, title);
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        emptyStateDesc = rootView.findViewById(R.id.empty_state_desc);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected Single<ListExtractor.InfoItemsPage<CommentsInfoItem>> loadMoreItemsLogic() {
        return ExtractorHelper.getMoreCommentItems(serviceId, currentInfo, currentNextPage);
    }

    @Override
    protected Single<CommentsInfo> loadResult(final boolean forceLoad) {
        if (replies == null) {
            if (preComment == null) {
                return ExtractorHelper.getCommentsInfo(serviceId, url, forceLoad);
            } else {
                return Single.fromCallable(() -> {
                    // get a info template
                    var info = ExtractorHelper.getCommentsInfo(
                            serviceId, url, forceLoad).blockingGet();
                    // clone comment object to avoid relatedItems and nextPage actually set null
                    info = CommentUtils.clone(info);
                    // push preComment
                    info.setRelatedItems(List.of(preComment));
                    info.setNextPage(null);
                    return info;
                });
            }
        } else {
            return ExtractorHelper.getCommentsReplyInfo(serviceId, url, forceLoad, replies);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void handleResult(@NonNull final CommentsInfo result) {
        super.handleResult(result);

        emptyStateDesc.setText(
                result.isCommentsDisabled()
                        ? R.string.comments_are_disabled
                        : R.string.no_comments);

        ViewUtils.slideUp(requireView(), 120, 150, 0.06f);
        disposables.clear();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void setTitle(final String title) {
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
    }

    @Override
    protected boolean isGridLayout() {
        return false;
    }
}
