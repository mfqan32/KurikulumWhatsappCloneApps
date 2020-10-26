package com.mfqan.creating.kurikulumwhatsappclone.util

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mfqan.creating.kurikulumwhatsappclone.R
import java.text.DateFormat
import java.util.*

fun populateImage(
    context: Context?,
    uri: String?,
    imageView: ImageView, errorDrawable: Int = R.drawable.empty
//  fungsi populateImage digunakan untuk memudahkan proses
//  pemasangan gambar ke dalam ImageView dengan menggunakan // Library pihak ketiga yaitu Glide
) {
    if (context != null) {
        val options =
            RequestOptions().placeholder(progressDrawable(context)).error(errorDrawable)
        Glide.with(context).load(uri).apply(options).into(imageView)
    }
}

//  menambahkan progressDrawable ketika Image dalam proses pemasangan
fun progressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f // ketebalan garis lingkaran
        centerRadius = 30f // diameter lingkaran
        start() // memulai progressDrawable
    }
}

fun getTime(): String {                         // untuk format tanggal yang digunakan dalam data statusTime
    val dateFormat = DateFormat.getDateInstance()
    return dateFormat.format(Date())
}
