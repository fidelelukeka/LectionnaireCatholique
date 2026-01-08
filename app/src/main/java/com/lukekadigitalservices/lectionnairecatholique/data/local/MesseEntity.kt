package com.lukekadigitalservices.lectionnairecatholique.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lukekadigitalservices.lectionnairecatholique.data.MessesResponse

@Entity(tableName = "messes_cache")
data class MesseEntity(
    @PrimaryKey val id: String, // format: "yyyy-MM-dd_zone"
    val date: String,
    val zone: String,
    val data: MessesResponse // Sera converti en String par Room
)