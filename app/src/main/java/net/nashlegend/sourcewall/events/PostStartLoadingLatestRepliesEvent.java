package net.nashlegend.sourcewall.events;

import net.nashlegend.sourcewall.model.Post;

/**
 * Created by NashLegend on 16/9/6.
 */

public class PostStartLoadingLatestRepliesEvent {
    public Post post;

    public PostStartLoadingLatestRepliesEvent(Post post) {
        this.post = post;
    }

}
