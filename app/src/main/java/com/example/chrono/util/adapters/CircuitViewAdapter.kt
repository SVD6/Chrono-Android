package com.example.chrono.util.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.chrono.R
import com.example.chrono.util.getDrawableByString
import com.example.chrono.util.objects.CircuitObject
import com.google.android.material.textview.MaterialTextView

class CircuitViewAdapter(private val data: List<CircuitObject>) :
    RecyclerView.Adapter<CircuitViewAdapter.CircuitViewHolder>() {

    class CircuitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: MaterialTextView = view.findViewById(R.id.circuit_name)
        val numSets: MaterialTextView = view.findViewById(R.id.num_sets)
        val timeRest: MaterialTextView = view.findViewById(R.id.rest_time)
        val timeWork: MaterialTextView = view.findViewById(R.id.work_time)
        val icon: ImageView = view.findViewById(R.id.circuit_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircuitViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.circuit_card_lay, parent, false)
        return CircuitViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CircuitViewHolder, position: Int) {
        val circuit = data[position]
        holder.name.text = circuit.name.toString()
        holder.numSets.text = circuit.sets.toString() + " Sets"
        holder.timeRest.text = "Rest:  " + circuit.rest.toString() + "s"
        holder.timeWork.text = "Work:  " + circuit.work.toString() + "s"
        holder.icon.setImageResource(circuit.iconId!!)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}