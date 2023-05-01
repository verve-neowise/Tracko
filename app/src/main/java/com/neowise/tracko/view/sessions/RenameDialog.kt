package com.neowise.tracko.view.sessions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.neowise.tracko.R

class RenameDialog(private val position: Int, private val oldName: String) : AppCompatDialogFragment() {

    private lateinit var renameEditText: EditText
    private lateinit var renameDialogListener: RenameDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_rename, null)

        val builder = AlertDialog.Builder(requireActivity())
                .setView(view)
                .setTitle(R.string.rename)
                .setNegativeButton(R.string.cancel) { _, _ ->
                }
                .setPositiveButton(R.string.rename) { _, _ ->
                    val name = renameEditText.text.toString()
                    renameDialogListener.onRename(position, name)
                }

        renameEditText = view.findViewById(R.id.name_edit)
        renameEditText.setText(oldName)
        renameEditText.selectAll()

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        renameDialogListener = context as RenameDialogListener
    }

    interface RenameDialogListener {
        fun onRename(position: Int, newName: String)
    }
}