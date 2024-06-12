package com.intec.template.robot.listeners

interface SpeechRecognitionListener {
    fun onSpeechPartialResult(result: String)
    fun onSpeechFinalResult(result: String)
    // Añadir más métodos según sea necesario
}