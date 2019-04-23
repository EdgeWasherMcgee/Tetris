package co

import kotlin.math.abs

class BlockController(inCords: Array<Int> = arrayOf(0, 5)) {

    var orientation = 0
    var cords: Array<Int> = inCords
    var nextShapeId = (0 until 7).random()
    var shapeId = (0 until 7).random()
    var nextShape = blocks[nextShapeId]
    var shape = blocks[shapeId]
    var block = Array(4) { outer ->
        Array(2) { inner ->
            shape[this.orientation][outer][inner] + this.cords[inner]
        }
    }

    fun block() = Array(4) { outer ->
        Array(2) { inner ->
            this.shape[this.orientation][outer][inner] + this.cords[inner]
        }
    }

    fun new() {
        this.cords = arrayOf(0, 5)
        this.orientation = 0
        this.shapeId = this.nextShapeId
        this.shape = blocks[this.shapeId]
        this.nextShapeId = (0 until 7).random()
        this.nextShape = blocks[this.nextShapeId]
    }

    companion object Blocks {
        val pJ = arrayOf(
            intArrayOf(0, -1, 0, 0, 0, 1, 1, 1).roughen(2),
            intArrayOf(-1, 0, -1, 1, 0, 0, 1, 0).roughen(2),
            intArrayOf(-1, -1, 0, -1, 0, 0, 0, 1).roughen(2),
            intArrayOf(-1, 0, 0, 0, 1, 0, 1, -1).roughen(2)
        )
        val pL = arrayOf(
            intArrayOf(0, -1, 0, 0, 0, 1, 1, -1).roughen(2),
            intArrayOf(-1, 0, 0, 0, 1, 0, 1, 1).roughen(2),
            intArrayOf(0, -1, 0, 0, 0, 1, -1, 1).roughen(2),
            intArrayOf(-1, -1, -1, 0, 0, 0, 1, 0).roughen(2)
        )
        val pZ = arrayOf(
            intArrayOf(0, -1, 0, 0, 1, 0, 1, 1).roughen(2),
            intArrayOf(-1, 0, 0, 0, 0, -1, 1, -1).roughen(2),
            intArrayOf(0, -1, 0, 0, 1, 0, 1, 1).roughen(2),
            intArrayOf(-1, 0, 0, 0, 0, -1, 1, -1).roughen(2)
        )
        val pS = arrayOf(
            intArrayOf(0, 0, 0, 1, 1, -1, 1, 0).roughen(2),
            intArrayOf(-1, 0, 0, 0, 0, 1, 1, 1).roughen(2),
            intArrayOf(0, 0, 0, 1, 1, -1, 1, 0).roughen(2),
            intArrayOf(-1, 0, 0, 0, 0, 1, 1, 1).roughen(2)
        )
        val pT = arrayOf(
            intArrayOf(0, -1, 0, 0, 0, 1, 1, 0).roughen(2),
            intArrayOf(-1, 0, 0, 0, 0, 1, 1, 0).roughen(2),
            intArrayOf(-1, 0, 0, -1, 0, 0, 0, 1).roughen(2),
            intArrayOf(-1, 0, 0, -1, 0, 0, 1, 0).roughen(2)
        )
        val pO = arrayOf(
            intArrayOf(0, -1, 0, 0, 1, -1, 1, 0).roughen(2),
            intArrayOf(0, -1, 0, 0, 1, -1, 1, 0).roughen(2),
            intArrayOf(0, -1, 0, 0, 1, -1, 1, 0).roughen(2),
            intArrayOf(0, -1, 0, 0, 1, -1, 1, 0).roughen(2)
        )
        val pI = arrayOf(
            intArrayOf(0, -2, 0, -1, 0, 0, 0, 1).roughen(2),
            intArrayOf(-2, 0, -1, 0, 0, 0, 1, 0).roughen(2),
            intArrayOf(0, -2, 0, -1, 0, 0, 0, 1).roughen(2),
            intArrayOf(-2, 0, -1, 0, 0, 0, 1, 0).roughen(2)
        )
        val blocks = arrayOf(
            pJ,
            pL,
            pZ,
            pS,
            pT,
            pO,
            pI
        )

        fun IntArray.roughen(x: Int): Array<IntArray> {
            return Array(this.size / x) { z: Int -> IntArray(x) { i: Int -> this[i + z * x] } }
        }
    }
}


class TetrisCore {

    private var locked = false
    private var block = BlockController()
    private var clearedLines: Int = 0
    private var clearedYCords = IntArray(4)
    private var coll = 0
    private var board = Array(20) { IntArray(10) }
    private var input = false
    private var curFrame = 0

    fun newTick(flip: Int, move: Int, down: Int = 0, freq: Int = 10) {
        input = false
        val sMove = if (move > 0) 1 else if (move < 0) -1 else 0
        val sFlip = if (flip > 0) 1 else if (flip < 0) -1 else 0
        this.clearedLines = 0
        var yCords = IntArray(4)
        if (coll < 8) {

            blockFlip(flip, sFlip)

            blockMove(move, sMove)

            blockFall(freq, down)

        } else {
            solidify()
            block.new()
            if (lockOut()) {
                locked = true
                return
            }
            yCords = checkForFullLines()
            clearLine(yCords)
            coll = 0
        }

        coll += 1
        println(coll)
        curFrame = (curFrame + 1) % 216000
    }



    fun getCord(x: Int, y: Int) = board[y][x]

    fun getShape() = block.shape[block.orientation]

    fun getNextShape() = block.nextShape[0]

    fun getNextShapeId() = block.nextShapeId

    fun getShapeId() = block.shapeId

    fun getClearedLines() = clearedLines

    fun getClearedYCords() = clearedYCords

    fun getCords() = block.cords

    fun isLocked() = locked

    private fun isOutOfBounds(xcord: Int, ycord: Int) = ((ycord < 0) or (ycord > 19) or (xcord < 0) or (xcord > 9))

    private fun isOccupied(xcord: Int, ycord: Int) = (board[ycord][xcord] > 0)

    private fun solidify() {
        for (bl in block.block()) {
            board[bl[0]][bl[1]] = block.shapeId + 1
        }
    }

    private fun blockFlip(flip: Int, sFlip: Int) {
        if ((abs(flip) == 1) or ((abs(flip) >= 16) and (((abs(flip) - 16) % 6) == 0))) {
            if (!blCollission(block.cords[0], block.cords[1], nRot = abs((block.orientation + sFlip) % 4))) {
                input = true
                block.orientation = abs((block.orientation + sFlip) % 4)
            }
        }
    }

    private fun blockMove(move: Int, sMove: Int) {
        if ((abs(move) == 1) or ((abs(move) > 16) and ((abs(move) % 6) == 0))) {
            if (!blCollission(block.cords[0], block.cords[1] + sMove)) {
                input = true
                block.cords[1] += sMove
            }
        }
    }

    private fun blockFall(freq: Int, down: Int) {
        if (!blCollission(block.cords[0] + 1, block.cords[1])) {
            if ((curFrame % (freq / (down + 1)) == 0)) {
                block.cords[0] += 1
            }
            coll = 0
        }
    }

    private fun checkForFullLines(): IntArray {
        var yCords = IntArray(4) { -1 }
        var whatLine = 0
        var full: Boolean
        for (yCord in 0 until 20) {
            full = true
            for (x in 0 until 10) {
                if (board[yCord][x] == 0) {
                    full = false
                    break
                }
            }
            if (full) {
                yCords[whatLine] = yCord
                whatLine++
            }
        }
        return yCords
    }

    private fun clearLine(yCords: IntArray) { //I want to move all the lines from the first Y-cord upwards, and not copy the remove lines
        if (efflen(yCords) == 0) {
            return
        }
        var offset = 1
        clearedLines = efflen(yCords)
        clearedYCords = yCords.copyOf()

        for (line in yCords[clearedLines - 1] downTo (0 + clearedLines)) {
            while (true) {
                if ((line - offset) in yCords) {
                    offset++
                } else {
                    break
                }
            }
            copyLine(line - offset, line)
        }

        for (line in 0 until clearedLines) {
            board[line] = IntArray(10) { 0 }
        }
    }

    private fun blCollission(ycord: Int, xcord: Int, nRot: Int = block.orientation): Boolean {

        for (s in block.shape[nRot]) {
            if (isOutOfBounds(xcord + s[1], ycord + s[0])) {
                return true
            } else if (isOccupied(xcord + s[1], ycord + s[0])) {
                return true
            }
        }
        return false
    }

    private fun lockOut(): Boolean {
        if (blCollission(block.cords[0], block.cords[1])) {
            return true
        }
        return false
    }

    private fun copyLine(fromLine: Int, toLine: Int) {
        board[toLine] = board[fromLine].copyOf()
    }

    private fun efflen(cords: IntArray): Int {
        var tally = 0
        for (i in cords) {
            if (i > -1) {
                tally++
            }
        }
        return tally
    }
}