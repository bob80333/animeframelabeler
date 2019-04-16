package com.erice.animeframelabeler

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import tornadofx.*


/**
 * Created by Eric Engelhart on 2019-03-26
 */

class LabelerView : View() {
    private val controller: LabelerController by inject()
    override val root = borderpane {
        prefWidth = 1200.0
        prefHeight = 675.0
        top {
            prefWidth = 1200.0
            buttonbar {
                button("Choose Video File") {
                    setOnAction { controller.chooseVideoFile() }
                }
                button("Save Csv of Processed Frames") {
                    setOnAction { controller.saveCsv() }
                }
                button("Load Csv of Processed Frames") {
                    setOnAction { controller.loadCsv() }
                }
            }
        }

        center {
            prefWidth = 1200.0
            hbox {
                prefWidth = 1200.0
                alignment = Pos.CENTER
                vbox {
                    prefWidth = 112.0
                    alignment = Pos.TOP_CENTER
                    label("Current Image-2:")

                    imageview {
                        fitWidth = 112.0
                        fitHeight = 112.0
                        controller.beforeBeforeCurrent = imageProperty()
                    }
                }
                separator {}
                vbox {
                    prefWidth = 112.0
                    alignment = Pos.TOP_CENTER
                    label("Current Image-1:")

                    imageview {
                        fitWidth = 112.0
                        fitHeight = 112.0
                        controller.beforeCurrent = imageProperty()
                    }
                }
                separator {}
                vbox {
                    prefWidth = 224.0
                    alignment = Pos.CENTER
                    label("Current Image:")

                    imageview {
                        fitWidth = 224.0
                        fitHeight = 224.0
                        controller.current = imageProperty()
                    }

                    label("Difference with image - 1")

                    imageview {
                        fitWidth = 224.0
                        fitHeight = 224.0
                        controller.difference = imageProperty()
                    }
                }
                separator {}
                vbox {
                    prefWidth = 112.0
                    alignment = Pos.TOP_CENTER
                    label("Current Image+1:")

                    imageview {
                        fitWidth = 112.0
                        fitHeight = 112.0
                        controller.afterCurrent = imageProperty()
                    }
                }
                separator {}
                vbox {
                    prefWidth = 112.0
                    alignment = Pos.TOP_CENTER
                    label("Current Image-2:")

                    imageview {
                        fitWidth = 112.0
                        fitHeight = 112.0
                        controller.afterAfterCurrent = imageProperty()
                    }
                }
            }
        }

        bottom {
            vbox {
                prefWidth = 1200.0
                prefHeight = 120.0
                hbox {
                    alignment = Pos.CENTER
                    prefWidth = 1200.0
                    prefHeight = 60.0
                    paddingAllProperty.value = 10.0
                    spacing = 12.5

                    checkbox("Nothing Frame") {
                        controller.nothingCheck = selectedProperty()
                    }

                    checkbox("Scene Begin") {
                        controller.beginCheck = selectedProperty()
                    }

                    checkbox("Scene End") {
                        controller.endCheck = selectedProperty()
                    }

                    checkbox("Character Frame") {
                        HBox.setMargin(this, Insets(0.0, 0.0, 0.0, 10.0))
                        controller.characterCheck = selectedProperty()
                    }

                    checkbox("Other Frame") {
                        controller.otherCheck = selectedProperty()
                    }

                    checkbox("Camera Frame") {
                        controller.cameraCheck = selectedProperty()
                    }

                    button("Repeat Last 2 Frames") {
                        HBox.setMargin(this, Insets(0.0, 0.0, 0.0, 20.0))
                        setOnAction { controller.repeatLast2() }
                    }

                    button("Repeat Last 3 Frames") {
                        setOnAction { controller.repeatLast3() }
                    }

                }

                hbox {
                    alignment = Pos.CENTER
                    prefWidth = 1200.0
                    prefHeight = 60.0
                    paddingAllProperty.value = 10.0
                    spacing = 10.0

                    button("Back") {
                        setOnAction {
                            this.isDisable = true
                            controller.back()
                        }
                        controller.backButtonDisabled = disableProperty()
                        isDisable = false
                    }

                    button("Next") {
                        HBox.setMargin(this, Insets(0.0, 20.0, 0.0, 0.0))
                        controller.nextButtonDisabled = disableProperty()
                        setOnAction {
                            this.isDisable = true
                            controller.next()
                        }
                    }

                    checkbox("Auto NearNothing Frames") {
                        controller.skipSmallDifferenceCheck = selectedProperty()
                        HBox.setMargin(this, Insets(0.0, 20.0, 0.0, 0.0))
                    }

                    label("?/?") {
                        controller.bottomLabel = textProperty()
                    }

                    label("Difference Image Total Value: ") {
                        controller.differenceLabel = textProperty()
                    }

                    separator(Orientation.VERTICAL)

                    label("Anime Frame Labeler Opened") {
                        controller.lastOperationLabel = textProperty()
                    }
                }
            }
        }
    }

}