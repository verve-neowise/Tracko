package com.neowise.tracko.view.sessions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neowise.tracko.R
import com.neowise.tracko.data.model.GpsSession
import com.neowise.tracko.util.Utilities

class SessionListAdapter(private val callback: SessionsCallback) : RecyclerView.Adapter<SessionListAdapter.SessionViewHolder>() {

    private val items = ArrayList<GpsSession>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(
            callback,
            view
        )
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun add(item: GpsSession) {
        this.items.add(item)
        notifyDataSetChanged()
    }

    fun add(vararg items: GpsSession) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun add(items: List<GpsSession>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun get(position: Int): GpsSession = this.items[position]

    override fun getItemCount(): Int {
        return items.size
    }

    class SessionViewHolder(private val callback: SessionsCallback, view: View): RecyclerView.ViewHolder(view) {

        fun bind(model: GpsSession) {

            val nameTextView = itemView.findViewById<TextView>(R.id.name)
            val distanceTextView = itemView.findViewById<TextView>(R.id.distance_txt)
            val timeTextView = itemView.findViewById<TextView>(R.id.time_txt)
            val speedTextView = itemView.findViewById<TextView>(R.id.speed_txt)

            val viewBtn = itemView.findViewById<Button>(R.id.view_btn)
            val renameBtn = itemView.findViewById<Button>(R.id.rename_btn)
            val deleteBtn = itemView.findViewById<Button>(R.id.delete_btn)

            viewBtn.setOnClickListener {
                callback.view(adapterPosition)
            }

            renameBtn.setOnClickListener {
                callback.rename(adapterPosition)
            }

            deleteBtn.setOnClickListener {
                callback.delete(adapterPosition)
            }

            nameTextView.text = "#${adapterPosition + 1}: ${model.name}"
            distanceTextView.text = "${model.distance}m"
            timeTextView.text = Utilities.formatTime(model.duration.toLong())
            speedTextView.text = Utilities.getPace(model.duration.toLong(), model.distance.toFloat())
        }
    }
}