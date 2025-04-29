package com.example.compraapp.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "productos")
data class Producto (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    val nombre: String,
    val supermercado: String,
    val precio: Double,
    val fotoUri: String? = null

// LO HACEMOS PARCELABLE PARA PODERLO PASAR COMO OBJETO A TRAVES DE LOS INTENTS
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(nombre)
        parcel.writeString(supermercado)
        parcel.writeDouble(precio)
        parcel.writeString(fotoUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Producto> {
        override fun createFromParcel(parcel: Parcel): Producto {
            return Producto(parcel)
        }

        override fun newArray(size: Int): Array<Producto?> {
            return arrayOfNulls(size)
        }
    }
}