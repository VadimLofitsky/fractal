package lofitsky.fractal

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelFormat
import javafx.scene.image.PixelWriter
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.kotlinmath.complex
import java.nio.ByteBuffer

class FractalDrawer {
    private val THREADS_COUNT = 8

    private lateinit var gc: GraphicsContext
    private lateinit var pixelWriter: PixelWriter
    private lateinit var pixelFormat: PixelFormat<ByteBuffer>

    var canvas: Canvas? = null
        set(value) {
            gc = value!!.graphicsContext2D
            pixelWriter = gc.pixelWriter
            pixelFormat = PixelFormat.getByteRgbInstance()
            field = value
        }

    private val buffer = ByteBuffer.allocate(CLIENT_WIDTHI * CLIENT_HEIGHTI * 3)

    private val colors = (0..255).map { ColorUtil.waveLengthToRGB(it) }.toTypedArray()

    private fun fillBufPart(x: Int, y: Int, fractalInfo: FractalInfo) {
        val (iterations, value) = calcPoint(x, y, fractalInfo)
        val bufIndex = (y * CLIENT_WIDTHI + x) * 3
        val colorIndex = (iterations.toDouble() * 255 / fractalInfo.depth).toInt()
        val (r, g, b) = colors[colorIndex]
        buffer.put(bufIndex, r)
        buffer.put(bufIndex + 1, g)
        buffer.put(bufIndex + 2, b)
    }

    private fun calcPoint(_x: Int, _y: Int, fractalInfo: FractalInfo): Pair<Int, Double> {
        val (x, y) = fractalInfo.rescaleToInner(_x, _y)
        val z0 = fractalInfo.z0
        val c = complex(x, y)
        var i = 0
        var zN = z0
        var zN1 = zN * zN + c
        var zN1Mod2 = zN1.re * zN1.re + zN1.im * zN1.im
        while(zN1Mod2 < 4 && i < fractalInfo.depth) {
            zN = zN1
            zN1 = zN * zN + c
            zN1Mod2 = zN1.re * zN1.re + zN1.im * zN1.im
            ++i
        }

        return i to zN1Mod2
    }

    fun draw(fractalInfo: FractalInfo) {
        runBlocking(newFixedThreadPoolContext(THREADS_COUNT, "drawPoolContext")) {
            (0 until CLIENT_HEIGHTI)
                .chunked(10)
                .map { chunk ->
                    async {
                        chunk.map { y ->
                            (0 until CLIENT_WIDTHI).forEach { x ->
                                fillBufPart(x, y, fractalInfo)
                            }
                        }
                    }
                }
            .forEach { it.await() }
        }

        pixelWriter.setPixels(0, 0, CLIENT_WIDTHI, CLIENT_HEIGHTI, pixelFormat, buffer, CLIENT_WIDTH3I)
    }
}
