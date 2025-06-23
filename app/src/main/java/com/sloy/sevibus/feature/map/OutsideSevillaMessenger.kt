package com.sloy.sevibus.feature.map

class OutsideSevillaMessenger {

    private val shuffledMessages = OTHER_MESSAGES.shuffled()
    private var clickCount = 0

    fun getNextMessage(): String {
        return if (clickCount == 0) {
            clickCount++
            FIRST_MESSAGE
        } else {
            val messageIndex = (clickCount - 1) % shuffledMessages.size
            clickCount++
            shuffledMessages[messageIndex]
        }
    }

    companion object {
        private const val FIRST_MESSAGE = "Estás fuera de Sevilla"
        private val OTHER_MESSAGES = listOf(
            "¿Tú ande vá? Estás fuera de Sevilla",
            "¿Pero tú estás en Sevilla ni ná?",
            "SeviBus sólo funciona en Sevilla y en Triana",
            "Papá, qué está más cerca, ¿Sevilla o la Luna?",
            "Estás fuera de Sevilla. ¿¿Qué haces??",
            "Anda anda, si estás en el quinto carajo",
            "Vente pa Sevilla que se está más calentito",
            "No, mejor ven tú a Sevilla que es el mismo camino"
        )
    }
}