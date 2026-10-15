package org.bolsillosano.project.domain.time

import java.time.LocalDate

interface DateProvider {
    fun today(): LocalDate

    fun hour(): Int
}
