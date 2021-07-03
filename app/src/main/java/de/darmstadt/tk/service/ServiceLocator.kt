package de.darmstadt.tk.service

import de.darmstadt.tk.repo.EventRepo
import de.darmstadt.tk.repo.MemEventRepo

object ServiceLocator {
    private val repo by lazy { MemEventRepo() }
    private val ulb by lazy { UlbService() }
    fun getRepository(): EventRepo = repo

    fun getUlbService(): UlbService = ulb

}