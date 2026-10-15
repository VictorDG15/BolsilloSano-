package org.bolsillosano.project.data.time

import org.bolsillosano.project.domain.time.DateProvider
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemDateProvider @Inject constructor() : DateProvider {
    override fun today(): LocalDate = LocalDate.now()

    override fun hour(): Int = LocalTime.now().hour
}
