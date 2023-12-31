package ru.vadimdorofeev.itschool.finaltask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), DateDialog.DataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение (или создание) ViewModel (можно было бы обойтись синглтоном)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        if (viewModel.db == null)
            viewModel.db = SQLiteHelper(this).writableDatabase

        // Обновление данных создании активности
        update()
    }

    private lateinit var viewModel: MainViewModel
    private val buttonPrevDay: ImageButton by lazy { findViewById(R.id.prev_day) }
    private val buttonNextDay: ImageButton by lazy { findViewById(R.id.next_day) }
    private val textviewCurrentDate: TextView by lazy { findViewById(R.id.currency_date) }
    private val list: RecyclerView by lazy { findViewById(R.id.list) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progress_bar) }

    // Обновление элементов управления - дата и курсы валют
    private fun update() {
        textviewCurrentDate.text = resources.getString(R.string.date_format_human)
            .format(viewModel.date.dayOfMonth, viewModel.date.monthNumber, viewModel.date.year)
        progressBar.visibility = View.VISIBLE
        thread {
            val curs = viewModel.getCurrencies()
            runOnUiThread {
                if (curs.isNotEmpty()) {
                    val adapter = CurrencyAdapter(curs)
                    adapter.setOnItemClickListener {
                        val dialog = ConvertDialog(curs[it])
                        dialog.show(supportFragmentManager, "my_dialog")
                    }
                    list.adapter = adapter
                }
                else {
                    list.adapter = null
                    Toast.makeText(this, resources.getString(R.string.load_error), Toast.LENGTH_LONG).show()
                }
                progressBar.visibility = View.GONE
            }
        }
    }

    // Нажатие на кнопку со стрелкой влево - предыдущий день
    fun buttonPrevDayClick(view: View) {
        if (viewModel.prevDay())
            update()
    }

    // Нажатие на кнопку со стрелкой вправо - следующий день
    fun buttonNextDayClick(view: View) {
        if (viewModel.nextDay())
            update()
    }

    // Нажатие на заголовок - отображение диалога выбора даты
    fun headerClick(view: View) {
        val dlg = DateDialog(viewModel.date.year, viewModel.date.monthNumber, viewModel.date.dayOfMonth)
        dlg.show(supportFragmentManager, "my_dialog")
    }

    // Положительное завершение диалога выбора даты - обновление данных
    override fun onDialogData(year: Int, month: Int, day: Int) {
        if (viewModel.setDate(year, month, day))
            update()
    }
}