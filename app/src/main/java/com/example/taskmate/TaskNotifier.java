package com.example.taskmate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskNotifier {

    private static final String TAG = "!!! TaskNotifier";
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String CHANNEL_ID = "REMINDER_CHANNEL";

    private static final long[] NOTIFICATION_INTERVALS = {
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(12),
            TimeUnit.HOURS.toMillis(6),
            TimeUnit.HOURS.toMillis(1)
    };

    /**
     * Memeriksa semua tugas yang belum selesai dan menampilkan notifikasi jika diperlukan.
     * Metode ini dipanggil saat aplikasi berada di foreground.
     */
    public static void checkAndShowNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        TaskDatabase db = new TaskDatabase(context);
        ArrayList<Task> tasks = db.getUncompletedTasks();
        long now = System.currentTimeMillis();

        createNotificationChannel(context);

        for (Task task : tasks) {
            Date deadlineDate = parseDate(task.getDate());
            if (deadlineDate == null) continue;

            long deadlineMillis = deadlineDate.getTime();

            for (long interval : NOTIFICATION_INTERVALS) {
                long triggerTime = deadlineMillis - interval;
                String notificationKey = task.getId() + "_" + interval;

                // Jika waktu pemicu sudah lewat DAN notifikasi ini belum pernah ditampilkan
                if (now >= triggerTime && !prefs.getBoolean(notificationKey, false)) {
                    Log.d(TAG, "Menampilkan notifikasi untuk tugas: " + task.getTitle() + " (Interval: " + TimeUnit.MILLISECONDS.toHours(interval) + " jam)");
                    showNotification(context, task);
                    
                    // Tandai bahwa notifikasi ini sudah ditampilkan
                    prefs.edit().putBoolean(notificationKey, true).apply();
                    
                    // Hanya tampilkan notifikasi terdekat, jangan spam
                    break; 
                }
            }
        }
    }

    private static void showNotification(Context context, Task task) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Date deadlineDate = parseDate(task.getDate());
        if (deadlineDate == null) return;
        long millisLeft = deadlineDate.getTime() - System.currentTimeMillis();

        String timeLeftText = formatTimeLeft(millisLeft);
        String title = "Tugas Mendekat: " + task.getTitle();
        String message = "Tenggat dalam " + timeLeftText;

        Intent intent = new Intent(context, DetailTaskActivity.class);
        intent.putExtra("id", task.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, task.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        nm.notify(task.getId(), builder.build());
    }

    /**
     * Membersihkan status notifikasi yang tersimpan untuk sebuah tugas.
     * Panggil ini saat tugas dihapus atau diselesaikan.
     */
    public static void clearNotificationStateForTask(Context context, int taskId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (long interval : NOTIFICATION_INTERVALS) {
            String notificationKey = taskId + "_" + interval;
            editor.remove(notificationKey);
        }
        editor.apply();
        Log.d(TAG, "Membersihkan status notifikasi untuk tugas ID: " + taskId);
    }
    
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                CharSequence name = "Pengingat Tugas";
                String description = "Notifikasi untuk tugas yang mendekati tenggat.";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                manager.createNotificationChannel(channel);
            }
        }
    }

    private static Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private static String formatTimeLeft(long millisLeft) {
        if (millisLeft < 0) return "kurang dari semenit";
        long days = TimeUnit.MILLISECONDS.toDays(millisLeft);
        long hours = TimeUnit.MILLISECONDS.toHours(millisLeft) % 24;

        if (days > 0) return days + " hari lagi";
        if (hours > 0) return hours + " jam lagi";
        return TimeUnit.MILLISECONDS.toMinutes(millisLeft) + " menit lagi";
    }
}