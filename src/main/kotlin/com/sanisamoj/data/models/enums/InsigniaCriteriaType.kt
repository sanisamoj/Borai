package com.sanisamoj.data.models.enums

enum class InsigniaCriteriaType(val title: String) {
    Comments("comments"), // Number of comments
    UpsComments("upsComments"), // Number of Ups in the comments
    AnswerComments("answerComments"), // Number of responses in comments

    Events("events"), // Number of events created
    PresencesEvents("presencesEvents"), // Number of attendances at created events
    CommentsEvents("commentsEvents"), // Number of comments on events
    //RatingEvents("presencesFromTheUser"), // Number of rate on events

    PresencesFromTheUser("invitationsReceived"), // Number of user presences at events
//    InvitationsReceived("invitationsSent"), // Number of invitations made
//    InvitationsSent("ratingEvents"), // Number of invitations received
//    FollowersCount("followersCount"), // Number of followers
//    FollowingCount("followingCount") // Number of following
}