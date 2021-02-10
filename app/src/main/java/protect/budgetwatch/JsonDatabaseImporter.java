package protect.budgetwatch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonToken;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class JsonDatabaseImporter implements DatabaseImporter
{
    public void importData(Context context, DBHelper db, InputStream input, ImportExportProgressUpdater updater) throws IOException, FormatException, InterruptedException
    {
        InputStreamReader reader = new InputStreamReader(input, Charsets.UTF_8);
        JsonReader parser = new JsonReader(reader);

        SQLiteDatabase database = db.getWritableDatabase();
        database.beginTransaction();

        try
        {
            parser.beginArray();

            while(parser.hasNext())
            {
                parser.beginObject();

                Integer id = null;
                String name = null;
                String type = null;
                String description = null;
                String account = null;
                String budget = null;
                Double value = null;
                String note = null;
                Long dateMs = null;
                String receiptFilename = null;

                while(parser.hasNext())
                {
                    String itemName = parser.nextName();
                    switch (itemName)
                    {
                        case DBHelper.TransactionDbIds.NAME:
                            name = parser.nextString();
                            break;
                        case "ID":
                            id = parser.nextInt();
                            break;
                        case DBHelper.TransactionDbIds.TYPE:
                            type = parser.nextString();
                            break;
                        case DBHelper.TransactionDbIds.DESCRIPTION:
                            description = parser.nextString();
                            break;
                        case DBHelper.TransactionDbIds.ACCOUNT:
                            account = parser.nextString();
                            break;
                        case DBHelper.TransactionDbIds.BUDGET:
                            budget = parser.nextString();
                            break;
                        case DBHelper.TransactionDbIds.VALUE:
                            value = parser.nextDouble();
                            break;
                        case DBHelper.TransactionDbIds.NOTE:
                            note = parser.nextString();
                            break;
                        case DBHelper.TransactionDbIds.DATE:
                            dateMs = parser.nextLong();
                            break;
                        case DBHelper.TransactionDbIds.RECEIPT:
                            receiptFilename = parser.nextString();
                            break;
                        default:
                            throw new FormatException("Issue parsing JSON data, unknown field: " + itemName);
                    }
                }

                if(type == null)
                {
                    throw new FormatException("Issue parsing JSON data, missing type");
                }

                switch (type)
                {
                    case "BUDGET":
                        importBudget(database, db, name, value);
                        break;
                    case "EXPENSE":
                    case "REVENUE":
                        importTransaction(context, database, db, id, type, description, account, budget, value, note, dateMs, receiptFilename);
                        break;
                    default:
                        throw new FormatException("Issue parsing JSON data, unexpected type: " + type);
                }

                parser.endObject();

                updater.update();

                if(Thread.currentThread().isInterrupted())
                {
                    throw new InterruptedException();
                }
            }

            parser.endArray();

            if(parser.peek() != JsonToken.END_DOCUMENT)
            {
                throw new FormatException("Issue parsing JSON data, no more data expected but found some");
            }

            parser.close();


            database.setTransactionSuccessful();
        }
        catch(IllegalArgumentException e)
        {
            throw new FormatException("Issue parsing JSON data", e);
        }
        finally
        {
            database.endTransaction();
            database.close();
        }
    }


    private void importTransaction(Context context, SQLiteDatabase database, DBHelper helper,
                                   Integer id, String typeStr, String description, String account,
                                   String budget, Double value, String note, Long dateMs,
                                   String receiptFilename)
            throws FormatException
    {
        int type;
        switch(typeStr)
        {
            case "EXPENSE":
                type = DBHelper.TransactionDbIds.EXPENSE;
                break;
            case "REVENUE":
                type = DBHelper.TransactionDbIds.REVENUE;
                break;
            default:
                throw new FormatException("Unrecognized type: " + typeStr);
        }

        if(id == null || budget == null || value == null || dateMs == null)
        {
            throw new FormatException("Missing required data in JSON record");
        }


        description = (description != null ? description : "");
        account = (account != null ? account : "");
        note = (note != null ? note : "");
        receiptFilename = (receiptFilename != null ? receiptFilename : "");

        String receipt = "";
        if(receiptFilename.length() > 0)
        {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if(dir != null)
            {
                File imageFile = new File(dir, receiptFilename);
                if(imageFile.isFile())
                {
                    receipt = imageFile.getAbsolutePath();
                }
            }
        }

        helper.insertTransaction(database, id, type, description, account, budget, value, note, dateMs, receipt);
    }


    private void importBudget(SQLiteDatabase database, DBHelper helper, String name, Double value)
            throws FormatException
    {

        if(name == null || value == null)
        {
            throw new FormatException("Missing required data in JSON record");
        }

        helper.insertBudget(database, name, value.intValue());
    }
}
