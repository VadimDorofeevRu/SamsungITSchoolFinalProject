package ru.vadimdorofeev.itschool.finaltask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

class CurrencyAdapter(val items: List<Currency>) : RecyclerView.Adapter<CurrencyAdapter.CurrencyHolder>() {

    private var colorOdd = 0
    private var colorEven = 0

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(f: (Int) -> Unit) { onItemClickListener = f }


    class CurrencyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.findViewById<ConstraintLayout>(R.id.container)
        val title = itemView.findViewById<TextView>(R.id.title)
        val rate = itemView.findViewById<TextView>(R.id.rate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false)
        colorOdd = parent.context.resources.getColor(R.color.white_almost, null)
        colorEven = parent.context.resources.getColor(R.color.white_not_almost, null)

        val holder = CurrencyHolder(view)
        view.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != NO_POSITION)
                onItemClickListener?.invoke(pos)
        }

        return holder
    }

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int) {
        holder.title.text = items[position].title
        holder.rate.text = items[position].rate.toString()
        holder.container.setBackgroundColor(if ((position % 2) == 0) colorEven else colorOdd)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}