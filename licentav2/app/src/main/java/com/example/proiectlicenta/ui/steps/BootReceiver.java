package com.example.proiectlicenta.ui.steps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, StepCounterService.class);
            context.startService(serviceIntent);
            Toast.makeText(context, "Step counter service started after boot", Toast.LENGTH_SHORT).show();
        }
    }
}
