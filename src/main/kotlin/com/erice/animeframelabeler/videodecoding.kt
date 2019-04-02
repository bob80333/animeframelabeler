package com.erice.animeframelabeler

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.io.File


/**
 * Created by Eric Engelhart on 2019-03-31
 */

class VideoManager(
    private val beforeBeforeCurrent: ObjectProperty<Image>,
    private val beforeCurrent: ObjectProperty<Image>,
    private val current: ObjectProperty<Image>,
    private val difference: ObjectProperty<Image>,
    private val afterCurrent: ObjectProperty<Image>,
    private val afterAfterCurrent: ObjectProperty<Image>,
    val backDisable: BooleanProperty,
    private val nextDisable: BooleanProperty,
    private val videoFile: File
) {
    private val frameGrabber: FFmpegFrameGrabber = FFmpegFrameGrabber(videoFile)
    private val frameConverter = Java2DFrameConverter()

    var position = 0
    val minPosition = 0
    val maxPosition: Int

    init {
        frameGrabber.start()
        frameGrabber.imageHeight = 256
        frameGrabber.imageWidth = 256
        maxPosition = frameGrabber.lengthInVideoFrames - 1
    }

    val labeledList = mutableListOf<FrameLabels>()

    val nothingFrame = FrameLabels(mutableSetOf(FrameCategory.NOTHING))
    val emptyImage = SwingFXUtils.toFXImage(BufferedImage(256, 256, 2), null)!!

    fun next(labels: Set<FrameCategory>, skipSmallDifferences: Boolean) {
        val currentFrame = FrameLabels(mutableSetOf())
        currentFrame.category.addAll(labels)
        position++
        checkNextDisable()
        updateImageViews(true)
        labeledList.add(currentFrame)
        if (skipSmallDifferences) {
            while (position < maxPosition) {
                val difference = valueImageDifference()
                if (difference > 111_000) {
                    break
                }
                labeledList.add(nothingFrame)
                position++
                updateImageViews(true)
            }
        }


    }

    fun checkNextDisable() {
        if (position + 1 > maxPosition) {
            nextDisable.value = true
        }
    }

    fun updateImageViews(next: Boolean = false, back: Boolean = false) {
        if (next) {
            if (position > maxPosition) {
                position = maxPosition
            } else {
                beforeBeforeCurrent.set(beforeCurrent.get())
                beforeCurrent.set(current.get())
                current.set(afterCurrent.get())
                afterCurrent.set(afterAfterCurrent.get())
                difference.set(imageDifference(beforeCurrent.get(), current.get()))

                if (position + 2 <= maxPosition) {
                    afterAfterCurrent.set(getFrame())
                } else {
                    afterAfterCurrent.set(emptyImage)
                }

                return
            }
        }

        var justGrab = false
        if (position - 2 >= minPosition) {
            beforeBeforeCurrent.set(getFrame(position - 2))
            justGrab = true
        } else {
            beforeBeforeCurrent.set(emptyImage)
        }

        if (position - 1 >= minPosition) {
            if (justGrab) {
                beforeCurrent.set(getFrame())
            } else {
                beforeCurrent.set(getFrame(position - 1))
                justGrab = true
            }
        } else {
            beforeCurrent.set(emptyImage)
        }

        if (position <= maxPosition) {
            if (justGrab) {
                current.set(getFrame())
            } else {
                current.set(getFrame(position))
            }
        } else {
            position = maxPosition
            current.set(getFrame(position))
        }

        difference.set(imageDifference(beforeCurrent.get(), current.get()))

        if (position + 1 <= maxPosition) {
            if (justGrab) {
                afterCurrent.set(getFrame())
            } else {
                afterCurrent.set(getFrame(position + 1))
            }
        } else {
            afterCurrent.set(emptyImage)
        }

        if (position + 2 <= maxPosition) {
            if (justGrab) {
                afterAfterCurrent.set(getFrame())
            } else {
                afterAfterCurrent.set(getFrame(position + 2))
            }
        } else {
            afterAfterCurrent.set(emptyImage)
        }
    }

    private fun getFrame(frameNumber: Int): Image {
        frameGrabber.setVideoFrameNumber(frameNumber)
        return SwingFXUtils.toFXImage(frameConverter.convert(frameGrabber.grabImage()), null)
    }

    private fun getFrame(): Image {
        return SwingFXUtils.toFXImage(frameConverter.convert(frameGrabber.grabImage()), null)
    }


    fun imageDifference(image0: Image, image1: Image): Image {
        val result = BufferedImage(256, 256, 2)
        for (y in 0..255) {
            for (x in 0..255) {
                val argb0 = image0.pixelReader.getArgb(x, y)
                val argb1 = image1.pixelReader.getArgb(x, y)

                val r0 = argb0 shr 16 and 0xFF
                val g0 = argb0 shr 8 and 0xFF
                val b0 = argb0 and 0xFF

                val r1 = argb1 shr 16 and 0xFF
                val g1 = argb1 shr 8 and 0xFF
                val b1 = argb1 and 0xFF

                val rDiff = Math.abs(r1 - r0)
                val gDiff = Math.abs(g1 - g0)
                val bDiff = Math.abs(b1 - b0)

                val diff = 255 shl 24 or (rDiff shl 16) or (gDiff shl 8) or bDiff
                result.setRGB(x, y, diff)
            }
        }

        return SwingFXUtils.toFXImage(result, null)
    }

    fun valueImageDifference(): Long {
        val differenceImg = difference.get()
        var sum: Long = 0
        for (y in 0..255) {
            for (x in 0..255) {
                val argb = differenceImg.pixelReader.getArgb(x, y)

                val r = argb shr 16 and 0xFF
                val g = argb shr 8 and 0xFF
                val b = argb and 0xFF

                sum += r
                sum += g
                sum += b
            }
        }

        return sum
    }
}