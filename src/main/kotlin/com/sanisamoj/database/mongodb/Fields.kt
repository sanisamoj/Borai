package com.sanisamoj.database.mongodb

enum class Fields(val title: String) {
    Id(title = "_id"),
    Ups(title = "ups"),
    Bio(title = "bio"),
    Nick(title = "nick"),
    Name(title = "name"),
    Date(title = "date"),
    Type(title = "type"),
    Email(title = "email"),
    Image(title = "image"),
    Phone(title = "phone"),
    Score(title = "score"),
    Status(title = "status"),
    UserId(title = "userId"),
    Criteria(title = "criteria"),
    Quantity(title = "quantity"),
    VisibleInsignias(title = "visibleInsignias"),
    EventId(title = "eventId"),
    Address(title = "address"),
    ParentId(title = "parentId"),
    Username(title = "username"),
    AccountId(title = "accountId"),
    Presences(title = "presences"),
    CreatedAt(title = "createdAt"),
    EventVotes(title = "eventVotes"),
    FollowerIds(title = "followerIds"),
    Description(title = "description"),
    OtherImages(title = "otherImages"),
    MediaStorage(title = "mediaStorage"),
    AnswersCount(title = "answersCount"),
    FollowingIds(title = "followingIds"),
    ImageProfile(title = "imageProfile"),
    AccountStatus(title = "accountStatus"),
    ValidationCode(title = "validationCode"),
    AccountIsPublic(title = "accountIsPublic"),
    PendingSentRequests(title = "pendingSentRequests"),
    PendingFollowRequests(title = "pendingFollowRequests"),
}