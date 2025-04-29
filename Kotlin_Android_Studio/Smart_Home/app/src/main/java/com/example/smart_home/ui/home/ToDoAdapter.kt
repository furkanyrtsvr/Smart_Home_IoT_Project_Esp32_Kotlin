import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_home.R
import com.example.smart_home.ToDoItem

class ToDoAdapter(
    private val todoList: MutableList<ToDoItem>,
    private val onDelete: (ToDoItem) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item = todoList[position]
        holder.tvTask.text = item.task

        // Silme butonu tıklanınca item'i kaldır
        holder.btnDelete.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount(): Int = todoList.size

    class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTask: TextView = itemView.findViewById(R.id.taskText)
        val btnDelete: Button = itemView.findViewById(R.id.deleteButton)
    }
}
