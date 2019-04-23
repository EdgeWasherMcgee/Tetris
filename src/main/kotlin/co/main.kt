package co

import co.views.LevelView
import org.hexworks.zircon.api.SwingApplications

fun main(args: Array<String>) {
    val application = SwingApplications.startApplication(GameConfig.buildAppConfig())

    application.dock(LevelView())
}