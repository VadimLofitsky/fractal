package lofitsky.fractal

import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.stage.Stage
import org.kotlinmath.Complex
import org.kotlinmath.complex
import kotlin.math.log10

class FractalInfo(
    var centerX: Double,
    var centerY: Double,
    var width: Double,
    var height: Double,
    var z0: Complex,
) {
    private val kx
        get() = width / CLIENT_WIDTHD
    private val ky
        get() = - height / CLIENT_HEIGHTD
    private val cx
        get() = -width / 2
    private val cy
        get() = height / 2
    val depth = (log10(width) * (-100) + 100).toInt()

    fun rescaleToInner(outerX: Int, outerY: Int): Pair<Double, Double> = kx * outerX + cx + centerX to ky * outerY + cy + centerY
    fun rescaleToInner(outerX: Double, outerY: Double): Pair<Double, Double> = kx * outerX + cx + centerX to ky * outerY + cy + centerY

    fun moveCenterTo(outerX: Double, outerY: Double, scale: Double) {
        rescaleToInner(outerX, outerY).also {
            centerX = it.first
            centerY = it.second
        }
        width *= scale
        height *= scale
    }
}

class FractalApp : Application() {
    companion object {
        private val screenK = 10.0 / 16
        private val frCx = -.75
        private val frCy = 0.0
        private val frZ0 = complex(0.0, 0.0)
        private val frWidth = 3.0
        private val frHgt = frWidth * screenK

        private val fractalDrawer = FractalDrawer()
        private val fractalInfo = FractalInfo(frCx, frCy, frWidth, frHgt, frZ0)
    }

    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(FractalApp::class.java.getResource("fractal-app-view.fxml"))

        val scene = Scene(fxmlLoader.load())

        stage.apply {
            title = "Fractalz"
            setScene(scene)
            isMaximized = true
            show()
        }

        (scene.lookup("#canvas") as Canvas)
            .also {
                it.addEventHandler(ScrollEvent.SCROLL, ZoomHandler)
                it.addEventHandler(MouseEvent.MOUSE_RELEASED, ClickHandler)
                fractalDrawer.canvas = it
            }
        fractalDrawer.draw(fractalInfo)
    }

    private object ZoomHandler : EventHandler<ScrollEvent> {
        override fun handle(event: ScrollEvent) {
            val deltaY = event.deltaY
            if(deltaY == 0.0) return
            val multiplierY = event.multiplierY
            val wheelUnits = deltaY / multiplierY
            val keyK = if(event.isShortcutDown) 2.0 else 1.0
            val finalZoomK = if(deltaY < 0) -ZOOM_K * wheelUnits * keyK else 1.0 / (ZOOM_K * wheelUnits * keyK)
            fractalInfo.moveCenterTo(event.sceneX, event.sceneY, finalZoomK)
            fractalDrawer.draw(fractalInfo)
        }
    }

    private object ClickHandler : EventHandler<MouseEvent> {
        override fun handle(event: MouseEvent) {
            fractalInfo.moveCenterTo(event.sceneX, event.sceneY, 1.0)
            fractalDrawer.draw(fractalInfo)
        }
    }
}

fun main() {
    Application.launch(FractalApp::class.java)
}
