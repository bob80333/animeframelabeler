package com.erice.animeframelabeler

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.FileChooser
import tornadofx.Controller
import tornadofx.alert
import tornadofx.onChange
import java.io.File


/**
 * Created by Eric Engelhart on 2019-03-26
 */

enum class FrameCategory {
    NOTHING, CHARACTER, OTHER, CAMERA, SCENE_END, SCENE_BEGIN
}

data class FrameLabels(var category: MutableSet<FrameCategory>)

class LabelerController : Controller() {

    var nImages = 0

    lateinit var videoFile: String

    lateinit var beforeBeforeCurrent: ObjectProperty<Image>
    lateinit var beforeCurrent: ObjectProperty<Image>
    lateinit var current: ObjectProperty<Image>
    lateinit var difference: ObjectProperty<Image>
    lateinit var afterCurrent: ObjectProperty<Image>
    lateinit var afterAfterCurrent: ObjectProperty<Image>

    lateinit var nothingCheck: BooleanProperty
    lateinit var beginCheck: BooleanProperty
    lateinit var endCheck: BooleanProperty
    lateinit var characterCheck: BooleanProperty
    lateinit var otherCheck: BooleanProperty
    lateinit var cameraCheck: BooleanProperty

    lateinit var skipSmallDifferenceCheck: BooleanProperty

    lateinit var nextButtonDisabled: BooleanProperty
    lateinit var backButtonDisabled: BooleanProperty

    lateinit var bottomLabel: StringProperty
    lateinit var differenceLabel: StringProperty
    lateinit var lastOperationLabel: StringProperty

    val currentFrame = FrameLabels(mutableSetOf())
    lateinit var videoManager: VideoManager

    fun init() {
        nothingCheck.onChange {
            if (it) {
                beginCheck.value = false
                endCheck.value = false
                characterCheck.value = false
                otherCheck.value = false
                cameraCheck.value = false
            }
            updateCurrentFrameCategory()
        }

        beginCheck.onChange {
            if (it) {
                nothingCheck.value = false
                endCheck.value = false
                characterCheck.value = false
                otherCheck.value = false
                cameraCheck.value = false
            }
            updateCurrentFrameCategory()
        }

        endCheck.onChange {
            if (it) {
                nothingCheck.value = false
                beginCheck.value = false
                characterCheck.value = false
                otherCheck.value = false
                cameraCheck.value = false
            }
            updateCurrentFrameCategory()
        }

        characterCheck.onChange {
            if (it) {
                if (nothingCheck.value) {
                    nothingCheck.value = false
                }

                if (beginCheck.value) {
                    beginCheck.value = false
                }

                if (endCheck.value) {
                    endCheck.value = false
                }
            }
            updateCurrentFrameCategory()
        }

        otherCheck.onChange {
            if (it) {
                if (nothingCheck.value) {
                    nothingCheck.value = false
                }

                if (beginCheck.value) {
                    beginCheck.value = false
                }

                if (endCheck.value) {
                    endCheck.value = false
                }
            }
            updateCurrentFrameCategory()
        }

        cameraCheck.onChange {
            if (it) {
                if (nothingCheck.value) {
                    nothingCheck.value = false
                }

                if (beginCheck.value) {
                    beginCheck.value = false
                }

                if (endCheck.value) {
                    endCheck.value = false
                }
            }
            updateCurrentFrameCategory()
        }
    }

    private fun updateCurrentFrameCategory() {
        val currentCategory = currentFrame.category
        currentCategory.clear()
        if (nothingCheck.value) {
            currentCategory.add(FrameCategory.NOTHING)
            nextButtonDisabled.value = currentCategory.isEmpty()
            return
        }

        if (beginCheck.value) {
            currentCategory.add(FrameCategory.SCENE_BEGIN)
            nextButtonDisabled.value = currentCategory.isEmpty()
            return
        }

        if (endCheck.value) {
            currentCategory.add(FrameCategory.SCENE_END)
            nextButtonDisabled.value = currentCategory.isEmpty()
            return
        }

        if (characterCheck.value) {
            currentCategory.add(FrameCategory.CHARACTER)
        }

        if (cameraCheck.value) {
            currentCategory.add(FrameCategory.CAMERA)
        }

        if (otherCheck.value) {
            currentCategory.add(FrameCategory.OTHER)
        }

        nextButtonDisabled.value = currentCategory.isEmpty()
    }

    private fun updateChecksFromFrame() {
        val currentCategory = currentFrame.category

        // reset all
        nothingCheck.value = false
        characterCheck.value = false
        otherCheck.value = false
        cameraCheck.value = false
        beginCheck.value = false
        endCheck.value = false

        if (currentCategory.contains(FrameCategory.NOTHING)) {
            nothingCheck.value = true
            return
        }

        if (currentCategory.contains(FrameCategory.SCENE_BEGIN)) {
            beginCheck.value = true
            return
        }

        if (currentCategory.contains(FrameCategory.SCENE_END)) {
            endCheck.value = true
            return
        }

        if (currentCategory.contains(FrameCategory.CHARACTER)) {
            characterCheck.value = true
        }

        if (currentCategory.contains(FrameCategory.CAMERA)) {
            cameraCheck.value = true
        }

        if (currentCategory.contains(FrameCategory.OTHER)) {
            otherCheck.value = true
        }
    }

    fun chooseVideoFile() {
        val fileChooser = FileChooser()
        fileChooser.title = "Choose the video file"
        val file = fileChooser.showOpenDialog(this.primaryStage.owner)

        if (file == null) {
            lastOperationLabel.value = "Video load canceled"
            return
        }
        if (!(file.exists() && file.canRead())) {
            alert(Alert.AlertType.WARNING, "That file doesn't exist, or I can't read it.")
            lastOperationLabel.value = "Couldn't read video file"
            return
        }

        videoFile = file.absolutePath
        lastOperationLabel.value = "Loading video file..."
        runAsync {

            videoManager = VideoManager(
                beforeBeforeCurrent,
                beforeCurrent,
                current,
                difference,
                afterCurrent,
                afterAfterCurrent,
                backButtonDisabled,
                nextButtonDisabled,
                file
            )

            videoManager.updateImageViews(false)
            updateCurrentFrameCategory()

        } ui {
            updateLabels()
            lastOperationLabel.value = "Video loaded"
        }

    }

    fun saveCsv() {
        val chooser = FileChooser()
        val video = File(videoFile)
        chooser.initialDirectory = File(video.parent)
        chooser.initialFileName = video.nameWithoutExtension + ".csv"
        // the only option is csv files.
        chooser.extensionFilters.clear()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("CSV files", ".csv"))

        val fileToSave = chooser.showSaveDialog(this.primaryStage.owner)

        if (fileToSave == null) {
            lastOperationLabel.value = "CSV save canceled"
            return
        }

        fileToSave.createNewFile()
        if (!(fileToSave.exists() && fileToSave.canWrite())) {
            alert(Alert.AlertType.WARNING, "I couldn't create that file, or I couldn't write to it.")
            lastOperationLabel.value = "Couldn't write to csv file"
            return
        }

        lastOperationLabel.value = "Saving to csv..."
        runAsync {
            val data = StringBuilder()
            data.append("File, $videoFile\n")
            data.append("frame number, label1, label 2 (optional), label 3 (optional)\n")
            videoManager.labeledList.forEachIndexed { index, frameLabels ->
                data.append("$index, ${frameLabels.category.joinToString(", ")}\n")
            }

            fileToSave.writeText(data.toString())
        } ui {
            lastOperationLabel.value = "Saved to csv"
        }

    }

    fun loadCsv() {
        val chooser = FileChooser()
        // the only option is csv files.
        chooser.extensionFilters.clear()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("CSV files", ".csv"))

        val fileToLoad = chooser.showOpenDialog(this.primaryStage.owner)
        if (fileToLoad == null) {
            lastOperationLabel.value = "CSV loading canceled"
            return
        }

        if (!(fileToLoad.exists() && fileToLoad.canRead())) {
            alert(Alert.AlertType.ERROR, "That file doesn't exist, or I couldn't read it.")
            lastOperationLabel.value = "Couldn't read csv file"
            return
        }

        lastOperationLabel.value = "Loading csv file..."

        runAsync {
            val lines = fileToLoad.readLines()
            val video = File(lines[0].split(", ")[1].trim())
            videoFile = video.absolutePath

            videoManager = VideoManager(
                beforeBeforeCurrent,
                beforeCurrent,
                current,
                difference,
                afterCurrent,
                afterAfterCurrent,
                backButtonDisabled,
                nextButtonDisabled,
                video
            )

            lines.forEachIndexed { index, s ->
                if (index == 0 || index == 1) {

                } else {
                    val pieces = s.split(", ")
                    var i = -1
                    val frameLabels = FrameLabels(mutableSetOf())
                    pieces.forEachIndexed { index, s ->
                        if (index == 0) {
                            i = s.trim().toInt()
                        } else {
                            when (s.trim()) {
                                "NOTHING" -> frameLabels.category.add(FrameCategory.NOTHING)
                                "SCENE_BEGIN" -> frameLabels.category.add(FrameCategory.SCENE_BEGIN)
                                "SCENE_END" -> frameLabels.category.add(FrameCategory.SCENE_END)
                                "OTHER" -> frameLabels.category.add(FrameCategory.OTHER)
                                "CAMERA" -> frameLabels.category.add(FrameCategory.CAMERA)
                                "CHARACTER" -> frameLabels.category.add(FrameCategory.CHARACTER)
                            }
                        }
                    }
                    videoManager.labeledList.add(i, frameLabels)
                }
            }

            videoManager.position = videoManager.labeledList.size - 1
            videoManager.updateImageViews(false)
            currentFrame.category.clear()
            currentFrame.category.addAll(videoManager.labeledList.last().category)
        } ui {
            lastOperationLabel.value = "CSV file loaded"
            updateLabels()
            updateChecksFromFrame()
        }
    }

    fun next() {
        lastOperationLabel.value = "Stepping forward..."
        runAsync {
            updateCurrentFrameCategory()
            videoManager.next(currentFrame.category, skipSmallDifferenceCheck.value)

        } ui {
            updateLabels()
            nextButtonDisabled.value = false
            lastOperationLabel.value = "Stepped forward"
        }
    }

    fun back() {
        lastOperationLabel.value = "Stepping back..."
        runAsync {
            val frameLabel = videoManager.labeledList.last()
            currentFrame.category.clear()
            currentFrame.category.addAll(frameLabel.category)
            videoManager.labeledList.removeAt(videoManager.labeledList.size - 1)
            updateChecksFromFrame()
            videoManager.position--
            videoManager.updateImageViews(back = true)
        } ui {
            updateLabels()
            updateChecksFromFrame()
            backButtonDisabled.value = false
            lastOperationLabel.value = "Stepped back"
        }
    }

    fun repeatLast2() {
        lastOperationLabel.value = "Repeating Last 2..."
        runAsync {
            if (videoManager.labeledList.size >= 2 && videoManager.position + 2 <= videoManager.maxPosition) {
                videoManager.labeledList.add(videoManager.labeledList[videoManager.labeledList.size - 2])
                videoManager.labeledList.add(videoManager.labeledList[videoManager.labeledList.size - 2])
                videoManager.position++
                videoManager.position++
                videoManager.updateImageViews(true)
                videoManager.updateImageViews(true)
            }
        } ui {
            updateLabels()
            lastOperationLabel.value = "Repeated Last 2"
        }
    }

    fun repeatLast3() {
        lastOperationLabel.value = "Repeating Last 3..."
        runAsync {
            if (videoManager.labeledList.size >= 3 && videoManager.position + 3 <= videoManager.maxPosition) {
                videoManager.labeledList.add(videoManager.labeledList[videoManager.labeledList.size - 3])
                videoManager.labeledList.add(videoManager.labeledList[videoManager.labeledList.size - 3])
                videoManager.labeledList.add(videoManager.labeledList[videoManager.labeledList.size - 3])
                videoManager.position++
                videoManager.position++
                videoManager.position++
                videoManager.updateImageViews(true)
                videoManager.updateImageViews(true)
                videoManager.updateImageViews(true)
            }
        }.setOnSucceeded {
            updateLabels()
            lastOperationLabel.value = "Repeated Last 3"
        }

    }

    private fun updateLabels() {
        bottomLabel.value = "${videoManager.labeledList.size}/${videoManager.maxPosition + 1}"
        differenceLabel.value = "${valueImageDifference()}"
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