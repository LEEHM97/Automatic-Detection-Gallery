package io.androidapp.gallerysearch.yolov4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.androidapp.gallerysearch.utils.ImageUtils;
import io.androidapp.gallerysearch.yolov4.customview.OverlayView;
import timber.log.Timber;

public class TFHelper {
    public TFHelper(Context context) {
        this.context = context;
        try {
            detector = YoloV4Classifier.create(
                    context.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_IS_QUANTIZED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private float minScore = 0.6f;

    Context context;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.2f;




    public static final int TF_OD_API_INPUT_SIZE = 416;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "ADG.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = false;
    private Integer sensorOrientation = 90;

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    private Bitmap sourceBitmap;
    private Bitmap cropBitmap;

    private void fun1(){
        Handler handler = new Handler();

        new Thread(() -> {
            final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handleResult(cropBitmap, results);
                }
            });
        }).start();
    }

    private void initBox(Context context) {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(context);
//        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {
            detector =
                    YoloV4Classifier.create(
                            context.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            Timber.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            context, "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
//            finish();
        }
    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint);
//                cropToFrameTransform.mapRect(location);
//
//                result.setLocation(location);
//                mappedRecognitions.add(result);
            }
        }
//        tracker.trackResults(mappedRecognitions, new Random().nextInt());
//        trackingOverlay.postInvalidate();


//        imageView.setImageBitmap(bitmap);
    }

    public ArrayList<String> tagging(String imgPath){
        List<Classifier.Recognition> list = detect(imgPath);
        ArrayList<String>  tags = new ArrayList<>();
        if(list != null) {
            for (Classifier.Recognition r : list) {
                if(r.getConfidence() > minScore)
                    tags.add(r.getTitle());
            }
        }
        return tags;
    }

    public List<Classifier.Recognition> detect(String imgPath){
        List<Classifier.Recognition> results = null;
        try {
            File f = new File(imgPath);
            InputStream buf = context.getContentResolver().openInputStream(Uri.fromFile(f));
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            bitmap = Bitmap.createScaledBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true);
            buf.close();
//            final long startTime = SystemClock.uptimeMillis();
//            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
//            Timber.i("소요시간 : %s", lastProcessingTimeMs);
            results = detector.recognizeImage(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
