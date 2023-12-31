package ru.vadimdorofeev.itschool.finaltask

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment

class ConvertDialog(val cur: Currency) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_convert, null)

        val rate = cur.rate.replace(',', '.').toDoubleOrNull() ?: 1.0

        val template = "%1$.2f"

        val dialog = AlertDialog.Builder(context)
            .setTitle(context?.resources?.getString(R.string.dialog_convert_title))
            .setView(view)
            .setNegativeButton(context?.resources?.getString(R.string.dialog_convert_close)) { _, _ ->
                this.dismiss()
            }.create()

        val textviewFromTitle = view.findViewById<TextView>(R.id.from_title)
        val edittextFrom = view.findViewById<EditText>(R.id.from)
        val edittextTo = view.findViewById<EditText>(R.id.to)

        textviewFromTitle.text = cur.title

        edittextFrom.doAfterTextChanged {
            if (edittextFrom.hasFocus()) {
                val value = it.toString().toDoubleOrNull()
                if (value != null)
                    edittextTo.setText(template.format (value * rate))
            }
        }

        edittextTo.doAfterTextChanged {
            if (edittextTo.hasFocus()) {
                val value = it.toString().toDoubleOrNull()
                if (value != null)
                    edittextFrom.setText(template.format (value / rate))
            }
        }

        return dialog
    }
}