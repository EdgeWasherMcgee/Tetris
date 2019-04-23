package co.views

import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.extensions.onKeyboardEvent
import org.hexworks.zircon.api.graphics.BoxType
import org.hexworks.zircon.api.mvc.base.BaseView
import org.hexworks.zircon.api.uievent.KeyCode
import org.hexworks.zircon.api.uievent.KeyboardEventType
import org.hexworks.zircon.api.uievent.Processed
import kotlin.math.floor

class LevelView : BaseView() {

    override val theme = ColorThemes.arc()

    override fun onDock() {

        var curFocus = 0

        val buttons = Array(10) {
            Components.button()
                .withSize(3, 1)
                .withBoxType(BoxType.SINGLE)
                .withPosition(22 + (it % 5) * 3,+ 27 + floor(it / 5.0).toInt())
                .withText(it.toString())
                .build()
        }

        screen.applyColorTheme(theme)
        buttons.forEach { screen.addComponent(it) }

        screen.onKeyboardEvent(KeyboardEventType.KEY_PRESSED) {

                event, _ ->

            buttons[curFocus].clearFocus()

            when (event.code) {
                KeyCode.DOWN -> {
                    curFocus = (curFocus + 5).rem(10); if (curFocus < 0) curFocus += 10
                }
                KeyCode.UP -> {
                    curFocus = (curFocus - 5).rem(10); if (curFocus < 0) curFocus += 10
                }
                KeyCode.LEFT -> {
                    curFocus = (curFocus - 1).rem(10); if (curFocus < 0) curFocus += 10
                }
                KeyCode.RIGHT -> {
                    curFocus = (curFocus + 1).rem(10); if (curFocus < 0) curFocus += 10
                }
                KeyCode.ENTER -> {
                    replaceWith(GameView(curFocus))
                    close()
                    Processed
                }
                else -> {
                }

            }

            buttons[curFocus].requestFocus()
            Processed
        }
        buttons[0].requestFocus()

    }
}