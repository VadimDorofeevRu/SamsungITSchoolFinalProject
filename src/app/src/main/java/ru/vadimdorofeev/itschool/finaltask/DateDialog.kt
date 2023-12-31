package ru.vadimdorofeev.itschool.finaltask

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DateDialog(val year: Int, val month: Int, val day: Int) : DialogFragment() {

    interface DataListener {
        fun onDialogData(year: Int, month: Int, day: Int)
    }

    // Слушатель интерфейса
    lateinit var listener: DataListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(requireContext(), {
                _, year, month, day -> listener.onDialogData(year, month + 1, day)
        }, year, month - 1, day)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as DataListener
    }
}