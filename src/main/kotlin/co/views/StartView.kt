package co.views

import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.component.TextBox
import org.hexworks.zircon.api.extensions.onKeyboardEvent
import org.hexworks.zircon.api.mvc.base.BaseView
import org.hexworks.zircon.api.uievent.KeyCode
import org.hexworks.zircon.api.uievent.KeyboardEventType
import org.hexworks.zircon.api.uievent.Processed

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