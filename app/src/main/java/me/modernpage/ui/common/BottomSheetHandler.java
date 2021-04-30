package me.modernpage.ui.common;

import me.modernpage.data.local.entity.relation.PostRelation;

public interface BottomSheetHandler {
    void deletePost(PostRelation post);

    void editPost(PostRelation post);

    void reportPost(PostRelation post);

    void hidePost(PostRelation post);
}
