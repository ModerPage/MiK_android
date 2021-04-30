package me.modernpage.ui.postdetail;

import me.modernpage.data.local.entity.relation.PostRelation;

interface PostDetailHandler {
    void onShareClicked(PostRelation post);
}
