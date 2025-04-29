package com.example.smart_home.ui.home

import ToDoAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_home.R
import com.example.smart_home.ToDoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ToDoListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etTodoInput: EditText
    private lateinit var btnAddTodo: Button
    private val todoList = mutableListOf<ToDoItem>()
    private lateinit var adapter: ToDoAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var nextId = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_do_list, container, false)

        recyclerView = view.findViewById(R.id.rv_todo_list)
        etTodoInput = view.findViewById(R.id.et_todo_input)
        btnAddTodo = view.findViewById(R.id.btn_add_todo)

        sharedPreferences = requireContext().getSharedPreferences("ToDoPrefs", Context.MODE_PRIVATE)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ToDoAdapter(todoList) { item -> deleteTodo(item) }
        recyclerView.adapter = adapter

        loadData() // Kayıtlı verileri yükle

        btnAddTodo.setOnClickListener {
            addTodo()
        }

        return view
    }

    private fun addTodo() {
        val task = etTodoInput.text.toString().trim()
        if (task.isNotEmpty()) {
            val newTodo = ToDoItem(nextId++, task)
            todoList.add(newTodo)
            adapter.notifyItemInserted(todoList.size - 1)
            etTodoInput.text.clear()
            saveData() // Yeni veri eklenince kaydet
        }
    }

    private fun deleteTodo(item: ToDoItem) {
        val position = todoList.indexOf(item)
        if (position != -1) {
            todoList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, todoList.size)
            saveData() // Silme işleminden sonra kaydet
        }
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(todoList)
        editor.putString("todo_list", json)
        editor.apply()
    }

    private fun loadData() {
        val gson = Gson()
        val json = sharedPreferences.getString("todo_list", null)
        val type = object : TypeToken<MutableList<ToDoItem>>() {}.type
        val savedList: MutableList<ToDoItem>? = gson.fromJson(json, type)

        if (savedList != null) {
            todoList.addAll(savedList)
            nextId = if (todoList.isEmpty()) 1 else (todoList.maxOf { it.id } + 1)
            adapter.notifyDataSetChanged()
        }
    }
}
