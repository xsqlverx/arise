package com.example.domain

/** UI callbacks cannot skip a question or complete an attempt twice. */
data class QuizAttempt(val id: String, val answerKey: List<Int>, val index: Int = 0,
    val selected: Int? = null, val answers: List<Boolean> = emptyList(),
    val checked: Boolean = false, val complete: Boolean = false) {
    val score: Int get() = answers.count { it }
    fun select(option: Int): QuizAttempt = if (checked || complete || option !in 0..2) this else copy(selected = option)
    fun check(): QuizAttempt = if (checked || complete || selected == null || index !in answerKey.indices) this
        else copy(checked = true, answers = answers + (selected == answerKey[index]))
    fun advance(): QuizAttempt = when {
        !checked || complete || answerKey.isEmpty() -> this
        index == answerKey.lastIndex -> copy(complete = true)
        else -> copy(index = index + 1, selected = null, checked = false)
    }
}
