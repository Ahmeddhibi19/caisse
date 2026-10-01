package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.printing.PrintQueue

class FakePrintQueue : PrintQueue {

    var wakeUps = 0
        private set

    override fun start() = Unit

    override fun wake() {
        wakeUps++
    }
}
