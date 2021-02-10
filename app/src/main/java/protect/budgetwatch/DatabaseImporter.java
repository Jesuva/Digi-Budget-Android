package protect.budgetwatch;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;


interface DatabaseImporter
{
    /**
     * Import data from the input stream in a given format into
     * the database.
     * @throws IOException
     * @throws FormatException
     */
    void importData(Context context, DBHelper db, InputStream input, ImportExportProgressUpdater updater) throws IOException, FormatException, InterruptedException;
}
