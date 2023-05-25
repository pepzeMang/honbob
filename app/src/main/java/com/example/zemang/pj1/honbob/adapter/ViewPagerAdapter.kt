package com.example.zemang.pj1.honbob.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zemang.pj1.honbob.R
import com.example.zemang.pj1.honbob.retrofit.RestaurantModel

class ViewPagerAdapter(val itemClicked: (RestaurantModel) -> Unit) :
    ListAdapter<RestaurantModel, ViewPagerAdapter.ItemViewHolder>(differ) {
    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(restaurantModel: RestaurantModel) {
            val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
            val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)

            titleTextView.text = restaurantModel.title
            priceTextView.text = restaurantModel.price

            view.setOnClickListener {
                itemClicked(restaurantModel)
            }

            Glide.with(thumbnailImageView.context)
                .load(restaurantModel.imgUrl)
                .into(thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(
            inflater.inflate(
                R.layout.item_restaurant_detail_for_viewpager,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<RestaurantModel>() {
            override fun areItemsTheSame(oldItem: RestaurantModel, newItem: RestaurantModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RestaurantModel, newItem: RestaurantModel): Boolean {
                return oldItem == newItem
            }

        }
    }

}