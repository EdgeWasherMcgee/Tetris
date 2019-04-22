import org.hexworks.zircon.api.AppConfigs
import org.hexworks.zircon.api.Sizes

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