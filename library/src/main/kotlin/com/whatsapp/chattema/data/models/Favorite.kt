package com.whatsapp.chattema.data.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "favorite")
data class Favorite(@SerializedName("url") @NonNull @PrimaryKey @ColumnInfo(name = "url") var url: String = "")