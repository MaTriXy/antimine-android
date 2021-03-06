package dev.lucasnlm.antimine.stats.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.stats.model.StatsModel

class StatsViewModel @ViewModelInject constructor(
    private val statsRepository: IStatsRepository,
    private val preferenceRepository: IPreferencesRepository
) : ViewModel() {
    val statsObserver = MutableLiveData<StatsModel>()

    suspend fun getStatsModel(): StatsModel? {
        val minId = preferenceRepository.getStatsBase()
        val stats = statsRepository.getAllStats(minId)
        val statsCount = stats.count()

        return if (statsCount > 0) {
            val result = stats.fold(
                StatsModel(statsCount, 0L, 0L, 0, 0, 0)
            ) { acc, value ->
                StatsModel(
                    acc.totalGames,
                    acc.duration + value.duration,
                    0,
                    acc.mines + value.mines,
                    acc.victory + value.victory,
                    acc.openArea + value.openArea
                )
            }
            result.copy(averageDuration = result.duration / result.totalGames)
        } else {
            emptyStats
        }
    }

    suspend fun deleteAll() {
        statsRepository.getAllStats(0).lastOrNull()?.let {
            preferenceRepository.updateStatsBase(it.uid + 1)
        }

        statsObserver.postValue(emptyStats)
    }

    suspend fun loadStats() {
        getStatsModel()?.let {
            if (it.totalGames > 0) {
                statsObserver.postValue(it)
            }
        }
    }

    companion object {
        val emptyStats = StatsModel(0, 0, 0, 0, 0, 0)
    }
}
