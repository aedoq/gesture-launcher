package com.julkali.glauncher.processing.score

import android.util.Log
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import kotlin.math.sqrt
import kotlin.math.pow

class GestureScoreCalculator {

    private val TAG = "GestureScoreCalculator"

    fun calculate(subject: Gesture, toCompare: Gesture): Double {
        if (subject.pointers.size != toCompare.pointers.size) {
            return -1.0 // todo: create score for each compbination of pointer lists for bigger one
        }
        // todo: have to compare every combination of pointers, because they might not have same indices
        val pointerScores = subject.pointers
            .zip(toCompare.pointers)
            .map { (subj, other) ->
                calculatePointerScore(subj, other)
            }
        return pointerScores.average()
    }

    private fun calculatePointerScore(subject: Pointer, toCompare: Pointer): Double {
        val subjCoords = subject.coords
        val otherCoords = toCompare.coords
        val size = subjCoords.size
        if (size != otherCoords.size) {
            if (subject.isPoint() || toCompare.isPoint()) return -1.0
            Log.e(TAG, "subjCoords.size = ${subjCoords.size}, otherCoords.size = ${otherCoords.size}")
            throw Exception("Coords are not of same length")
        }
        val total = subjCoords
            .zip(otherCoords)
            .map { (subj, other) ->
                (other.x - subj.x).pow(2.0) + (other.y - subj.y).pow(2.0)
            }
            .sum()
        val averageDifference = sqrt(total / size)
        return 1.0 - averageDifference
    }

    private fun Pointer.isPoint(): Boolean {
        return coords.size == 1
    }
}