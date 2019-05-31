package deepmeme.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class textDecoding extends AsyncTask<ImageSteganography, Void, ImageSteganography> {

    //Tag for Log
    private final static String TAG = textDecoding.class.getName();

    private final ImageSteganography result;
    //Callback interface for AsyncTask
    private final TextDecodingCallback textDecodingCallback;
    private ProgressDialog progressDialog;

    public textDecoding(Activity activity, TextDecodingCallback textDecodingCallback) {
        super();
        this.progressDialog = new ProgressDialog(activity);
        this.textDecodingCallback = textDecodingCallback;
        //making result object
        this.result = new ImageSteganography();
    }

    //setting progress dialog if wanted
    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    //pre execution of method
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //setting parameters of progress dialog
        if (progressDialog != null) {
            progressDialog.setMessage("Loading, Please Wait...");
            progressDialog.setTitle("Decoding Message");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }
    }

    @Override
    protected void onPostExecute(ImageSteganography imageSteganography) {
        super.onPostExecute(imageSteganography);

        //dismiss progress dialog
        if (progressDialog != null)
            progressDialog.dismiss();

        //sending result to callback
        textDecodingCallback.onCompleteTextEncoding(result);
    }

    @Override
    protected ImageSteganography doInBackground(ImageSteganography... imageSteganographies) {

        //If it is not already decoded
        if (imageSteganographies.length > 0) {

            ImageSteganography imageSteganography = imageSteganographies[0];

            //getting bitmap image from file
            Bitmap bitmap = imageSteganography.getImage();

            //return null if bitmap is null
//            if (bitmap == null)
//                return null;

            //splitting images
            List<Bitmap> srcEncodedList = stegoCrypto.splitImage(bitmap);

            //decoding encrypted zipped message
            String decoded_message = EncodeDecode.decodeMessage(srcEncodedList);

            Log.d(TAG, "Decoded_Message : " + decoded_message);

            //text decoded = true
            if (!stegoCrypto.isStringEmpty(decoded_message)) {
                result.setDecoded(true);
            }

            //decrypting the encoded message
            String decrypted_message = ImageSteganography.decryptMessage(decoded_message, imageSteganography.getSecret_key());
            Log.d(TAG, "Decrypted message : " + decrypted_message);

            //If decrypted_message is null it means that the secret key is wrong otherwise secret key is right.
            if (!stegoCrypto.isStringEmpty(decrypted_message)) {

                //secret key provided is right
                result.setSecretKeyWrong(false);

                // Set Results

                result.setMessage(decrypted_message);


                //free memory
                for (Bitmap bitm : srcEncodedList)
                    bitm.recycle();

                //Java Garbage Collector
                System.gc();
            }
        }

        return result;
    }
}