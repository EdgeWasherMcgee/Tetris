import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.component.Panel
import org.hexworks.zircon.api.component.TextBox
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

//import TetrisCore

object GameConfig {

    val SIDEBAR_WIDTH = 8
    val WINDOW_WIDTH = 60
    val WINDOW_HEIGHT = 60

    val WORLD_SIZE = Sizes.create(WINDOW_WIDTH, WINDOW_HEIGHT)

    val TileNames = arrayOf(
        "BlueSquare",
        "LightBlue",
        "Blue",
        "PurpleSquare",
        "Red",
        "Purple",
        "GreenSquare",
        "Yellow",
        "Green",
        "CeriseSquare",
        "Lime",
        "Cerise",
        "PinkSquare",
        "LightPink",
        "Pink",
        "OrangeSquare",
        "LightOrange",
        "Orange"
    )

    fun buildAppConfig() = AppConfigs.newConfig()
        .enableBetaFeatures()
        .withSize(Sizes.create(WINDOW_WIDTH, WINDOW_HEIGHT))
        .build()

}


class StartView : BaseView() {

    override val theme = ColorThemes.arc()

    override fun onDock() {

        val header: TextBox = Components.textBox()
            .withContentWidth("Press ENTER to continue".length)
            .addHeader("        TETRIS         ")
            .addParagraph("Press ENTER to continue")
            .addNewLine()
            .withAlignmentWithin(screen, ComponentAlignment.CENTER)
            .build()

        screen.applyColorTheme(theme)

        screen.onKeyboardEvent(KeyboardEventType.KEY_RELEASED) {

                event, _ ->

            if (event.code == KeyCode.ENTER) {
                replaceWith(GameView())
                close()
                Processed
            }
            Processed
        }

        screen.addComponent(header)

    }

}


class GameView : BaseView() {

    override val theme = ColorThemes.arc()
    private val graphicalTileset = GraphicalTilesetResource(
        width = 32,
        height = 32,
        path = "/home/co/IdeaProjects/Tetris2/src/main/resources/graphic_tilsets/tetris_32x32.zip"
    )

    private val graphicalTiles = Array<GraphicTile>(18) { i ->
        Tiles.newBuilder()
            .withName(GameConfig.TileNames[i])
            .withTileset(graphicalTileset)
            .buildGraphicTile()
    }

    var tC = TetrisCore()


    override fun onDock() {

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
            .withSize(Sizes.create(22, 42))
            .withAlignmentWithin(screen, ComponentAlignment.CENTER)
            .wrapWithBox()
            .withTitle("TETRIS")
            .wrapWithShadow()
            .withBoxType(BoxType.SINGLE)
            .build()


        val tetrisLayer: Layer = Layers.newBuilder()
            .withTileset(graphicalTileset)
            .withSize(Sizes.create(10, 20))
            .withOffset(Positions.create(10, 5))
            .build()

        screen.pushLayer(tetrisLayer)
        screen.addComponent(board)
        screen.addComponent(statPanel)

        thread {

            var clear = false

            screen.onKeyboardEvent(KeyboardEventType.KEY_PRESSED) {

                    event, phase ->
                if (phase == UIEventPhase.TARGET) {
                    when (event.code) {
                        KeyCode.DOWN -> {
                            down = 1; println("DOWN pressed")
                        }
                        KeyCode.LEFT -> {
                            move = -1; println("LEFT pressed")
                        }
                        KeyCode.RIGHT -> {
                            move = 1; println("RIGHT pressed")
                        }
                        KeyCode.KEY_A -> {
                            flip = -1; println("KEY_A pressed")
                        }
                        KeyCode.KEY_S -> {
                            flip = 1; println("KEY_S pressed")
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
                tC.newTick(flip, move, down = down, freq = 6)
                paintBoard(tetrisLayer)
                move += if (move > 0) 1 else if (move < 0) -1 else 0
                flip += if (flip > 0) 1 else if (flip < 0) -1 else 0
                Thread.sleep(1000 / 60)
            }
        }
    }


    fun paintBoard(tL: Layer) {
        for (y in 0 until 20) {
            for (x in 0 until 10) {
                if (tC.getCord(x, y) > 0) {
                    tL.setTileAt(Positions.create(x, y), graphicalTiles[0])
                } else {
                    tL.setTileAt(Position.create(x, y), Tiles.empty())
                }
            }
        }
        for (i in tC.getShape()) {
            tL.setTileAt(Positions.create(tC.blCords[1] + i[1], tC.blCords[0] + i[0]), graphicalTiles[1])
        }
    }
}


fun main() {
    val application = SwingApplications.startApplication(GameConfig.buildAppConfig())
    application.dock(StartView())
}