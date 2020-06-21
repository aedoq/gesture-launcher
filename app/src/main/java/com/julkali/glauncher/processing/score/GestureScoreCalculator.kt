package com.julkali.glauncher.processing.score

import android.util.Log
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import com.julkali.glauncher.processing.data.Coordinate
import kotlin.math.sqrt
import kotlin.math.pow
import java.util.ArrayDeque

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
        if (subject.isPoint() && toCompare.isPoint()) return 1.0-dist_square(subjCoords[0], otherCoords[0])
        if (subject.isPoint() || toCompare.isPoint()) return -1.0

        // Dynamic time warping in O(n^2) time and O(n) space
        // Both coord lists have at least two points
        val dtw_buf = ArrayDeque<Pair<Double,Int>>(otherCoords.size)
        // i=j=0
        dtw_buf.addLast(Pair(dist_square(subjCoords[0], otherCoords[0]), 1))
        // i=0
        for (j in 1 until otherCoords.size) {
            dtw_buf.addLast(Pair(dist_square(subjCoords[0], otherCoords[j]), 1) + dtw_buf.last())
        }

        for (i in 1 until subjCoords.size) {
            // j = 0
            dtw_buf.addLast(Pair(dist_square(subjCoords[i], otherCoords[0]), 1) + dtw_buf.first())
            for (j in 1 until otherCoords.size) {
                val shortest = listOf(
                    dtw_buf.removeFirst(),
                    dtw_buf.first(),
                    dtw_buf.last()
                ).minBy { it.first }!!
                dtw_buf.addLast(Pair(dist_square(subjCoords[i], otherCoords[j]), 1) + shortest)
            }
            dtw_buf.removeFirst()
        }

        return 1.0 - sqrt(dtw_buf.last().first / dtw_buf.last().second)
    }

    private operator fun Pair<Double, Int>.plus(other: Pair<Double, Int>): Pair<Double, Int> {
        return Pair(this.first + other.first, this.second + other.second)
    }

    private fun dist_square(a: Coordinate, b: Coordinate): Double {
        return sqrt((a.x - b.x).pow(2.0) + (a.y - b.y).pow(2.0))
    }

    private fun Pointer.isPoint(): Boolean {
        return coords.size == 1
    }
}