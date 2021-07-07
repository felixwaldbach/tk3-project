package de.darmstadt.tk.service

import de.darmstadt.tk.repo.EventRepo
import de.darmstadt.tk.repo.MemEventRepo

object ServiceLocator {
    private val repo by lazy { MemEventRepo() }
    private val ulb by lazy { UlbService() }
    private val rewe by lazy { ReweService() }
    private val herrngarten by lazy { HerrngartenService() }

    fun getRepository(): EventRepo = repo

    fun getUlbService(): UlbService = ulb

    fun getReweService(): ReweService = rewe

    fun getHerrngartenService(): HerrngartenService = herrngarten

}