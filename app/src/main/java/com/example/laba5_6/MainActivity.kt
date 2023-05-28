package com.example.laba5_6
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.laba5_6.DetailActivity
import com.example.laba5_6.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Type
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var data: MutableList<String>
    private lateinit var originalData: MutableList<String>
    private lateinit var gson: Gson
    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView)

        gson = Gson()
        file = File(filesDir, "list_data.json")

        // Загрузка списка из файла
        data = loadDataFromFile()
        originalData = data.toMutableList()

        adapter = ArrayAdapter(this, R.layout.list_item, R.id.textView, data)
        listView.adapter = adapter

        // Устанавливаем изображение для каждого элемента списка
        val images = intArrayOf(R.drawable.image1, R.drawable.image2, R.drawable.image3)

        listView.setOnItemClickListener { _, view, position, _ ->
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            imageView.setImageResource(images[position % images.size])

            // Остальной код обработки нажатия на элемент списка...
        }

        val addButton: Button = findViewById(R.id.addButton)
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        val clearButton: Button = findViewById(R.id.clearButton)

        addButton.setOnClickListener {
            showCreateDialog()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearButton.setOnClickListener {
            clearSearch()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = data[position]
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("selectedItem", selectedItem)
            startActivity(intent)
        }

        registerForContextMenu(listView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create -> {
                showCreateDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.cotext_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val position = info.position

        val selectedItem = data[position]

        when (item.itemId) {
            R.id.menu_delete -> {
                deleteItem(position)
                return true
            }

            R.id.menu_edit -> {
                showEditDialog(position, selectedItem)
                return true
            }

            R.id.menu_view -> {
                showMessage("Просмотр элемента: $selectedItem")
                return true
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun showCreateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить элемент")

        val editText = EditText(this)
        builder.setView(editText)

        builder.setPositiveButton("Добавить") { _, _ ->
            val newItem = editText.text.toString()
            addItem(newItem)
        }

        builder.setNegativeButton("Отмена", null)

        builder.show()
    }

    private fun showEditDialog(position: Int, item: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Редактировать элемент")

        val editText = EditText(this)
        editText.setText(item)
        builder.setView(editText)

        builder.setPositiveButton("Сохранить") { _, _ ->
            val updatedItem = editText.text.toString()
            updateItem(position, updatedItem)
        }

        builder.setNegativeButton("Отмена", null)

        builder.show()
    }

    private fun addItem(item: String) {
        data.add(item)
        originalData.add(item) // Добавление элемента в originalData
        adapter.notifyDataSetChanged()

        // Сохранение списка в файл
        saveDataToFile()
    }

    private fun updateItem(position: Int, updatedItem: String) {
        data[position] = updatedItem
        originalData[position] = updatedItem // Обновление элемента в originalData
        adapter.notifyDataSetChanged()

        // Сохранение списка в файл
        saveDataToFile()
    }

    private fun deleteItem(position: Int) {
        data.removeAt(position)
        adapter.notifyDataSetChanged()

        // Сохранение списка в файл
        saveDataToFile()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sortItems() {
        data.sort()
        adapter.notifyDataSetChanged()
    }

    private fun filterItems(query: String) {
        data.clear()
        if (query.isEmpty()) {
            data.addAll(originalData)
        } else {
            data.addAll(originalData.filter { it.contains(query, ignoreCase = true) })
        }
        adapter.notifyDataSetChanged()
    }

    private fun clearSearch() {
        data.clear()
        data.addAll(originalData)
        adapter.notifyDataSetChanged()
    }

    // Функция для сохранения списка в файл
    private fun saveDataToFile() {
        val jsonString = gson.toJson(data)
        file.writeText(jsonString, Charset.defaultCharset())
    }

    // Функция для загрузки списка из файла
    private fun loadDataFromFile(): MutableList<String> {
        return try {
            val jsonString = file.readText(Charset.defaultCharset())
            val type: Type = object : TypeToken<MutableList<String>>() {}.type
            gson.fromJson(jsonString, type) ?: mutableListOf()
        } catch (ex: FileNotFoundException) {
            mutableListOf() // Возвращаем пустой список, если файл не найден
        } catch (ex: Exception) {
            ex.printStackTrace()
            mutableListOf() // Возвращаем пустой список в случае других ошибок
        }
    }

}
