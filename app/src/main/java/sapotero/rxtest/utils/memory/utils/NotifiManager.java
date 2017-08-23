package sapotero.rxtest.utils.memory.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;



public class NotifiManager {
    Context appContext = EsdApplication.getApplication();
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder ;

    public NotifiManager() {
        mBuilder = new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.drawable.ic_error)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
//                      .addAction(R.drawable.previous_step,"test title",)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        this.mNotificationManager = (NotificationManager) appContext.getSystemService(appContext.NOTIFICATION_SERVICE);

//        Intent openDocIntent = new Intent(appContext, DocumentInfocardFullScreenActivity.class);
//        openDocIntent.setAction("ACTION_VIEW");
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
//        stackBuilder.addParentStack(DocumentInfocardFullScreenActivity.class);
//        PendingIntent pendingIntentOpenDoc = PendingIntent.getActivities(stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT) )



    }

    void generateNotifyMsg() {
        //действие на кнопке "открыть документ"
         mNotificationManager.notify(0,mBuilder.build() );
    }


}
