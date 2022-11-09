package com.example.deltabot

import android.util.Log
import java.io.OutputStream

// Class representant le robot
class Bot(pA: Int, pB: Int, pC: Int, outStream: OutputStream) {

    var positions = HashMap<String, Position>()

    // Coordonnées du préhenseur
    var a: Int = pA
    var b: Int = pB
    var c: Int = pC

    // Flux permettant pour envoyer des données au robot
    var oStream: OutputStream = outStream

    // Position PREDEFINI que le prehenseur peut atteindre (cercle de 6 points + point central)
    init {
        positions.put("A", Position(0, 40, 40))
        positions.put("B", Position(0, 0, 40))
        positions.put("C", Position(40, 0, 40))
        positions.put("D", Position(40, 0, 0))
        positions.put("E", Position(40, 40, 0))
        positions.put("F", Position(40, 40, 40))
        positions.put("G", Position(0, 0, 0))
    }

    // Quand on recoit une action du tableau d'action, on la traite
    fun processAction(action: UserAction) {
        if (action.type == 0) { // MOVE
            var pos = positions.get(action.text)
            var armA: String = "A"
            if (pos!!.a < a) {
                armA += "0" +  (a - pos.a).toString() // On recule
            } else {
                armA += "1" +  (pos.a - a).toString() // On avance
            }
            Log.i("msgcustom", armA)
            sendMessage(armA)
            a = pos.a
            var armB: String = "B"
            if (pos!!.b < b) {
                armB += "0" +  (b - pos.b).toString() // On recule
            } else {
                armB += "1" +  (pos.b - b).toString() // On avance
            }
            Log.i("msgcustom", armB)
            sendMessage(armB)
            b = pos.b
            var armC: String = "C"
            if (pos!!.c < c) {
                armC += "0" + (c - pos.c).toString() // On recule
            } else {
                armC += "1" + (pos.c - c).toString() // On avance
            }
            Log.i("msgcustom", armC)
            sendMessage(armC)
            c = pos.c
            // Affichage positions bras
        } else if (action.type == 1) { // MAGNET ON
            sendMessage("M1")
        } else { // MAGNET OFF
            sendMessage("M0")
        }
    }

    // Envoi une string au robot finissant par "?" (désigne la fin d'un message pour le programme sur le mbed)
    private fun sendMessage(str : String) {
        val messageToSend: String = str + "?"
        oStream.write(messageToSend.toByteArray())
    }
}