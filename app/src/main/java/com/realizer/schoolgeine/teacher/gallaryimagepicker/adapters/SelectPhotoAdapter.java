package com.realizer.schoolgeine.teacher.gallaryimagepicker.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.realizer.schoolgeine.teacher.R;
import com.realizer.schoolgeine.teacher.Utils.Config;
import com.realizer.schoolgeine.teacher.Utils.Singlton;
import com.realizer.schoolgeine.teacher.gallaryimagepicker.utils.MediaStorePhoto;
import com.realizer.schoolgeine.teacher.gallaryimagepicker.utils.SquareImageView;

import java.util.ArrayList;




/**
 * Created by Kush on 9/11/2015.
 * Adapter for photo select
 */

public class SelectPhotoAdapter extends RecyclerView.Adapter {

    private ArrayList<MediaStorePhoto> bucketPhotoList;
    private Activity mAct;
    private SelectPhotoCallback selectPhotoCallback;
    int count=0;
    int imageCount=0;

    public SelectPhotoAdapter(ArrayList<MediaStorePhoto> bucketPhotoList, Activity mActivity) {
        this.bucketPhotoList = bucketPhotoList;
        this.mAct = mActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_image_item, parent, false);

        return new PhotoViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((PhotoViewHolder) holder).setId(position);
        ((PhotoViewHolder) holder).getImageView().setImageBitmap(null);
        ((PhotoViewHolder) holder).getSelectView().setChecked(bucketPhotoList.get(position).getStatus().equals("checked"));
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                return MediaStore.Images.Thumbnails.getThumbnail(
                        mAct.getContentResolver(),
                        Long.parseLong(bucketPhotoList.get(position).getId()),
                        MediaStore.Images.Thumbnails.MICRO_KIND,
                        null);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (((PhotoViewHolder) holder).getId() == position)
                    ((PhotoViewHolder) holder).getImageView().setImageBitmap(bitmap);
            }
        }.execute();

        count= Singlton.getImageList().size();
        //bucketPhotoList.get(position).setStatus(bucketPhotoList.get(position).getStatus());


        ((PhotoViewHolder) holder).getImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (bucketPhotoList.get(position).getStatus().equals("checked"))
                    {
                        bucketPhotoList.get(position).setStatus("null");
                        imageCount = imageCount-1;
                        if (selectPhotoCallback != null) {
                            selectPhotoCallback.selectViewPressed(position, bucketPhotoList.get(position).getStatus());
                        }
                        ((PhotoViewHolder) holder).getSelectView().setChecked(bucketPhotoList.get(position).getStatus().equals("checked"));
                    }
                    else
                    {
                        if(imageCount==10)
                        {
                           // Config.alertDialog(Singlton.getContext(), "Gallery", "Please Select only 10 image");
                        }
                        else
                        {
                            bucketPhotoList.get(position).setStatus("checked");
                            imageCount = imageCount+1;
                            if (selectPhotoCallback != null) {
                                selectPhotoCallback.selectViewPressed(position, bucketPhotoList.get(position).getStatus());
                            }
                            ((PhotoViewHolder) holder).getSelectView().setChecked(bucketPhotoList.get(position).getStatus().equals("checked"));
                        }

                    }



            }
        });

        ((PhotoViewHolder) holder).getSelectView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bucketPhotoList.get(position).getStatus().equals("checked")) {
                    bucketPhotoList.get(position).setStatus("null");
                    imageCount = imageCount-1;
                    if (selectPhotoCallback != null) {
                        selectPhotoCallback.selectViewPressed(position, bucketPhotoList.get(position).getStatus());
                    }
                    ((PhotoViewHolder) holder).getSelectView().setChecked(bucketPhotoList.get(position).getStatus().equals("checked"));
                }
                else {
                    if(imageCount==10)
                    {
                        //Config.alertDialog(mAct.getApplicationContext(), "Gallery", "Please Select only 10 image");
                    }
                    else {
                        bucketPhotoList.get(position).setStatus("checked");
                        imageCount = imageCount+1;
                        if (selectPhotoCallback != null) {
                            selectPhotoCallback.selectViewPressed(position, bucketPhotoList.get(position).getStatus());
                        }
                        ((PhotoViewHolder) holder).getSelectView().setChecked(bucketPhotoList.get(position).getStatus().equals("checked"));
                    }
                }



                Log.e("Status", bucketPhotoList.get(position).getStatus());
            }
        });
    }

    @Override
    public int getItemCount() {
        return bucketPhotoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setCallback(SelectPhotoCallback selectPhotoCallback) {
        this.selectPhotoCallback = selectPhotoCallback;
    }

    public interface SelectPhotoCallback {
        void selectViewPressed(int position, String status);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        private SquareImageView imageView;
        private CheckBox selectView;
        private int id;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = (SquareImageView) itemView.findViewById(R.id.bucket_image);
            selectView = (CheckBox) itemView.findViewById(R.id.select_check_box);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public CheckBox getSelectView() {
            return selectView;
        }
    }
}
