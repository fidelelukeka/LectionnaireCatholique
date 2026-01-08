package com.lukekadigitalservices.lectionnairecatholique.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MesseEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // On connecte nos convertisseurs JSON
abstract class AppDatabase : RoomDatabase() {

    abstract fun messeDao(): MesseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si l'instance existe, on la retourne, sinon on crée la base
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lectionnaire_database" // Nom du fichier sur le téléphone
                )
                    .fallbackToDestructiveMigration(false) // Utile en développement si vous changez l'entité
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}