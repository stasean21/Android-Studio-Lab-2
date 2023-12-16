package com.example.myapplicationlab2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var showNamesButton: Button
    private lateinit var sortNamesButton: Button
    private lateinit var saveSortedNamesButton: Button

    private var originalNames: MutableList<String> = mutableListOf()

    private val CREATE_FILE_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение ссылок на кнопки
        showNamesButton = findViewById(R.id.showNamesButton)
        sortNamesButton = findViewById(R.id.sortNamesButton)
        saveSortedNamesButton = findViewById(R.id.saveSortedNamesButton)

        // Загрузка имен при запуске приложения
        loadNames()

        // Обработка нажатия на кнопку "Показать имена"
        showNamesButton.setOnClickListener {
            showNamesDialog(originalNames)
        }

        // Обработка нажатия на кнопку "Сортировать имена"
        sortNamesButton.setOnClickListener {
            val sortedNames = originalNames.sorted().toMutableList()
            showNamesDialog(sortedNames)
        }

        // Обработка нажатия на кнопку "Сохранить отсортированные имена"
        saveSortedNamesButton.setOnClickListener {
            val sortedNames = originalNames.sorted()
            openDocumentPicker(sortedNames)
        }
    }

    private fun loadNames() {
        // Очистка списка перед загрузкой
        originalNames.clear()

        // Чтение данных из текстового файла в папке assets
        assets.open("names.txt").bufferedReader().useLines { lines ->
            lines.forEach {
                originalNames.add(it)
            }
        }
    }

    private fun showNamesDialog(names: List<String>) {
        // Формирование строки с именами
        val namesString = names.joinToString("\n")

        // Создание AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Список имен")
        builder.setMessage(namesString)
        builder.setPositiveButton("OK", null)

        // Отображение диалога
        val dialog = builder.create()
        dialog.show()
    }

    private fun openDocumentPicker(sortedNames: List<String>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "sorted_names.txt")
        }

        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                saveSortedNames(uri, originalNames.sorted())
            }
        }
    }

    private fun saveSortedNames(uri: Uri, sortedNames: List<String>) {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                for (name in sortedNames) {
                    writer.write("$name\n")
                }
            }
            showAlert("Сохранено в $uri", "Отсортированные имена успешно сохранены.")
        } ?: showAlert("Ошибка сохранения", "Произошла ошибка при сохранении отсортированных имен.")
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }
}
