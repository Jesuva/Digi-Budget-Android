package protect.budgetwatch;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipDatabaseImporter implements DatabaseImporter
{
    private static final String TAG = "BudgetWatch";

    public void importData(Context context, DBHelper db, InputStream input, ImportExportProgressUpdater updater) throws IOException, FormatException, InterruptedException
    {
        File receiptDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        byte [] buffer = new byte[1024];

        ZipInputStream zipStream = new ZipInputStream(input);
        ZipEntry entry;
        while( (entry = zipStream.getNextEntry()) != null)
        {
            if(entry.getName().equals("database.csv"))
            {
                boolean result = MultiFormatImporter.importData(context, db, zipStream, DataFormat.CSV, updater);
                if(result == false)
                {
                    Log.e(TAG, "Failed to import database.csv");
                }
            }
            else
            {
                if(receiptDir != null)
                {
                    File receipt = new File(receiptDir, entry.getName());
                    FileOutputStream out = new FileOutputStream(receipt);

                    try
                    {
                        int count;
                        while ((count = zipStream.read(buffer, 0, buffer.length)) > 0)
                        {
                            out.write(buffer, 0, count);
                        }
                    }
                    finally
                    {
                        out.close();
                    }
                }
            }
        }
    }
}
