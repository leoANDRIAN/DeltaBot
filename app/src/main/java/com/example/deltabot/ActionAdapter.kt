package com.example.deltabot

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

// Mécaniques de suppression des tableaux d'éléments UI
class ActionAdapter(private val context: Context, private val tabActions: ArrayList<UserAction>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return tabActions.size
    }

    override fun getItem(position: Int): Any {
        return tabActions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.action, parent, false)
        val textAction = rowView.findViewById(R.id.textAction) as TextView
        val btnDeleteAction = rowView.findViewById(R.id.btnDeleteAction) as Button
        btnDeleteAction.setOnClickListener {
            tabActions.removeAt(position)
            notifyDataSetChanged();
        }
        val action = getItem(position) as UserAction
        textAction.text = action.text
        return rowView
    }
}