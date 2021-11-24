package com.whatsapp.chattema.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whatsapp.chattema.data.models.Favorite

@Dao // Data Access Object
interface FavoritesDao {
    @Query("select * from favorite")
    fun getAllFavorites(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // insert into favorites values (name, author, url, ...)
    fun insert(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(favorites: List<Favorite>)

    @Delete
    fun delete(favorite: Favorite)

    @Query("delete from favorite where url = :url")
    fun deleteByUrl(url: String)

    @Query("delete from favorite")
    fun nuke()
}