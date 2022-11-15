package com.example.ratemytoilet

import androidx.lifecycle.MutableLiveData

class FilterClass {
    var paper : Int ?= -1
    var soap : Int ?= -1
    var save : Int ?= -1
    var rate : Int ?= -1
    var gender : String ?= null

    constructor(paper: Int?, soap: Int?, save: Int?, rate: Int?, gender: String?) {
        this.paper = paper
        this.soap = soap
        this.save = save
        this.rate = rate
        this.gender = gender
    }
}