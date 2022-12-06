package CapStoneDisign.som

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ViewPagerAdapter(private var context: Context, photoList:ArrayList<Uri>):RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
    var item = photoList

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)){
            val photo: ImageView = itemView.findViewById(R.id.photoViewPagerImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(parent)
    }

    override fun getItemCount(): Int = item.size

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        item[position].let{
            with(holder){
                Glide.with(context)
                    .load(it)
                    .fitCenter()
                    .into(photo)
            }
        }
    }
}