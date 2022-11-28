package com.example.ratemytoilet

class UserComment {
    var userName:String ?= null
    var date:String ?= null
    var rate:Float ?= 0.0f
    var comment:String ?= null


    constructor(userName: String?, date: String?, rate: Float?, comment: String?) {
        this.userName = userName
        this.date = date
        this.rate = rate
        this.comment = comment
    }
}