package com.neowise.tracko.view.sessions

interface SessionsCallback {

    fun view(position: Int)

    fun rename(position: Int)

    fun delete(position: Int)
}