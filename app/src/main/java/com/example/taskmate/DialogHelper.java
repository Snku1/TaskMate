package com.example.taskmate;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;

public class DialogHelper {

    /**
     * Menampilkan dialog popup yang berisi daftar tugas yang belum selesai.
     * @param context Konteks dari activity yang memanggil.
     * @param urgentTasks Daftar tugas yang akan ditampilkan.
     */
    public static void showUncompletedTasksDialog(Context context, ArrayList<Task> urgentTasks) {
        if (urgentTasks == null || urgentTasks.isEmpty()) {
            return; // Jangan tampilkan dialog jika tidak ada tugas
        }

        StringBuilder message = new StringBuilder();
        message.append("Anda memiliki ").append(urgentTasks.size()).append(" tugas yang belum selesai:\n\n");

        for (Task task : urgentTasks) {
            // Tambahkan setiap judul tugas sebagai item daftar
            message.append("• ").append(task.getTitle()).append("\n");
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Tugas Mendekati Tenggat")
                .setMessage(message.toString())
                .setPositiveButton("Mengerti", (dialog, which) -> dialog.dismiss())
                .show();
    }
}