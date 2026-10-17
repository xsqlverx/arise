package com.example.domain

data class QuizAttempt(
    val id: String,
    val answerKey: List<Int>,
    val index: Int = 0,
    val selected: Int? = null,
    val answers: List<Boolean> = emptyList(),
    val checked: Boolean = false,
    val complete: Boolean = false
) {
    val score: Int get() = answers.count { it }

    fun select(option: Int): QuizAttempt {
        if (checked || complete || option !in 0..2) return this
        return copy(selected = option)
    }

    fun check(): QuizAttempt {
        if (checked || complete || selected == null) return this
        val isCorrect = selected == answerKey.getOrNull(index)
        return copy(checked = true, answers = answers + isCorrect)
    }

    fun advance(): QuizAttempt {
        if (!checked || complete) return this
        if (index + 1 >= answerKey.size) {
            return copy(complete = true)
        }
        return copy(index = index + 1, selected = null, checked = false)
    }
}
