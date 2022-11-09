package com.example.deltabot

class UserAction(aType: Int, aText: String) {

    var type: Int = aType
    var text: String? = null

    init {
        if (aType == 0) { // Cas coordonnees
            text = aText
        } else if (aType == 1) {
            text = "MAGNET ON"
        } else {
            text = "MAGNET OFF"
        }
    }
}