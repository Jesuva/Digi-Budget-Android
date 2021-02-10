package protect.budgetwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class TransactionDatabaseChangedReceiver extends BroadcastReceiver
{
    public static final String ACTION_DATABASE_CHANGED = "protect.budgetwatch.TRANSACTION_DATABASE_CHANGED";

    private boolean _hasChanged = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        _hasChanged = true;
    }

    public boolean hasChanged()
    {
        return _hasChanged;
    }

    public void reset()
    {
        _hasChanged = false;
    }
}