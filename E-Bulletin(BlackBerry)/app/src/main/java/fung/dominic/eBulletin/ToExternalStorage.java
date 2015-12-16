package fung.dominic.eBulletin;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ToExternalStorage {

    private static final String TAG = "External";
    public static final String DEFAULT_NAME = "bulletin";


    public static Uri ToExternalStorage(File file, String name){

        if(isExternalStorageWritable()){

            name = name.replace("/", "-");

            File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Documents");
            boolean isPresent = true;
            File newFile;

                Log.d("ToExternalStorage", "file Transfer");

            try{

                path.mkdirs();
                if (!path.exists()){
                    isPresent = path.createNewFile();
                    Log.i("ToExternal", String.valueOf(isPresent));
                }

                if(isPresent){
                    newFile = new File(path.getAbsolutePath(), name + ".pdf");
                    Log.i("ToExternal", newFile.getAbsolutePath());
                }
                else{
                    Log.d("ThisLog", "returning Null");
                    return null;
                }
                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = new FileInputStream(file);

                while ((bytesRead = inputStream.read(buffer)) > 0){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = byteArrayOutputStream.toByteArray();

                FileOutputStream outStream = new FileOutputStream(newFile);
                outStream.write(bytes);

                inputStream.close();
                outStream.flush();
                outStream.close();

                Log.d("ThisLog", "after: " + newFile.getPath() +
                        " " + newFile.getTotalSpace() + "  " + newFile.length());

            }catch(FileNotFoundException e){
                e.printStackTrace();
                return null;
            }catch(IOException e){
                e.printStackTrace();
                return null;
            }

            Uri uri = Uri.fromFile(newFile);

            return uri;
        }

        return null;
    }


    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Log.v(TAG, "returning false");
        return false;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
