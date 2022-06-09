package com.tms.mysongs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tms.mysongs.R;
import com.tms.mysongs.models.Singer;
import com.tms.mysongs.models.Song;
import com.tms.mysongs.services.MyService;

import java.util.ArrayList;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(context.getString(R.string.action), 0);

        Intent intentService = new Intent(context, MyService.class);
        intentService.putExtra(context.getString(R.string.action), action);

        context.startService(intentService);
    }
}
