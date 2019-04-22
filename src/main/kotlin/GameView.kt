import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.component.Panel
import org.hexworks.zircon.api.data.GraphicTile
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.extensions.onKeyboardEvent
import org.hexworks.zircon.api.graphics.BoxType
import org.hexworks.zircon.api.graphics.Layer
import org.hexworks.zircon.api.mvc.base.BaseView
import org.hexworks.zircon.api.resource.GraphicalTilesetResource
import org.hexworks.zircon.api.uievent.KeyCode
import org.hexworks.zircon.api.uievent.KeyboardEventType
import org.hexworks.zircon.api.uievent.Processed
import org.hexworks.zircon.api.uievent.UIEventPhase
import kotlin.concurrent.thread

class GameView (startingLevel: Int = 9): BaseView() {


    override val theme = ColorThemes.arc()
    private val graphicalTileset = GraphicalTilesetResource(
        width = 32,
        height = 32,
        path = javaClass.getResource("/graphic_tilesets/tetris_32x32.zip").path
    )

    private val graphicalTiles = Array<GraphicTile>(18) { i ->
        Tiles.newBuilder()
            .withName(GameConfig.TileNames[i])
            .withTileset(graphicalTileset)
            .buildGraphicTile()
    }

    var core = TetrisCore()
    val startLevel = startingLevel

    override fun onDock() {

        var score = 0

        var down = 0

        var move = 0

        var flip = 0

        val statPanel: Panel = Components.panel()
            .withSize(Sizes.create(GameConfig.SIDEBAR_WIDTH, GameConfig.WINDOW_HEIGHT - 8))
            .withAlignmentWithin(screen, ComponentAlignment.LEFT_CENTER)
            .wrapWithBox()
            .withBoxType(BoxType.SINGLE)
            .build()

        val board: Panel = Components.panel()
            .withSize(Sizes.create(22, 41))
            .withAlignmentWithin(screen, ComponentAlignment.CENTER)
            .wrapWithBox()
            .withTitle("TETRIS")
            .withBoxType(BoxType.SINGLE)
            .build()


        val tetrisLayer: Layer = Layers.newBuilder()
            .withTileset(graphicalTileset)
            .withSize(Sizes.create(10, 20))
            .withOffset(Positions.create(10, 5))
            .build()

        val nextLayer: Layer = Layers.newBuilder()
            .withTileset(graphicalTileset)
            .withSize(Sizes.create(4, 4))
            .withOffset(Positions.create(21, 11))
            .build()

        val linesPanel: Panel = Components.panel()
            .withSize(22, 5)
            .wrapWithBox()
            .withTitle("LINES")
            .withBoxType(BoxType.SINGLE)
            .withAlignmentAround(board, ComponentAlignment.TOP_CENTER)
            .build()

        val scorePanel: Panel = Components.panel()
            .withSize(Sizes.create(10, 7))
            .withBoxType(BoxType.SINGLE)
            .withTitle("SCORE")
            .wrapWithBox()
            .withAlignmentAround(board, ComponentAlignment.TOP_RIGHT)
            .build()

        val levelPanel: Panel = Components.panel()
            .withSize(Sizes.create(10, 5))
            .withBoxType(BoxType.SINGLE)
            .withTitle("LEVEL")
            .wrapWithBox()
            .withAlignmentAround(scorePanel, ComponentAlignment.BOTTOM_CENTER)
            .build()

        val nextBlockPanel: Panel = Components.panel()
            .withSize(10, 10)
            .withBoxType(BoxType.SINGLE)
            .wrapWithBox()
            .withAlignmentAround(levelPanel, ComponentAlignment.BOTTOM_CENTER)
            .build()

        val myScore = Components.label()
            .withText("000000")
            .withAlignmentWithin(scorePanel, ComponentAlignment.RIGHT_CENTER)
            .build()

        val myLevel = Components.label()
            .withText("000000")
            .withAlignmentWithin(levelPanel, ComponentAlignment.RIGHT_CENTER)
            .build()

        val myLines = Components.label()
            .withText("000000")
            .withAlignmentWithin(linesPanel, ComponentAlignment.CENTER)
            .build()

        scorePanel.addComponent(myScore)
        levelPanel.addComponent(myLevel)
        linesPanel.addComponent(myLines)
        screen.addComponent(nextBlockPanel)
        screen.addComponent(linesPanel)
        screen.addComponent(levelPanel)
        screen.addComponent(scorePanel)
        screen.addComponent(board)
        screen.addComponent(statPanel)
        screen.pushLayer(tetrisLayer)
        screen.pushLayer(nextLayer)

        thread {

            val gravity = arrayOf(48, 43, 38, 33, 28, 23, 18, 13, 8, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 2)
            var level = startLevel + 0
            var lines = 0
            var linesAfterProgress = 0

//            val keyStates = mutableMapOf<KeyCode, KeyboardEventType>()

            screen.onKeyboardEvent(KeyboardEventType.KEY_PRESSED) {

                    event, phase ->
                if (phase == UIEventPhase.TARGET) {
//                    keyStates[event.code] = KeyboardEventType.KEY_PRESSED
                    when (event.code) {
                        KeyCode.DOWN -> {
                            down = if (down == 0) 1 else down; println("DOWN pressed")
                        }
                        KeyCode.LEFT -> {
                            move = if (move == 0) -1 else move; println("LEFT pressed")
                        }
                        KeyCode.RIGHT -> {
                            move = if (move == 0) 1 else move; println("RIGHT pressed")
                        }
                        KeyCode.KEY_A -> {
                            flip = if (flip == 0) -1 else flip; println("KEY_A pressed")
                        }
                        KeyCode.KEY_S -> {
                            flip = if (flip == 0) 1 else flip; println("KEY_S pressed")
                        }
                        else -> {
                        }
                    }
                }
                Processed
            }

            screen.onKeyboardEvent(KeyboardEventType.KEY_RELEASED) { event, _ ->
                when (event.code) {
                    KeyCode.DOWN -> {
                        down = 0; println("DOWN released")
                    }
                    KeyCode.LEFT -> {
                        move = 0; println("LEFT released")
                    }

                    KeyCode.RIGHT -> {
                        move = 0; println("RIGHT released")
                    }
                    KeyCode.KEY_A -> {
                        flip = 0; println("KEY_A released")
                    }

                    KeyCode.KEY_S -> {
                        flip = 0; println("KEY_S released")
                    }
                    else -> {
                    }
                }
                Processed
            }

            while (true) {
                myScore.text = score.toString()
                myLevel.text = level.toString()
                myLines.text = lines.toString()

                core.newTick(flip, move, down = down, freq = gravity[level])
                if (core.isLocked()) {
                    break
                }
                lines += core.getClearedLines()
                linesAfterProgress += core.getClearedLines()
                when (core.getClearedLines()) {
                    1 -> score += 40 * (level + 1)
                    2 -> score += 100 * (level + 1)
                    3 -> score += 300 * (level + 1)
                    4 -> score += 1200 * (level + 1)
                    else -> {
                    }
                }
                if (startLevel <= level) {
                    if ((lines > (startLevel * 10 + 10)) or (lines > (Integer.max(100, (startLevel * 10 - 10))))) {
                        level++
                        linesAfterProgress = 0
                    }
                } else {
                    if (linesAfterProgress >= 10) {
                        level++
                        linesAfterProgress = 0
                    }
                }

                paintBoard(tetrisLayer)
                placeNext(nextLayer)
                move += if (move > 0) 1 else if (move < 0) -1 else 0
                flip += if (flip > 0) 1 else if (flip < 0) -1 else 0
                Thread.sleep(1000 / 60)
            }
            replaceWith(LevelView())
        }
    }

    fun paintBoard(tetrisLayer: Layer) {
        for (y in 0 until 20) {
            for (x in 0 until 10) {
                if (core.getCord(x, y) > 0) {
                    tetrisLayer.setTileAt(Positions.create(x, y), graphicalTiles[core.getCord(x, y)])
                } else {
                    tetrisLayer.setTileAt(Position.create(x, y), Tiles.empty())
                }
            }
        }
        for (i in core.getShape()) {
            tetrisLayer.setTileAt(
                Positions.create(core.getCords()[1] + i[1], core.getCords()[0] + i[0]),
                graphicalTiles[core.getShapeId() + 1]
            )
        }
    }

    fun placeNext(tetrisLayer: Layer) {
        val block0 = arrayOf(2, 2)

        for (y in 0 until 5) {
            for (x in 0 until 5) {
                tetrisLayer.setTileAt(Positions.create(x, y), Tiles.empty())
            }
        }

        for (bl in core.getNextShape()){
            tetrisLayer.setTileAt(
                Positions.create(block0[0] + bl[1], block0[1] + bl[0]),
                graphicalTiles[core.getNextShapeId() + 1]
            )
        }
    }
}