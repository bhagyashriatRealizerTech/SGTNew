package com.realizer.schoolgeine.teacher.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.gson.Gson;
import com.realizer.schoolgeine.teacher.DrawerActivity;
import com.realizer.schoolgeine.teacher.Notification.NotificationModel;
import com.realizer.schoolgeine.teacher.Utils.Config;
import com.realizer.schoolgeine.teacher.Utils.OnTaskCompleted;
import com.realizer.schoolgeine.teacher.Utils.Singlton;
import com.realizer.schoolgeine.teacher.backend.DatabaseQueries;
import com.realizer.schoolgeine.teacher.chat.asynctask.TeacherQueryAsyncTaskPost;
import com.realizer.schoolgeine.teacher.chat.model.TeacherQuerySendModel;
import com.realizer.schoolgeine.teacher.exceptionhandler.asynctask.ExceptionAsyncTaskPost;
import com.realizer.schoolgeine.teacher.exceptionhandler.model.ExceptionModel;
import com.realizer.schoolgeine.teacher.funcenter.asynctask.GoogleDriveImageUploadAsyncTask;
import com.realizer.schoolgeine.teacher.funcenter.asynctask.TeacherFunCenterAsyncTaskPost;
import com.realizer.schoolgeine.teacher.funcenter.asynctask.TeacherFunCenterImageAsynckPost;
import com.realizer.schoolgeine.teacher.funcenter.model.GoogleDriveUploadClass;
import com.realizer.schoolgeine.teacher.funcenter.model.TeacherFunCenterEventModel;
import com.realizer.schoolgeine.teacher.funcenter.model.TeacherFunCenterImageModel;
import com.realizer.schoolgeine.teacher.generalcommunication.asynctask.TeacherGCommunicationAsyncTaskPost;
import com.realizer.schoolgeine.teacher.generalcommunication.model.TeacherGeneralCommunicationListModel;
import com.realizer.schoolgeine.teacher.homework.asynctask.TeacherClassworkAsyncTaskPost;
import com.realizer.schoolgeine.teacher.homework.asynctask.TeacherHomeworkAsyncTaskPost;
import com.realizer.schoolgeine.teacher.homework.model.TeacherHomeworkModel;
import com.realizer.schoolgeine.teacher.myclass.asynctask.TeacherAttendanceAsyncTaskPost;
import com.realizer.schoolgeine.teacher.myclass.model.TeacherAttendanceListModel;
import com.realizer.schoolgeine.teacher.queue.QueueListModel;
import com.realizer.schoolgeine.teacher.star.asynctask.TeacherGiveStarAsyncTaskPost;
import com.realizer.schoolgeine.teacher.star.model.TeacherGiveStarModel;
import com.realizer.schoolgeine.teacher.timetable.asynctask.TeacherTimeTableAsyncTask;
import com.realizer.schoolgeine.teacher.timetable.model.TeacherTimeTableExamListModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by shree on 11/20/2015.
 */
public class ManualSyncService extends Service implements OnTaskCompleted {

    DatabaseQueries qr;
    String type;
    int id;
    Handler handler;
    ProgressDialog dialog;
    AlertDialog.Builder adbdialog;
    ArrayList<QueueListModel> quelist;
    Context mContext;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Singlton.setManualserviceIntent(null);
        Log.d("ManualSyncService", "Stop");
       // Toast.makeText(this, "Service Destroy", Toast.LENGTH_LONG).show();
    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //Toast.makeText(this, "Service LowMemory", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        qr = new DatabaseQueries(this);
        Log.d("ManualSyncService", "Start");
        handler = new Handler();
        BackgroundThread background = new BackgroundThread();
        background.start();

        return START_NOT_STICKY;
    }

   private class BackgroundThread extends Thread{
       @Override
       public void run() {
           super.run();
           if(Config.isConnectingToInternet(Singlton.getContext()))
           {
               Syncdata();
           }
       }
   }


    @Override
    public void onTaskCompleted(String s, final QueueListModel queueListModel) {

        if (s.toString().equalsIgnoreCase("true"))
        {
            if(queueListModel.getType().equalsIgnoreCase("Query"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(), queueListModel.getType());
                if (n > 0) {
                    TeacherQuerySendModel o = qr.GetQuery(queueListModel.getId());
                    n = qr.updateQurySyncFlag(o);
                }
            }

            else if(queueListModel.getType().equalsIgnoreCase("GiveStar"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(), queueListModel.getType());
                if (n > 0) {
                    TeacherGiveStarModel o = qr.GetStar(queueListModel.getId());
                    n= qr.updateGiveStarSyncFlag(o);
                }
            }

            else if(queueListModel.getType().equalsIgnoreCase("Attendance"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(),queueListModel.getType());
                if (n > 0) {
                    n = qr.updateAttendanceSyncFlag(qr.GetAttendanceID(queueListModel.getId()));
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("Announcement"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(),queueListModel.getType());
                if(n>=0) {
                    TeacherGeneralCommunicationListModel obj = qr.GetAnnouncementID(queueListModel.getId());
                    n = qr.updateAnnouncementSyncFlag(obj);
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("TimeTable"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(),queueListModel.getType());
                TeacherTimeTableExamListModel timeTableObj = new TeacherTimeTableExamListModel();
                if(n>0) {
                    timeTableObj = qr.GetTimeTable(queueListModel.getId());
                    n = qr.updateTimeTableSyncFlag(timeTableObj);

                    if(n>0)
                    {
                        n =0;
                        NotificationModel obj = new NotificationModel();
                        obj.setNotificationId(timeTableObj.getTtid());
                        obj.setNotificationDate(timeTableObj.getDate());
                        obj.setNotificationtype("TimeTable");
                        obj.setMessage("Uploaded Successfully for");
                        obj.setIsRead("false");
                        obj.setAdditionalData2("");
                        obj.setAdditionalData1(timeTableObj.getTitle() + "@@@" + timeTableObj.getStandard() + "@@@" +
                                timeTableObj.getDivision());
                        n = qr.InsertNotification(obj);

                        if(Singlton.getResultReceiver() != null)
                            Singlton.getResultReceiver().send(1,null);
                    }
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("Exception"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(),queueListModel.getType());
                if(n>=0) {
                    ExceptionModel obj = qr.GetException(queueListModel.getId());
                    n = qr.updateException(obj);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (id == queueListModel.getId()) {
                        if(!((Activity) Singlton.getContext()).isFinishing())
                        Config.alertDialog(Singlton.getContext(), "Manual Sync", "Sync Completed Successfully");

                        Intent intent = Singlton.getManualserviceIntent();
                        Singlton.setManualserviceIntent(null);
                        if(intent != null)
                        stopService(intent);

                        //Toast.makeText(Singlton.getContext(),"Sync Completed Successfully",Toast.LENGTH_SHORT).show();
                        if(Singlton.getResultReceiver() != null)
                            Singlton.getResultReceiver().send(1000,null);
                   /* if (dialog != null && dialog.isShowing())
                        dialog.dismiss();*/
                    }
                }
            });


        }
        else if(s.replace("\"","").equalsIgnoreCase("success"))
        {

            if(queueListModel.getType().equalsIgnoreCase("EventMaster"))
            {
                long n = qr.deleteQueueRow(queueListModel.getId(), "EventMaster");
                if (n > 0) {
                    n = qr.updateEventSyncFlag(qr.GetEventByID(queueListModel.getId()));
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("EventImages")) {
                long n = qr.deleteQueueRow(queueListModel.getId(), "EventImages");
                if (n > 0) {
                    n = qr.updateImageSyncFlag(qr.getImageById(queueListModel.getId()));
                }
            }
            else {
                long n = qr.deleteQueueRow(queueListModel.getId(), queueListModel.getType());
                TeacherHomeworkModel homeworkObj = new TeacherHomeworkModel();
                if (n > 0) {
                    homeworkObj = qr.GetHomework(queueListModel.getId());
                    n = qr.updateHomeworkSyncFlag(homeworkObj);
                    if(n>0)
                    {
                        n =0;
                        NotificationModel obj = new NotificationModel();
                        obj.setNotificationId(homeworkObj.getHid());
                        obj.setNotificationDate(homeworkObj.getHwDate());
                        obj.setNotificationtype(homeworkObj.getWork());
                        obj.setMessage("Uploaded Successfully for");
                        obj.setIsRead("false");
                        obj.setAdditionalData2("");
                        obj.setAdditionalData1(homeworkObj.getStd()+"@@@"+homeworkObj.getDiv()+"@@@"+
                                homeworkObj.getSubject());
                        n = qr.InsertNotification(obj);


                        Bundle b = new Bundle();
                        b.putInt("NotificationId", homeworkObj.getHid());
                        b.putString("NotificationDate", homeworkObj.getHwDate());
                        b.putString("NotificationType", homeworkObj.getWork());
                        b.putString("NotificationMessage", "Uploaded Successfully for");
                        b.putString("IsNotificationread", "false");
                        b.putString("AdditionalData1", homeworkObj.getStd()+"@@@"+homeworkObj.getDiv()+"@@@"+
                                homeworkObj.getSubject());
                        b.putString("AdditionalData2","");

                        if(Singlton.getResultReceiver() != null)
                            Singlton.getResultReceiver().send(1,null);
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (id == queueListModel.getId()) {
                        if(!((Activity) Singlton.getContext()).isFinishing())
                        Config.alertDialog(Singlton.getContext(), "Manual Sync", "Sync Completed Successfully");

                        //Toast.makeText(Singlton.getContext(),"Sync Completed Successfully",Toast.LENGTH_SHORT).show();
                        if (Singlton.getResultReceiver() != null)
                            Singlton.getResultReceiver().send(1000, null);
                   /* if (dialog != null && dialog.isShowing())
                        dialog.dismiss();*/
                    }
                }
            });

        }
        else if(s.equalsIgnoreCase("done"))
        {

            if(queueListModel.getType().equalsIgnoreCase("EventImages")) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ManualSyncService.this);
                TeacherFunCenterImageModel o = qr.getImageById(queueListModel.getId());
                o.setSharedlink(queueListModel.getTime());
                qr.updateSharedImageLink(o);

                if(!TextUtils.isEmpty(o.getSharedlink())) {
                    TeacherFunCenterImageAsynckPost objasync = new TeacherFunCenterImageAsynckPost(o, preferences.getString("STANDARD", ""), preferences.getString("DIVISION", ""), ManualSyncService.this, ManualSyncService.this, "false");
                    objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("EventMaster")) {
                TeacherFunCenterEventModel o = qr.GetEventByID(queueListModel.getId());
                o.setSharedlink(queueListModel.getTime());

                qr.updateEventSharedLink(o);
                if(!TextUtils.isEmpty(o.getSharedlink())) {
                    TeacherFunCenterAsyncTaskPost objasync = new TeacherFunCenterAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                    objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("Homework")) {
                TeacherHomeworkModel o = qr.GetHomework(queueListModel.getId());
                o.setSharedLink(queueListModel.getTime());

                qr.updateHomeworkSharedLink(o);
                if(!TextUtils.isEmpty(o.getSharedLink())) {

                        TeacherHomeworkAsyncTaskPost obj = new TeacherHomeworkAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                        obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("Classwork")) {
                TeacherHomeworkModel o = qr.GetHomework(queueListModel.getId());
                o.setSharedLink(queueListModel.getTime());

                qr.updateHomeworkSharedLink(o);
                if(!TextUtils.isEmpty(o.getSharedLink())) {

                    TeacherClassworkAsyncTaskPost obj = new TeacherClassworkAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                    obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            }
            else if(queueListModel.getType().equalsIgnoreCase("TimeTable")) {

                TeacherTimeTableExamListModel o = qr.GetTimeTable(queueListModel.getId());
                o.setSharedLink(queueListModel.getTime());

                qr.updateTimeTableSharedLink(o);

                TeacherTimeTableAsyncTask obj = new TeacherTimeTableAsyncTask(o, ManualSyncService.this, ManualSyncService.this, "false");
                obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }
        else {
            if(!((Activity) Singlton.getContext()).isFinishing())
            Config.alertDialog(Singlton.getContext(),"Network Error","Server Not Responding");
        }



    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public void Syncdata()
    {

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ManualSyncService.this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        SimpleDateFormat df1 = new SimpleDateFormat("dd MMM hh:mm:ss a");
        edit.putString("LastSyncUpTime", df1.format(Calendar.getInstance().getTime()));
        edit.commit();

        final ArrayList<QueueListModel> lst = qr.GetQueueData();
        Log.d("TIMER", " " + Calendar.getInstance().getTime() + ": " + lst.size());
        if(lst.size()>0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    adbdialog = new AlertDialog.Builder(Singlton.getContext());
                    adbdialog.setTitle("Manual Sync");
                    adbdialog.setMessage("Sync will be Performed in Background, you will be Notified once sync is Completed.");
                    //adbdialog.setIcon(android.R.drawable.ic_dialog_info);
                    adbdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                           /* Intent intent = new Intent(ManualSyncService.this,DrawerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);*/

                            for (int i = 0; i < lst.size(); i++) {
                                id = lst.get(i).getId();
                                type = lst.get(i).getType();
                                if (type.equals("Query")) {
                                    TeacherQuerySendModel obj = qr.GetQuery(id);
                                    TeacherQueryAsyncTaskPost asyncobj = new TeacherQueryAsyncTaskPost(obj, ManualSyncService.this, ManualSyncService.this, "false");
                                    asyncobj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                } else if (type.equals("Homework")) {

                                    if (Singlton.getmCredential() != null) {
                                        TeacherHomeworkModel o = qr.GetHomework(id);
                                        GoogleDriveUploadClass o1 = new GoogleDriveUploadClass();
                                        o1.setGdID(Integer.valueOf(o.getHid()));
                                        o1.setFilepath(o.getHwImage64Lst());
                                        if (o.getWork().equalsIgnoreCase("Homework"))
                                            o1.setFoldername(Config.HOMEWORK_FOLDER);
                                        else
                                            o1.setFoldername(Config.CLASSWORK_FOLDER);

                                        o1.setGdfilename(o.getHwImage64Lst());
                                        o1.setGdtype(o.getWork());
                                        if (TextUtils.isEmpty(o.getSharedLink()) && !o.getHwImage64Lst().equalsIgnoreCase("NoIcon")) {
                                            GoogleDriveImageUploadAsyncTask objasync = new GoogleDriveImageUploadAsyncTask(Singlton.getmCredential(), ManualSyncService.this, o1, ManualSyncService.this);
                                            objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                        } else {
                                            if (o.getWork().equalsIgnoreCase("Homework")) {
                                                TeacherHomeworkAsyncTaskPost obj = new TeacherHomeworkAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                                                obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                            } else if (o.getWork().equalsIgnoreCase("Classwork")) {
                                                TeacherClassworkAsyncTaskPost obj = new TeacherClassworkAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                                                obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                            }
                                        }
                                    }
                                } else if (type.equals("GiveStar")) {
                                    TeacherGiveStarModel o = qr.GetStar(id);
                                    TeacherGiveStarAsyncTaskPost obj = new TeacherGiveStarAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                                    obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                } else if (type.equals("Attendance")) {
                                    TeacherAttendanceListModel o = qr.GetAttendanceID(id);
                                    TeacherAttendanceAsyncTaskPost obj = new TeacherAttendanceAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                                    obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                } else if (type.equals("Announcement")) {
                                    TeacherGeneralCommunicationListModel obj = qr.GetAnnouncementID(id);
                                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ManualSyncService.this);
                                    String scode = sharedpreferences.getString("SchoolCode", "");
                                    obj.setSchoolCode(scode);
                                    TeacherGCommunicationAsyncTaskPost asyncobj = new TeacherGCommunicationAsyncTaskPost(obj, ManualSyncService.this, ManualSyncService.this, "false");
                                    asyncobj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                } else if (type.equals("EventMaster")) {
                                    TeacherFunCenterEventModel o = qr.GetEventByID(id);
                                    GoogleDriveUploadClass o1 = new GoogleDriveUploadClass();
                                    o1.setGdID(Integer.valueOf(o.getEventId()));
                                    o1.setFilepath(o.getThumbNailImage());
                                    o1.setFoldername(Config.FUN_CENTER_FOLDER);
                                    o1.setGdfilename(o.getFilename());
                                    o1.setGdtype("EventMaster");
                                    if (TextUtils.isEmpty(o.getSharedlink())) {
                                        GoogleDriveImageUploadAsyncTask objasync = new GoogleDriveImageUploadAsyncTask(Singlton.getmCredential(), ManualSyncService.this, o1, ManualSyncService.this);
                                        objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                    } else {
                                        TeacherFunCenterAsyncTaskPost objasync = new TeacherFunCenterAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this, "false");
                                        objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                    }
                                } else if (type.equals("EventImages")) {

                                    if (Singlton.getmCredential() != null) {
                                        TeacherFunCenterImageModel o = qr.getImageById(id);
                                        GoogleDriveUploadClass o1 = new GoogleDriveUploadClass();
                                        o1.setGdID(Integer.valueOf(o.getImageid()));
                                        o1.setFilepath(o.getImage());
                                        o1.setFoldername(Config.FUN_CENTER_FOLDER);
                                        o1.setGdfilename(o.getFilename());
                                        o1.setGdtype("EventImages");
                                        if (TextUtils.isEmpty(o.getSharedlink()) || o.getSharedlink().equalsIgnoreCase("NoData")) {
                                            GoogleDriveImageUploadAsyncTask objasync = new GoogleDriveImageUploadAsyncTask(Singlton.getmCredential(), ManualSyncService.this, o1, ManualSyncService.this);
                                            objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                        } else {
                                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ManualSyncService.this);
                                            TeacherFunCenterImageAsynckPost objasync = new TeacherFunCenterImageAsynckPost(o, preferences.getString("STANDARD", ""), preferences.getString("DIVISION", ""), ManualSyncService.this, ManualSyncService.this, "false");
                                            objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                        }
                                    }

                                } else if (type.equals("TimeTable")) {


                                    TeacherTimeTableExamListModel o = qr.GetTimeTable(id);
                                    GoogleDriveUploadClass o1 = new GoogleDriveUploadClass();
                                    o1.setGdID(Integer.valueOf(o.getTtid()));
                                    o1.setFilepath(o.getImage());
                                    o1.setFoldername(Config.TIMETABLE_FOLDER);
                                    o1.setGdfilename(o.getImage());
                                    o1.setGdtype("TimeTable");
                                    if (TextUtils.isEmpty(o.getSharedLink())) {
                                        GoogleDriveImageUploadAsyncTask objasync = new GoogleDriveImageUploadAsyncTask(Singlton.getmCredential(), ManualSyncService.this, o1, ManualSyncService.this);
                                        objasync.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                    } else {
                                        TeacherTimeTableAsyncTask obj = new TeacherTimeTableAsyncTask(o, ManualSyncService.this, ManualSyncService.this, "false");
                                        obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                    }


                                } else if (type.equals("Exception")) {
                                    ExceptionModel o = qr.GetException(id);
                                    ExceptionAsyncTaskPost obj = new ExceptionAsyncTaskPost(o, ManualSyncService.this, ManualSyncService.this);
                                    obj.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                                }


                            }

                        }
                    });


                    adbdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(Singlton.getManualserviceIntent() != null)
                           stopService(Singlton.getManualserviceIntent());
                        } });
                    if(!((Activity) Singlton.getContext()).isFinishing())
                    adbdialog.show();
                }
            });
            //Toast.makeText(this,"Sync Start...",Toast.LENGTH_SHORT).show();

        }
        else
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!((Activity) Singlton.getContext()).isFinishing())
                    Config.alertDialog(Singlton.getContext(), "Manual Sync", "There is No Data to Sync");
                    //Toast.makeText(Singlton.getContext(), "No Data to Sync", Toast.LENGTH_SHORT).show();
                }
            });

            if(Singlton.getManualserviceIntent() != null)
                stopService(Singlton.getManualserviceIntent());
        }
    }





}
