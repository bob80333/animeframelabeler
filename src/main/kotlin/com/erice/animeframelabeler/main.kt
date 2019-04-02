package com.erice.animeframelabeler

import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch


/**
 * Created by Eric Engelhart on 2019-03-26
 */

class AnimeFrameLabelerApp : App(LabelerView::class) {
    val controller: LabelerController by inject()

    override fun start(stage: Stage) {
        super.start(stage)
        controller.init()
    }
}

fun main() {
    launch<AnimeFrameLabelerApp>()
}