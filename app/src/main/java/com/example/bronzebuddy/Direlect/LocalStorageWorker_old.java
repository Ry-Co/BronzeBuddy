package com.example.bronzebuddy.Direlect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//We can abstract the saveimage method and AsyncTask to other objects if needed

//by default only Internal Storage is in use

//HOWEVER, if you read from a users phone for a profile picture
//then permission READ_EXTERNAL_STORAGE is required

public class LocalStorageWorker_old {
    private static final String TAG = LocalStorageWorker_old.class.getSimpleName();
    private String directoryName, fileName;
    Context mContext;
    boolean external = false;

    public LocalStorageWorker_old(Context context){
        mContext=context;
    }

    private File createFile(){
        File directory;
        if(external){
            directory = getAlbumStorageDir(directoryName);
        }else{
            directory = mContext.getDir(directoryName, Context.MODE_PRIVATE);
        }
        if(!directory.exists() && !directory.mkdirs()){
            Log.e(TAG, "Error with directory creation"+directory);
        }
        return new File(directory, fileName);
    }

    public boolean deleteFile(){
        File file = createFile();
        return file.delete();
    }

    public void saveImage(boolean async,Bitmap bitmap){
        FileOutputStream fileOutputStream = null;
        if(async){
            saveImageAsync task = new saveImageAsync();
            task.execute(bitmap);
        }else{
            try{
                fileOutputStream = new FileOutputStream(createFile());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try {
                    if(fileOutputStream != null){
                        fileOutputStream.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap loadImage() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(createFile());
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public LocalStorageWorker_old setDirectoryName(String DirectoryName){
        this.directoryName = DirectoryName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalStorageWorker_old setFileName(String FileName){
        this.fileName = FileName;
        return this;
    }

    public Context getmContext() {
        return mContext;
    }

    public LocalStorageWorker_old setContext(Context context){
        this.mContext = context;
        return this;
    }

    public boolean isExternal() {
        return external;
    }

    public LocalStorageWorker_old setExternal(boolean ex){
        this.external = ex;
        return this;
    }

    public class saveImageAsync extends AsyncTask<Bitmap, Integer, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            FileOutputStream fileOutputStream = null;
            for(Bitmap b : bitmaps){
                //publishProgress()
                try{
                    fileOutputStream = new FileOutputStream(createFile());
                    b.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    try {
                        if(fileOutputStream != null){
                            fileOutputStream.close();
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
