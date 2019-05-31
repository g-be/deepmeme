package deepmeme.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class stegoCrypto {

    /*------------------------------------------- this section contains useful functions */
    // Utility function:
    //Taking the square block size constant
    private static final int BLOCK_SIDE_LENGTH = 512;
    private final static String TAG = stegoCrypto.class.getName();

    // Utility:
    // this function calculates the square blocks
    public static int squareBlockNeeded(int pixels) {
        int result;

        int area = BLOCK_SIDE_LENGTH * BLOCK_SIDE_LENGTH;
        int divisor = pixels / (area);
        int remainder = pixels % (area);

        result = divisor + (remainder > 0 ? 1 : 0);

        return result;
    }

    // Utility:
    // this method splits the image into many squares of a calculated size
    // (BLOCK_SIDE_LENGTH * BLOCK_SIDE_LENGTH)
    public static List<Bitmap> splitImage(Bitmap btmp) {

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        ArrayList<Bitmap> chunkedImages = new ArrayList<>();

        // Assume like a matrix in which the element is a Small Square block
        //Rows and columns of that matrix
        int rows = btmp.getHeight() / BLOCK_SIDE_LENGTH;
        int cols = btmp.getWidth() / BLOCK_SIDE_LENGTH;

        int chunk_height_mod = btmp.getHeight() % BLOCK_SIDE_LENGTH;
        int chunk_width_mod = btmp.getWidth() % BLOCK_SIDE_LENGTH;

        if (chunk_height_mod > 0)
            rows++;
        if (chunk_width_mod > 0)
            cols++;


        //x_coordinate and y_coordinate are the pixel positions of the image chunks
        int y_coordinate = 0;

        for (int x = 0; x < rows; x++) {

            int x_coordinate = 0;

            for (int y = 0; y < cols; y++) {

                chunkHeight = BLOCK_SIDE_LENGTH;
                chunkWidth = BLOCK_SIDE_LENGTH;

                if (y == cols - 1 && chunk_width_mod > 0)
                    chunkWidth = chunk_width_mod;

                if (x == rows - 1 && chunk_height_mod > 0)
                    chunkHeight = chunk_height_mod;

                //Adding chunk images to the list
                chunkedImages.add(Bitmap.createBitmap(btmp, x_coordinate, y_coordinate, chunkWidth, chunkHeight));
                x_coordinate += BLOCK_SIDE_LENGTH;

            }

            y_coordinate += BLOCK_SIDE_LENGTH;

        }

        //returning the list
        return chunkedImages;
    }
    // this method will merge the image chunks into a single image
    public static Bitmap mergeImage(List<Bitmap> images, int initial_height, int initial_width) {

        //Calculating number of Rows and columns of that matrix
        int rows = initial_height / BLOCK_SIDE_LENGTH;
        int cols = initial_width / BLOCK_SIDE_LENGTH;

        int chunk_height_mod = initial_height % BLOCK_SIDE_LENGTH;
        int chunk_width_mod = initial_width % BLOCK_SIDE_LENGTH;

        if (chunk_height_mod > 0)
            rows++;
        if (chunk_width_mod > 0)
            cols++;

        //create a bitmap of a size which can hold the complete image after merging
        Log.d(TAG, "Size width " + initial_width + " size height " + initial_height);
        Bitmap bitmap = Bitmap.createBitmap(initial_width, initial_height, Bitmap.Config.ARGB_4444);

        //Creating canvas
        Canvas canvas = new Canvas(bitmap);

        int count = 0;

        for (int irows = 0; irows < rows; irows++) {
            for (int icols = 0; icols < cols; icols++) {

                //Drawing all the chunk images of canvas
                canvas.drawBitmap(images.get(count), (BLOCK_SIDE_LENGTH * icols), (BLOCK_SIDE_LENGTH * irows), new Paint());
                count++;

            }
        }

        //returning bitmap
        return bitmap;
    }
    // converts a byte array into an int array
    public static int[] byteArrayToIntArray(byte[] b) {

        Log.v("Size byte array", b.length + "");

        int size = b.length / 3;

        Log.v("Size Int array", size + "");

        System.runFinalization();
        //Garbage collection
        System.gc();

        Log.v("FreeMemory", Runtime.getRuntime().freeMemory() + "");
        int[] result = new int[size];
        int offset = 0;
        int index = 0;

        while (offset < b.length) {
            result[index++] = byteArrayToInt(b, offset);
            offset = offset + 3;
        }

        return result;
    }

    // byte array to int
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    // convert the byte array to an int starting from offset
    private static int byteArrayToInt(byte[] b, int offset) {
        int value = 0x00000000;

        for (int i = 0; i < 3; i++) {
            int shift = (3 - 1 - i) * 8;
            value |= (b[i + offset] & 0x000000FF) << shift;
        }

        value = value & 0x00FFFFFF;

        return value;
    }

    // convert integer ARGB array values to byte array
    public static byte[] convertArray(int[] array) {

        byte[] newarray = new byte[array.length * 3];

        for (int i = 0; i < array.length; i++) {

            newarray[i * 3] = (byte) ((array[i] >> 16) & 0xFF);
            newarray[i * 3 + 1] = (byte) ((array[i] >> 8) & 0xFF);
            newarray[i * 3 + 2] = (byte) ((array[i]) & 0xFF);

        }

        return newarray;
    }

    // empty check for a given string
    public static boolean isStringEmpty(String str) {
        boolean result = true;

        if (str == null) ;
        else {
            str = str.trim();
            if (str.length() > 0 && !str.equals("undefined"))
                result = false;
        }

        return result;
    }
    /*------------------------------------------- this section contains compressing functions */
    // zip
    public static byte[] compress(String string) throws Exception {

        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();

        byte[] compressed = os.toByteArray();
        os.close();

        return compressed;
    }


    // unzip
    public static String decompress(byte[] compressed) throws Exception {

        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.ISO_8859_1));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        gis.close();
        bis.close();

        return sb.toString();
    }

    /*------------------------------------------- this section contains encryption functions */

    public static String encryptMessage(String message, String secret_key) throws Exception {

        // Creating key and cipher
        SecretKeySpec aesKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        Cipher cipher;

        //AES cipher
        cipher = Cipher.getInstance("AES");

        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] encrypted;

        encrypted = cipher.doFinal(message.getBytes());

        Log.d("crypto", "Encrypted  in crypto (mine): " + Arrays.toString(encrypted) + "string: "
                + android.util.Base64.encodeToString(cipher.doFinal(message.getBytes()), 0));

        Log.d("crypto", "Encrypted  in crypto (theirs): "
                + Arrays.toString(cipher.doFinal(message.getBytes())) + "string : " + new String(encrypted));

        return android.util.Base64.encodeToString(cipher.doFinal(message.getBytes()), 0);
    }

    //Decryption Method
    /*
    @parameter : Encrypted Message {String}, Secret key {String}
    @return : Message {String}
     */
    public static String decryptMessage(String encrypted_message, String secret_key) throws Exception {

        Log.d("Decrypt", "message: + " + encrypted_message);
        // Creating key and cipher
        SecretKeySpec aesKey = new SecretKeySpec(secret_key.getBytes(), "AES");
        Cipher cipher;

        //AES cipher
        cipher = Cipher.getInstance("AES");

        // decrypting the text
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted;
        byte[] decoded;
        decoded = android.util.Base64.decode(encrypted_message.getBytes(), 0);
        decrypted = new String(cipher.doFinal(decoded));

        //returning decrypted text
        return decrypted;
    }
}
