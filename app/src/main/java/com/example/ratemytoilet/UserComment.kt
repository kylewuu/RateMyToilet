package com.example.ratemytoilet

class UserComment {
    var userName:String ?= null
    var date:String ?= null
    var rate:Float ?= 0.0f
    var comment:String ?= null
    var correspondToiletID : Long ?= 0L

    constructor(userName: String?, date: String?, rate: Float?, comment: String?, correspondToiletID: Long?) {
        this.userName = userName
        this.date = date
        this.rate = rate
        this.comment = comment
        this.correspondToiletID = correspondToiletID
    }
}