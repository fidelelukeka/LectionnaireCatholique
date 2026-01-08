package com.lukekadigitalservices.lectionnairecatholique.data

import com.lukekadigitalservices.lectionnairecatholique.data.local.MesseDao
import com.lukekadigitalservices.lectionnairecatholique.data.local.MesseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class LiturgieRepository(
    private val api: AelfApi,
    private val dao: MesseDao
) {
    fun getMesses(date: String, zone: String): Flow<ApiResult<MessesResponse>> = flow {
        emit(ApiResult.Loading)

        // 1. Essayer de lire depuis Room
        val localData = dao.getMesse(date, zone)

        if (localData != null) {
            emit(ApiResult.Success(localData.data))
        }

        // 2. Toujours essayer de mettre à jour depuis l'API (ou seulement si local est nul)
        try {
            val response = api.getMesses(date, zone)
            if (response.isSuccessful && response.body() != null) {
                val remoteData = response.body()!!
                // Sauvegarder dans Room pour la prochaine fois
                dao.insertMesse(MesseEntity("${date}_$zone", date, zone, remoteData))
                emit(ApiResult.Success(remoteData))
            } else if (localData == null) {
                emit(ApiResult.Error("Erreur serveur et aucune donnée locale."))
            }
        } catch (e: Exception) {
            // Si erreur réseau et qu'on a déjà affiché le cache, on ne fait rien
            // Sinon on affiche l'erreur
            if (localData == null) {
                emit(ApiResult.Error("Pas de connexion internet."))
            }
        }
    }

    suspend fun prefetchRange(startDate: LocalDate, endDate: LocalDate, zone: String) {
        var current = startDate
        var count = 0
        val batchSize = 10 // On traite par paquets de 10

        while (!current.isAfter(endDate)) {
            val dateStr = current.toString()

            // Vérification locale pour ne pas gaspiller de requêtes
            if (dao.getMesse(dateStr, zone) == null) {
                try {
                    val response = api.getMesses(dateStr, zone)
                    if (response.isSuccessful && response.body() != null) {
                        dao.insertMesse(MesseEntity("${dateStr}_$zone", dateStr, zone, response.body()!!))
                    }
                } catch (e: Exception) {
                    // Log l'erreur mais ne bloque pas la suite
                }

                count++

                // Après chaque batch de 10 appels, on marque une pause
                if (count % batchSize == 0) {
                    kotlinx.coroutines.delay(1000) // Délai de 1 seconde pour laisser respirer l'API
                }
            }
            current = current.plusDays(1)
        }
    }
}