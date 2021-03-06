package com.realizer.schoolgeine.teacher.homework.newhomework;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.realizer.schoolgeine.teacher.DrawerActivity;
import com.realizer.schoolgeine.teacher.FragmentBackPressedListener;
import com.realizer.schoolgeine.teacher.Utils.Config;
import com.realizer.schoolgeine.teacher.Utils.ImageStorage;
import com.realizer.schoolgeine.teacher.Utils.Singlton;
import com.realizer.schoolgeine.teacher.backend.DatabaseQueries;
import com.realizer.schoolgeine.teacher.exceptionhandler.ExceptionHandler;
import com.realizer.schoolgeine.teacher.gallaryimagepicker.PhotoAlbumActivity;
import com.realizer.schoolgeine.teacher.homework.TeacherHomeworkFragment;
import com.realizer.schoolgeine.teacher.homework.model.TeacherHomeworkModel;
import com.realizer.schoolgeine.teacher.homework.newhomework.adapter.NewHomeworkGalleryAdapter;
import com.realizer.schoolgeine.teacher.R;
import com.realizer.schoolgeine.teacher.myclass.TeacherMyClassDialogBoxActivity;
import com.realizer.schoolgeine.teacher.view.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Bhagyashri on 10/6/2016.
 */
public class NewHomeworkActivity extends Fragment implements FragmentBackPressedListener {

    String htext;
    TextView txtstd,txtclss,hwMessage;
    EditText homeworktext;
    GridView gridView;
    ImageButton addImage;
    ArrayList<String> templist;
    ArrayList<TeacherHomeworkModel> hwimage;
    NewHomeworkGalleryAdapter adapter;
    ArrayList<String> base64imageList;
    DatabaseQueries qr ;
    int hid = 0;
    String date = null;
    Spinner spinnersub, spinnerdate;
    ArrayList<String> listofDate = new ArrayList<>();
    ArrayList<String> listofDay = new ArrayList<>();
    int REQUEST_CAMERA = 100;
    ProgressWheel loading ;
    File cameraCapturedFile;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        final View rootView = inflater.inflate(R.layout.new_homework_layout, container, false);
        setHasOptionsMenu(true);
        Singlton.setFialbitmaplist(new ArrayList<TeacherHomeworkModel>());
        Bundle b = getArguments();
        htext = b.getString("HEADERTEXT");
        templist = new ArrayList<>();
        qr = new DatabaseQueries(getActivity());
        ((DrawerActivity) getActivity()).getSupportActionBar().setTitle(Config.actionBarTitle(htext, getActivity()));
        ((DrawerActivity) getActivity()).getSupportActionBar().show();

        initiateView(rootView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        txtstd.setText(preferences.getString("STANDARD", ""));
        txtclss.setText(preferences.getString("DIVISION", ""));


        FillSubjectTypes();
        FillDates();

        spinnersub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                loading.setVisibility(View.VISIBLE);
                String sub1 = spinnersub.getSelectedItem().toString();
                String date1 = listofDate.get(spinnerdate.getSelectedItemPosition());


                int cnt = qr.GetHomeworkForSub(date1,htext.toString(),txtstd.getText().toString(),txtclss.getText().toString(),sub1);
                if(cnt>0)
                {
                    loading.setVisibility(View.GONE);
                    hwMessage.setText(htext+" provided for this subject today");
                    hwMessage.setVisibility(View.VISIBLE);
                    addImage.setVisibility(View.GONE);
                    homeworktext.setEnabled(false);
                }
                else
                {
                    loading.setVisibility(View.GONE);
                    hwMessage.setVisibility(View.GONE);
                    addImage.setVisibility(View.VISIBLE);
                    homeworktext.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootView;
    }

    public void SwitchClass()
    {

        String classList="1.,,2nd,,B,,Hindi@@@2.,,4th,,A,,English@@@3.,,2nd,,A,,English@@@4.,,7th,,B,,History@@@5.,,3rd,,B,,English@@@6.,,6th,,B,,History";
        TeacherMyClassDialogBoxActivity newTermDialogFragment = new TeacherMyClassDialogBoxActivity();
        Singlton.setSelectedFragment(newTermDialogFragment);
        FragmentManager fragmentManager = getFragmentManager();
        Bundle b =new Bundle();
        b.putString("StudentClassList", classList);
        b.putInt("MYCLASS", 10);
        b.putString("HeaderText", htext);
        newTermDialogFragment.setArguments(b);
        newTermDialogFragment.setCancelable(false);
        newTermDialogFragment.show(fragmentManager, "Dialog!");
    }


    public void FillDates() {

        listofDate.clear();
        listofDay.clear();
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int currentdate = c.get(Calendar.DATE);
        if(currentdate>=1 && currentdate<10) {
            if(month>=1 && month<10)
                listofDate.add(0, "0" + currentdate + "/0" + month + "/" + year);
            else
                listofDate.add(0, "0" + currentdate + "/" + month + "/" + year);
        }
        else
        {
            if(month>=1 && month<10)
                listofDate.add(0, "" + currentdate + "/0" + month + "/" + year);
            else
                listofDate.add(0, "" + currentdate + "/" + month + "/" + year);
        }
        listofDay.add(0, "Today" + " (" + currentdate + " " + Config.getMonth(month) + ")");
        for (int i = 1; i <7; i++) {
            c.add(Calendar.DATE, 1);
            int month1 = c.get(Calendar.MONTH) + 1;
            int year1 = c.get(Calendar.YEAR);
            int currentdate1 = c.get(Calendar.DATE);
            if(currentdate1>=1 && currentdate1<10) {
                if(month1>=1 && month1<10)
                    listofDate.add(i, "0" + currentdate1 + "/0" + month1 + "/" + year1);
                else
                    listofDate.add(i, "0" + currentdate1 + "/" + month1 + "/" + year1);
            }
            else {
                if(month1>=1 && month1<10)
                    listofDate.add(i, "" + currentdate1 + "/0" + month1 + "/" + year1);
                else
                    listofDate.add(i, "" + currentdate1 + "/" + month1 + "/" + year1);
            }

            listofDay.add(i, Config.getDayOfWeek(c.get(Calendar.DAY_OF_WEEK)) + " (" +currentdate1+" "+ Config.getMonth(month1) + ")");
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, listofDay);
        adapter.setDropDownViewResource(R.layout.viewstar_subject_spiner);
        for (int i = 0; i < adapter.toString().length(); i++) {
            spinnerdate.setAdapter(adapter);
            break;
        }

        spinnerdate.setSelection(0);
        spinnerdate.setEnabled(false);
    }

    public void FillSubjectTypes()
    {
        // set adapter

        ArrayList<String> listofSubject = qr.GetSub(txtstd.getText().toString(), txtclss.getText().toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, listofSubject);
        adapter.setDropDownViewResource(R.layout.viewstar_subject_spiner);
        for(int i=0;i<adapter.toString().length();i++) {
            spinnersub.setAdapter(adapter);
            break;
        }
        spinnersub.setSelection(0);
    }

    public void initiateView(View rootview)
    {
        addImage = (ImageButton) rootview.findViewById(R.id.addimage);
        homeworktext = (EditText) rootview.findViewById(R.id.edtmsgtxt);
        gridView= (GridView) rootview.findViewById(R.id.gallerygridView);
        spinnersub = (Spinner) rootview.findViewById(R.id.spinnersub);
        spinnerdate = (Spinner) rootview.findViewById(R.id.spinnerdate);
        txtstd  = (TextView) rootview.findViewById(R.id.txtstdname);
        txtclss = (TextView) rootview.findViewById(R.id.txttdivname);
        loading = (ProgressWheel)rootview.findViewById(R.id.loading);
        hwMessage = (TextView) rootview.findViewById(R.id.tvNoDataMsg);

        homeworktext.setHint("Enter " + htext + " Text");
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font.ttf");

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.imagepickerdialog_layout, null);
                ImageButton camera_btn = (ImageButton) dialoglayout.findViewById(R.id.img_camera);
                ImageButton gallery_btn = (ImageButton) dialoglayout.findViewById(R.id.img_gallary);
                Button cancel = (Button) dialoglayout.findViewById(R.id.btn_cancel);
                cancel.setTypeface(face);

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialoglayout);

                final AlertDialog alertDialog = builder.create();


                camera_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraCapturedFile= new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "temp.png");

                        Uri tempURI = Uri.fromFile(cameraCapturedFile);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempURI);
                        startActivityForResult(intent, REQUEST_CAMERA);
                        alertDialog.dismiss();
                    }
                });


                gallery_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getActivity(), PhotoAlbumActivity.class);
                        Bundle b = new Bundle();
                        b.putBoolean("FunCenter", false);
                        b.putBoolean("Homework", true);
                        intent.putExtras(b);
                        getActivity().startActivity(intent);
                        alertDialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            }
        });
    }

    @Override
    public void onFragmentBackPressed() {
        Singlton.setSelectedFragment(Singlton.getMainFragment());
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
    }

    public class GetImagesForEvent extends AsyncTask<Void, Void,Void>
    {


        ArrayList<String> temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // loading.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(Void... params) {

             templist = new ArrayList<>();
             base64imageList = new ArrayList<>();
             templist.addAll(Singlton.getImageList());
             Singlton.setImageList(new ArrayList<String>());
             hwimage = new ArrayList<>();
             temp = new ArrayList<>();

            for(int i=0;i<templist.size();i++)
            {
                String path = templist.get(i).toString();
                Bitmap bitmap =BitmapFactory.decodeFile(path);

                TeacherHomeworkModel obj = new TeacherHomeworkModel();
                obj.setPic(bitmap);

                hwimage.add(i, obj);
                temp.add(i,path);
            }
            if(templist.size()<10)
            {
                Bitmap icon = BitmapFactory.decodeResource(NewHomeworkActivity.this.getResources(),
                        R.drawable.addimageicon);
                TeacherHomeworkModel obj = new TeacherHomeworkModel();
                obj.setPic(icon);
                obj.setHwTxtLst("NoIcon");
                hwimage.add(templist.size(),obj);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if(templist.size()>0) {
                addImage.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                adapter = new NewHomeworkGalleryAdapter(getActivity(), hwimage,temp,true,NewHomeworkActivity.this);
                gridView.setAdapter(adapter);
                gridView.setFastScrollEnabled(true);
            }
            else
            {
                addImage.setVisibility(View.VISIBLE);
            }
            //loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_switchclass:
                Config.hideSoftKeyboardWithoutReq(getActivity(), homeworktext);
                SwitchClass();
                return true;
            case R.id.action_done:
                Config.hideSoftKeyboardWithoutReq(getActivity(), homeworktext);
                saveHomework();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }


    public void saveHomework() {
        if(txtstd.getText().toString().isEmpty() )
        {
            Config.alertDialog(Singlton.getContext(), "New " + htext, "Please Select Standard");
            // Toast.makeText(getActivity(), "Please Select Standard", Toast.LENGTH_SHORT).show();
        }
        else if(txtclss.getText().toString().isEmpty())
        {
            Config.alertDialog(Singlton.getContext(), "New "+htext, "Please Select Division");
            // Toast.makeText(getActivity(), "Please Select Division", Toast.LENGTH_SHORT).show();
        }
        else if( spinnersub.getSelectedItem().toString().isEmpty())
        {
            Config.alertDialog(Singlton.getContext(), "New "+htext, "Please Select Subject");
            // Toast.makeText(getActivity(), "Please Select Subject", Toast.LENGTH_SHORT).show();
        }
        else if( spinnerdate.getSelectedItem().toString().isEmpty())
        {
            Config.alertDialog(Singlton.getContext(), "New "+htext, "Please Select Date");
            // Toast.makeText(getActivity(), "Please Select Date", Toast.LENGTH_SHORT).show();
        }
        else if(homeworktext.getText().toString().isEmpty())
        {
            Config.alertDialog(Singlton.getContext(), "New "+htext, "Please Enter "+htext+" Description");
            //Toast.makeText(getActivity(), "Please Enter Description", Toast.LENGTH_SHORT).show();
        }
        else {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String sub = spinnersub.getSelectedItem().toString();
        String date = listofDate.get(spinnerdate.getSelectedItemPosition());
        JSONArray imglstbase64;
        String txtlst;
        if (homeworktext.getText().toString().trim().length() > 0)
            txtlst = homeworktext.getText().toString();
        else
            txtlst = "No Homework Text";


        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String givenby = sharedpreferences.getString("UidName", "");

        ArrayList<TeacherHomeworkModel> tempImageList = new ArrayList<>();
        tempImageList = Singlton.getFialbitmaplist();
        for (int i = 0; i < tempImageList.size(); i++) {
            if (tempImageList.get(i).getHwTxtLst().equals("NoIcon")) {
                tempImageList.remove(i);
            }
        }
        String encodedImage = "";
        String hwUUID = String.valueOf(UUID.randomUUID());

            if(tempImageList.size()>0) {
                for (int i = 0; i < tempImageList.size(); i++) {
                    imglstbase64 = new JSONArray();

                    encodedImage = ImageStorage.saveEventToSdCard(tempImageList.get(i).getPic(), "Homework", getActivity());

                    try {
                        imglstbase64.put(0, encodedImage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    long n = qr.insertHomework(givenby, sub, date, txtlst, encodedImage, sharedpreferences.getString("STANDARD", ""), sharedpreferences.getString("DIVISION", ""), htext, hwUUID);
                    if (n > 0) {
                        // Toast.makeText(getActivity(), "Homework Inserted Successfully", Toast.LENGTH_SHORT).show();
                        n = -1;

                        hid = qr.getHomeworkId();
                        SimpleDateFormat df1 = new SimpleDateFormat("dd MMM hh:mm:ss a");
                        n = qr.insertQueue(hid, "Homework", "1", df1.format(calendar.getTime()));
                    }

                }
            }
            else
            {
                encodedImage ="NoIcon";
                long n = qr.insertHomework(givenby, sub, date, txtlst, encodedImage, sharedpreferences.getString("STANDARD", ""), sharedpreferences.getString("DIVISION", ""), htext, hwUUID);
                if (n > 0) {
                    // Toast.makeText(getActivity(), "Homework Inserted Successfully", Toast.LENGTH_SHORT).show();
                    n = -1;

                    hid = qr.getHomeworkId();
                    SimpleDateFormat df1 = new SimpleDateFormat("dd MMM hh:mm:ss a");
                    n = qr.insertQueue(hid, "Homework", "1", df1.format(calendar.getTime()));
                }
            }

        TeacherHomeworkFragment fragment = new TeacherHomeworkFragment();
        Singlton.setMainFragment(fragment);
        Singlton.setSelectedFragment(fragment);
        Bundle bundle = new Bundle();
        bundle.putString("HEADERTEXT", htext);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
      }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(Singlton.getImageList().size()>0)
        {
            new GetImagesForEvent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            addImage.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
       /* Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
        Uri tempUri = getImageUri(getActivity(), thumbnail);

        // CALL THIS METHOD TO GET THE ACTUAL PATH
        File finalFile = new File(getRealPathFromURI(tempUri));*/
        ArrayList<String> imageList = new ArrayList<>();
        imageList = Singlton.getImageList();

        imageList.add(imageList.size(), cameraCapturedFile.getAbsolutePath());
        Singlton.setImageList(imageList);

        if(Singlton.getImageList().size()>0)
        {
            new GetImagesForEvent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            addImage.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        }

       // ivImage.setImageBitmap(thumbnail);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }



}
