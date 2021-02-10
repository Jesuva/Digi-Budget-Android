package protect.budgetwatch;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

class MultiFormatExporter
{
    private static final String TAG = "BudgetWatch";


    public static boolean exportData(Context context, DBHelper db, Long startTimeMs, Long endTimeMs, OutputStream output, DataFormat format, ImportExportProgressUpdater updater)
    {
        DatabaseExporter exporter = null;

        switch(format)
        {
            case CSV:
                exporter = new CsvDatabaseExporter();
                break;
            case JSON:
                exporter = new JsonDatabaseExporter();
                break;
            case ZIP:
                exporter = new ZipDatabaseExporter();
                break;
        }

        if(exporter != null)
        {
            try
            {
                exporter.exportData(context, db, startTimeMs, endTimeMs, output, updater);
                return true;
            }
            catch(IOException | InterruptedException e)
            {
                Log.e(TAG, "Failed to export data", e);
            }

            return false;
        }
        else
        {
            Log.e(TAG, "Unsupported data format exported: " + format.name());
            return false;
        }
    }
}
