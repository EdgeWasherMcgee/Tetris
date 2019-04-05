import kotlin.math.abs

object Blocks {
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
    val blocks = arrayOf(pJ, pL, pZ, pS, pT, pO, pI)

    fun IntArray.roughen(x: Int): Array<IntArray> {
        return Array(this.size / x) { z: Int -> IntArray(x) { i: Int -> this[i + z * x] } }
    }
}

class TetrisCore {

    var blCords = intArrayOf(0, 5)
    var locked = false
    private var clearedLines: Int = 0
    private var clearedyCords = IntArray(4)
    private var coll = false
    private var board = Array(20) { IntArray(10) }
    private var input = false
    private var rot = 0
    private var curFrame = 0
    private var curBlockNum: Int = newBlock()
    private var blShape = Blocks.blocks[curBlockNum]
    private var nextShape = newBlock()

    fun newTick(flip: Int, move: Int, down: Int = 0, freq: Int = 10) {
        input = false
        val sMove = if (move > 0) 1 else if (move < 0) -1 else 0
        val sFlip = if (flip > 0) 1 else if (flip < 0) -1 else 0
        this.clearedLines = 0
        var yCords = IntArray(4)
        if (coll) {
            solidify()
            blShape = Blocks.blocks[nextShape]
            curBlockNum = nextShape
            nextShape = newBlock()
            if (lockOut()) {
                locked = true
                return
            }
            yCords = checkForFullLines()
            if (efflen(yCords) > 0) {
                clearLine(yCords)
            }
            coll = false
        } else {
            if ((abs(flip) == 1) or ((abs(flip) >= 16) and (((abs(flip) - 16) % 6) == 0))) {
                if (!blCollission(blCords[0], blCords[1], nRot = abs((rot + sFlip) % 4))) {
                    input = true
                    rot = abs((rot + sFlip) % 4)
                }
            }
            if ((abs(move) == 1) or ((abs(move) > 16) and ((abs(move) % 6) == 0))) {
                if (!blCollission(blCords[0], blCords[1] + sMove)) {
                    input = true
                    blCords[1] += sMove
                }
            }
            if ((curFrame % (freq / (down + 1)) == 0)) {
                if (!blCollission(blCords[0] + 1, blCords[1], true)) {
                    blCords[0] += 1
                }
            }
        }
        curFrame = (curFrame + 1) % 216000

    }

    fun getCord(x: Int, y: Int): Int {
        return board[y][x]
    }

    fun gotInput(): Boolean {
        return input
    }

    fun getShape(): Array<IntArray> {
        return blShape[rot]
    }

    fun getNextShape(): Int {
        return nextShape
    }

    fun getCurShapeNum() : Int {
        return curBlockNum
    }

    fun getClearedLines() : Int {
        return clearedLines
    }

    fun getClearedyCords() : IntArray {
        return clearedyCords
    }

    private fun solidify() {
        for (bl in blShape[rot]) {
            board[(bl[0] + blCords[0])][bl[1] + blCords[1]] = 1
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

    private fun newBlock(): Int {
        blCords[0] = 0
        blCords[1] = 5
        rot = 0
        return (0 until 7).random()
    }

    private fun clearLine(yCords: IntArray) { //I want to move all the lines from the first Y-cord upwards, and not copy the remove lines

        var offset = 1
        clearedLines = efflen(yCords)
        clearedyCords = yCords.copyOf()

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

    private fun blCollission(ycord: Int, xcord: Int, record: Boolean = false, nRot: Int = rot): Boolean {

        for (s in blShape[nRot]) {
            if (((ycord + s[0] < 0) or (ycord + s[0] > 19) or (xcord + s[1] < 0) or (xcord + s[1] > 9))) {
                if (record) {
                    coll = true
                }
                return true
            } else if ((board[ycord + s[0]][xcord + s[1]] > 0)) {
                if (record) {
                    coll = true
                }
                return true
            }
        }
        return false
    }

    private fun lockOut() : Boolean {
        for (block in blShape[0]) {
            if (blCollission(blCords[0] + block[0], blCords[1] + block[1])) {
                return true
            }
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