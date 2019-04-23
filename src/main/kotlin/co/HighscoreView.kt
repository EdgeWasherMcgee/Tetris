package co

import co.views.LevelView
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.component.ComponentAlignment
import org.hexworks.zircon.api.extensions.onKeyboardEvent
import org.hexworks.zircon.api.graphics.BoxType
import org.hexworks.zircon.api.mvc.base.BaseView
import org.hexworks.zircon.api.uievent.KeyCode
import org.hexworks.zircon.api.uievent.KeyboardEventType
import org.hexworks.zircon.api.uievent.Processed

class HighscoreView(sc: Int) : BaseView() {

    val score = sc.toString()

    override val theme = ColorThemes.arc()

    override fun onDock() {

        val box = Components.panel()
            .withSize(13, 5)
            .wrapWithBox()
            .withAlignmentWithin(screen, ComponentAlignment.CENTER)
            .withBoxType(BoxType.DOUBLE)
            .withTitle("SCORE!")
            .build()

//        val highscoreHeader = Components.header()
//            .withSize("SCORE!".length, 1)
//            .withText("SCORE!")
//            .withAlignmentWithin(box, ComponentAlignment.TOP_CENTER)
//            .build()

        val text = Components.header()
            .withSize(score.length, 1)
            .withText(score)
            .withAlignmentWithin(box, ComponentAlignment.BOTTOM_CENTER)
            .build()

        screen.addComponent(box)
        box.addComponent(text)
//        box.addComponent(highscoreHeader)

        screen.onKeyboardEvent(KeyboardEventType.KEY_PRESSED) {
            event, _ ->

            if (event.code == KeyCode.ENTER) {
                replaceWith(LevelView())
                close()
                Processed
            }

            println(event.code)

            Processed
        }
    }
}