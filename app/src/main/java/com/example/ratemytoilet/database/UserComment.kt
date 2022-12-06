package com.example.ratemytoilet

class UserComment {
    var userName:String ?= null
    var date:String ?= null
    var rate:Float ?= 0.0f
    var comment:String ?= null
    var soap:Int ?= -1
    var paper:Int ?= -1
    var access:Int ?= -1
    var leftByAdmin = false

    constructor(userName: String?, date: String?, rate: Float?, comment: String?, soap: Int?, paper: Int?, access: Int?, leftByAdmin: Boolean) {
        this.userName = userName
        this.date = date
        this.rate = rate
        this.comment = comment
        this.soap = soap
        this.paper = paper
        this.access = access
        this.leftByAdmin = leftByAdmin
    }
}