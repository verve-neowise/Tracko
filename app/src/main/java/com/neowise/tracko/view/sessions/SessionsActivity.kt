package com.neowise.tracko.view.sessions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.neowise.tracko.R
import com.neowise.tracko.data.Database
import com.neowise.tracko.view.sessions.viewer.ViewSessionActivity
import kotlinx.android.synthetic.main.activity_sessions.*

class SessionsActivity : AppCompatActivity(),
    SessionsCallback, RenameDialog.RenameDialogListener {

    private lateinit var adapter : SessionListAdapter

    private val database = Database.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        adapter = SessionListAdapter(this)

        sessions_list.layoutManager = LinearLayoutManager(this)
        sessions_list.adapter = adapter

        back_btn.setOnClickListener {
            finish()
        }

        loadSessions()
    }

    private fun loadSessions() {
        try {
            val list = database.getSessions()
            adapter.add(list)
        }
        catch(e: Exception) {
            Log.d("SESSIONS", "enable to load session -> $e")
        }
    }

    override fun view(position: Int) {
        val session = adapter.get(position)
        val intent = Intent(this, ViewSessionActivity::class.java)
        intent.putExtra("session_id", session.id)
        startActivity(intent)
    }

    override fun rename(position: Int) {
        val oldName = adapter.get(position).name
        RenameDialog(position, oldName)
            .show(supportFragmentManager, "RenameDialog")
    }

    override fun delete(position: Int) {
        AlertDialog.Builder(this)
                .setTitle("Delete session")
                .setMessage("Do you want delete a session?")
                .setPositiveButton("Delete") { _, _ ->
                    val item = adapter.get(position)
                    adapter.removeItem(position)
                    database.removeSession(item.id)
                    database.removeLocations(item.id)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    override fun onRename(position: Int, newName: String) {
        val session = adapter.get(position)

        session.name = newName
        database.renameSession(session.id, newName)

        adapter.notifyItemChanged(position)
    }
}