package de.darmstadt.tk.service

import de.darmstadt.tk.repo.EventRepo
import de.darmstadt.tk.repo.MemEventRepo

object ServiceLocator {
    private val repo = MemEventRepo()
    private val ulb = UlbService()
    fun getRepository(): EventRepo = repo

    fun getUlbService(): UlbService = ulb

}