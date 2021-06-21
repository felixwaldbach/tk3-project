package de.darmstadt.tk.service

import de.darmstadt.tk.repo.EventRepo
import de.darmstadt.tk.repo.MemEventRepo

object ServiceLocator {
    private val repo = MemEventRepo()
    fun getRepository(): EventRepo = repo

}