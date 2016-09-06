package net.nashlegend.sourcewall.events;

import net.nashlegend.sourcewall.model.Post;

/**
 * Created by NashLegend on 16/9/6.
 */

public class PostFinishLoadingLatestRepliesEvent {
    public Post post;

    public PostFinishLoadingLatestRepliesEvent(Post post) {
        this.post = post;
    }

}
