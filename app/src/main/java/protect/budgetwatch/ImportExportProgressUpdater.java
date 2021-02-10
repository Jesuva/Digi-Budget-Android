package protect.budgetwatch;

import android.app.Activity;
import android.app.ProgressDialog;


class ImportExportProgressUpdater
{
    private static final long UPDATE_INTERVAL = 250;

    private final Activity activity;
    private final ProgressDialog dialog;
    private final String baseMessage;

    private Integer totalEntries;
    private int entriesMoved = 0;
    private long lastUpdateTimeMs = 0;

    ImportExportProgressUpdater(Activity activity, ProgressDialog dialog, String baseMessage)
    {
        this.activity = activity;
        this.dialog = dialog;
        this.baseMessage = baseMessage;
        this.totalEntries = null;
    }

    public void setTotal(int totalEntries)
    {
        this.totalEntries = totalEntries;
    }


    public void update()
    {
        entriesMoved += 1;


        long currentTime = System.currentTimeMillis();
        if( (currentTime - lastUpdateTimeMs) >= UPDATE_INTERVAL ||
                (totalEntries != null && entriesMoved == totalEntries) )
        {
            lastUpdateTimeMs = currentTime;

            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    String formatted;

                    if(totalEntries != null)
                    {
                        formatted = String.format(baseMessage, entriesMoved, totalEntries);
                    }
                    else
                    {
                        formatted = String.format(baseMessage, entriesMoved);
                    }

                    dialog.setMessage(formatted);
                }
            });
        }
    }
}
