package lofitsky.fractal

import kotlin.math.pow


// https://stackoverflow.com/questions/1472514/convert-light-frequency-to-rgb/?#14917481
class ColorUtil {
    companion object {
        private const val gamma: Double = 0.80
        private const val intensityMax: Double = 255.0
        private const val SPECTRUM_BEGIN = 380.0
        private const val SPECTRUM_END = 780.0
        private const val SPECTRUM_WIDTH = SPECTRUM_END - SPECTRUM_BEGIN

        fun waveLengthToRGB(index: Int): Array<Byte> {
            val indexFactor = index.toDouble() / 255
            val indexFactor2 = indexFactor.pow(2)
            val waveLength = SPECTRUM_BEGIN + indexFactor2 * SPECTRUM_WIDTH
            return waveLengthToRGB(waveLength).map { (it.toDouble() * indexFactor).toInt().toByte() }.toTypedArray()
        }

        /**
         * Taken from Earl F. Glynn's web page:
         * [Spectra Lab Report](http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm)
         */
        private fun waveLengthToRGB(waveLength: Double): IntArray {
            val red: Double
            val green: Double
            val blue: Double

            if((waveLength >= 380) && (waveLength < 440)) {
                red = -(waveLength - 440) / (440 - 380)
                green = 0.0
                blue = 1.0
            } else if((waveLength >= 440) && (waveLength < 490)) {
                red = 0.0
                green = (waveLength - 440) / (490 - 440)
                blue = 1.0
            } else if((waveLength >= 490) && (waveLength < 510)) {
                red = 0.0
                green = 1.0
                blue = -(waveLength - 510) / (510 - 490)
            } else if((waveLength >= 510) && (waveLength < 580)) {
                red = (waveLength - 510) / (580 - 510)
                green = 1.0
                blue = 0.0
            } else if((waveLength >= 580) && (waveLength < 645)) {
                red = 1.0
                green = -(waveLength - 645) / (645 - 580)
                blue = 0.0
            } else if((waveLength >= 645) && (waveLength < 781)) {
                red = 1.0
                green = 0.0
                blue = 0.0
            } else {
                red = 0.0
                green = 0.0
                blue = 0.0
            }

            // Let the intensity fall off near the vision limits
            val factor = if((waveLength >= 380) && (waveLength < 420)) {
                0.3 + 0.7 * (waveLength - 380) / (420 - 380)
            } else if((waveLength >= 420) && (waveLength < 701)) {
                1.0
            } else if((waveLength >= 701) && (waveLength < 781)) {
                0.3 + 0.7 * (780 - waveLength) / (780 - 700)
            } else {
                0.0
            }


            val rgb = IntArray(3)

            // Don't want 0^x = 1 for x <> 0
            rgb[0] = if(red == 0.0) 0 else Math.round(intensityMax * (red * factor).pow(gamma)).toInt()
            rgb[1] = if(green == 0.0) 0 else Math.round(intensityMax * (green * factor).pow(gamma)).toInt()
            rgb[2] = if(blue == 0.0) 0 else Math.round(intensityMax * (blue * factor).pow(gamma)).toInt()

            return rgb
        }
    }
}
