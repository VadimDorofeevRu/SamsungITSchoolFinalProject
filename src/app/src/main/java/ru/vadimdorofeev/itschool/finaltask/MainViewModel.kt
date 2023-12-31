package ru.vadimdorofeev.itschool.finaltask

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL
import java.nio.charset.Charset

class MainViewModel : ViewModel() {

    var db: SQLiteDatabase? = null

    var date: LocalDate

    // Предельные даты, для которых есть курсы
    private var minDate = LocalDate(1992, 7, 1)
    private var maxDate: LocalDate

    // Проверка валидности даты: должна быть от 01.07.1992 до текущего дня
    private fun isValidDate(newDate: LocalDate): Boolean {
        return newDate in minDate..maxDate
    }

    // Переход к предыдущему дню
    fun prevDay(): Boolean {
        val newDate = date.minus(1, DateTimeUnit.DAY)
        if (isValidDate(newDate)) {
            date = newDate
            return true
        }
        return false
    }

    // Переход к следующему дню
    fun nextDay(): Boolean {
        val newDate = date.plus(1, DateTimeUnit.DAY)
        if (isValidDate(newDate)) {
            date = newDate
            return true
        }
        return false
    }

    // Установка даты
    fun setDate(year: Int, month: Int, day: Int): Boolean {
        val newDate = LocalDate(year, month, day)
        if (isValidDate(newDate)) {
            date = newDate
            return true
        }
        return false
    }

    // Перевод даты в SQL-формат
    private fun dateToSQL(): String {
        return date.year.toString() + "-" +
               date.monthNumber.toString().padStart(2, '0') + "-" +
               date.dayOfMonth.toString().padStart(2, '0')
    }

    // Загрузка курсов валют из БД
    private fun getCurrenciesFromDb(): List<Currency> {
        val result = mutableListOf<Currency>()
        try {
            val cursor = db?.rawQuery("SELECT code, name, rate FROM rates WHERE date=?",
                arrayOf(dateToSQL()))
            cursor?.moveToFirst()
            while (cursor?.isAfterLast == false) {
                val c = Currency(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
                result.add(c)
                cursor.moveToNext()
            }
            cursor?.close()
        }
        catch (_: Exception) {}
        return result
    }

    // Курсы некоторых валют настолько маленькие, что ЦБ приводит их
    // за десяток или даже больше единиц, приводя и наименование валюты
    // в родительном падеже. Для отображения в списке это не подходит,
    // поэтому такие названия заменяются в полуавтоматическом режиме
    private var replaces = mapOf(
        Pair("Армянских драмов", "Армянский драм"),
        Pair("Венгерских форинтов", "Венгерский форинт"),
        Pair("Вьетнамских донгов", "Вьетнамский донг"),
        Pair("Индонезийских рупий", "Индонезийская рупия"),
        Pair("Казахстанских тенге", "Казахстанское тенге"),
        Pair("Узбекских сумов", "Узбекский сум"),
        Pair("Сербских динаров", "Сербский динар"),
        Pair("Вон Республики Корея", "Южнокорейский вон"),
        Pair("Японских иен", "Японская иена"),
        Pair("Египетских фунтов", "Египетский фунт"),
        Pair("Индийских рупий", "Индийская рупия"),
        Pair("Киргизских сомов", "Киргизский сом"),
        Pair("Молдавских леев", "Молдавский лей"),
        Pair("Норвежских крон", "Норвежская крона"),
        Pair("Таджикских сомони", "Таджикский сомони"),
        Pair("Таиландских батов", "Таиландский бат"),
        Pair("Турецких лир", "Турецкая лира"),
        Pair("Украинских гривен", "Украинская гривна"),
        Pair("Чешских крон", "Чешская крона"),
        Pair("Шведских крон", "Шведская крона"),
        Pair("Южноафриканских рэндов", "Южноафриканский рэнд")
    )

    // Загрузка курсов валют с сайта ЦБ
    private fun getCurrenciesFromSite(): List<Currency> {
        val result = mutableListOf<Currency>()
        try {
            // Скачивание файла
            val url = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" +
                    date.dayOfMonth.toString().padStart(2, '0') + "/" +
                    date.monthNumber.toString().padStart(2, '0') + "/" +
                    date.year.toString()
            val xml = URL(url)
                .readText(Charset.forName("Windows-1251"))
            // Парсинг XML-файла
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(xml))
            var nominal = ""
            var value = ""
            var name = ""
            var code = ""
            var tag = ""
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG)
                    tag = parser.name
                else if (parser.eventType == XmlPullParser.TEXT)
                    when (tag) {
                        "Nominal" -> nominal = parser.text
                        "VunitRate" -> value = parser.text
                        "Name" -> {
                            name = parser.text
                            if (replaces.containsKey(name))
                                name = replaces.getOrDefault(name, "")
                        }
                        "CharCode" -> code = parser.text
                    }
                else if (parser.eventType == XmlPullParser.END_TAG && parser.name == "Valute") {
                    result.add(Currency(code, name, value))
                    db?.execSQL("INSERT INTO rates (date, code, name, rate) VALUES (?, ?, ?, ?)",
                        arrayOf(dateToSQL(), code, name, value))
                }
                parser.next()
            }
        }
        catch (_: Exception) {}
        return result
    }

    // Получение курсов валют для установленной даты
    fun getCurrencies(): List<Currency> {
        var result = getCurrenciesFromDb()
        if (result.isEmpty())
            result = getCurrenciesFromSite()
        return result
    }

    init {
        val now: Instant = Clock.System.now()
        date = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        maxDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
}