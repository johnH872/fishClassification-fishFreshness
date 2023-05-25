package hcmute.edu.project_06_fishclassification.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import hcmute.edu.project_06_fishclassification.HelperDialog;
import hcmute.edu.project_06_fishclassification.R;
import hcmute.edu.project_06_fishclassification.ml.Model;
import hcmute.edu.project_06_fishclassification.ml.ModelFreshNonfresh;
import hcmute.edu.project_06_fishclassification.staticVal.staVal;

public class HomeFragment extends Fragment implements View.OnClickListener {
    FirebaseAuth mAuth;
    Button btnCamera, btnGallery, btnSave, btnHelpIcon;
    TextView tvNameDetected, tvLink, tvFreshOrNon;
    CardView cardViewFish, cardViewEyes;
    ImageView fishImage, eyeImage;
    Bitmap fishBitmap, eyeBitMap;
    String fishNameLabels[] = new String[31];
    String fishLinksLabels[] = new String[31];
    String fishFreshOrNonLabels[] = new String[2];
    boolean hasEye = false;
    boolean isFish = false;
    CascadeClassifier cascadeClassifier;
    private Mat rgba, grayscaleImage;
    private int absoluteEyeSize = 0, mDetectorType = 0;
    private float mRelativeEyeSize = 0.2f;
    private static final Scalar EYE_RECT_COLOR = new Scalar(0, 255, 0, 255);


    private Uri filePath; //local file path

    // instance for firebase storage and StorageReference
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageReference;
    String fileName;
    String wikiLink;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    rgba = new Mat();
                    grayscaleImage = new Mat();
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.cascade);
            File cascadeDir = getContext().getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnCamera = view.findViewById(R.id.btn_camera);
        btnGallery = view.findViewById(R.id.btn_gallery);
        btnSave = view.findViewById(R.id.btn_save);
        btnHelpIcon = view.findViewById(R.id.helpIcon);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnHelpIcon.setOnClickListener(this);

        tvNameDetected = view.findViewById(R.id.tv_nameDetected);
        tvLink = view.findViewById(R.id.tv_link);
        tvFreshOrNon = view.findViewById(R.id.tv_freshOrNonFish);
        cardViewFish = view.findViewById(R.id.card_view_fish);
        cardViewEyes = view.findViewById(R.id.card_view_fisheye);
        fishImage = view.findViewById(R.id.fish_img);
        eyeImage = view.findViewById(R.id.fisheye_img);

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getContext(), mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //get fish name labels
        fishNameLabels = labelCreator("fishName_VN.txt",31);
        //get fish links label
        fishLinksLabels = labelCreator("fish_link.txt", 31);
        //get fish fresh or non label
        fishFreshOrNonLabels = labelCreator("classes_fresh_nonfresh.txt",2);

        //Get image from activity
        try {
            byte[] byteArray = getArguments().getByteArray("fishImageFromMainActivity");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            fishBitmap = bmp;
            fishImage.setImageBitmap(Bitmap.createScaledBitmap(fishBitmap,360, 200, false));
            predictFish();
            if (!isFish) {
                tvFreshOrNon.setText("Đây không là cá");
                eyeImage.setImageResource(R.drawable.clown_fish_eye);
                return;
            }
            detectFishEye();
        } catch (NullPointerException ex) {
            Log.e("No arg","No image");
        }

    }

    public String[] labelCreator(String fileName, int maxIndex) {
        String labelNames[] = new String[maxIndex];
        int cnt = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getContext().getAssets().open(fileName)));
            String line = bufferedReader.readLine();
            while (line!=null) {
                labelNames[cnt] = line;
                cnt++;
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return labelNames;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                Intent intentG = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureImageLauncher.launch(intentG);
                break;
            case R.id.btn_gallery:
                Intent intentC = new Intent();
                intentC.setAction(Intent.ACTION_GET_CONTENT);
                intentC.setType("image/*");
                getImageLauncher.launch(intentC);
                break;
            case R.id.btn_save:
                UploadImg();
                break;
            case R.id.helpIcon:
                HelperDialog helperDialog = new HelperDialog(getContext());
                helperDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
                helperDialog.show();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    private void UploadImg() {
        if (filePath != null) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "saved_images/"+fileName+".jpg");
            // adding listeners on upload
            // or failure of image

            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    // Image uploaded successfully
                                    Toast.makeText(getContext(),
                                            "Image Uploaded!!",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // Error, Image not uploaded
                            Toast
                                    .makeText(getContext(),
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // Progress Listener for loading
                        // percentage on the dialog box
                        @Override
                        public void onProgress(
                                UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress
                                    = (100.0
                                    * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(
                                    "Uploaded "
                                            + (int)progress + "%");
                            if((int) progress == 100){
                                progressDialog.cancel();
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        createDocInDB(uri);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private void createDocInDB(Uri uri) {
        Map<String, Object> saveImg = new HashMap<>();
        saveImg.put("imageUrl", uri);
        saveImg.put("name", fileName);
        saveImg.put("relatedLinkUrl", wikiLink);
        saveImg.put("saveDate", new Date(System.currentTimeMillis()));
        saveImg.put("userId", staVal.cur_UDI);

        db.collection("saved_images").document(UUID.randomUUID().toString())
                .set(saveImg)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Adding doc success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error writing document", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Predict fish images
    private void predictFish() {
        try {
            Model model = Model.newInstance(getContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
//            fishBitmap = Bitmap.createScaledBitmap(fishBitmap, 224, 224, true);
//            inputFeature0.loadBuffer(TensorImage.fromBitmap(fishBitmap).getBuffer(), new int[]{1, 224, 224, 3});
            Bitmap input=Bitmap.createScaledBitmap(fishBitmap,224,224,true);
            TensorImage image=new TensorImage(DataType.FLOAT32);
            image.load(input);
            ByteBuffer byteBuffer=image.getBuffer();
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            if (getAccuracy(outputFeature0.getFloatArray()) < 0.65) {
                tvNameDetected.setText("Đây có thể không phải cá !!!");
                tvLink.setText("https://google.com");
                isFish = false;
            } else {
                tvNameDetected.setText(fishNameLabels[getMax(outputFeature0.getFloatArray())] + " ");
                fileName = fishNameLabels[getMax(outputFeature0.getFloatArray())];
                tvLink.setText(fishLinksLabels[getMax(outputFeature0.getFloatArray())] + " ");
                wikiLink = fishLinksLabels[getMax(outputFeature0.getFloatArray())];
                isFish = true;
            }
            // Releases model resources if no longer used.
            Log.e("Accuracy", String.valueOf(getAccuracy(outputFeature0.getFloatArray())));
            model.close();
        } catch (IOException e) {
            Log.e("Exception",e.getMessage());
        }
    }

    private void detectFreshNonFresh() {
        try {
            ModelFreshNonfresh model = ModelFreshNonfresh.newInstance(getContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 50, 50, 3}, DataType.FLOAT32);
            Bitmap input=Bitmap.createScaledBitmap(eyeBitMap,50,50,true);
            TensorImage image=new TensorImage(DataType.FLOAT32);
            image.load(input);
            ByteBuffer byteBuffer=image.getBuffer();
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelFreshNonfresh.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            tvFreshOrNon.setText("Cá này " + fishFreshOrNonLabels[getMax(outputFeature0.getFloatArray())] + " ");
            Log.e("Mắt",fishFreshOrNonLabels[getMax(outputFeature0.getFloatArray())] + " ");
            // Releases model resources if no longer used
            model.close();
        } catch (IOException e) {
            Log.e("Exception",e.getMessage());
        }
    }

    private void detectFishEye() {
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading, please wait...");
        dialog.show();

        grayscaleImage = new Mat(fishBitmap.getHeight(), fishBitmap.getWidth(), CvType.CV_8UC4); // grayImage same size as image view
        rgba = new Mat(fishImage.getHeight(), fishImage.getWidth(), CvType.CV_8UC4); // rgbaImage same size as image view
        Utils.bitmapToMat(fishBitmap, rgba); // Convert bitmap to Mat
        absoluteEyeSize = (int) (fishBitmap.getHeight() * 0.2); // set the face size
        Utils.matToBitmap(drawRect(rgba), fishBitmap);// Convert the Mat with the triangle to bitmap

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!hasEye) {
                    tvFreshOrNon.setText("Không tìm thấy mắt");
                    eyeImage.setImageResource(R.drawable.clown_fish_eye);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    return;
                }
                else detectFreshNonFresh();
                hasEye = false;
                Bitmap pass = null;
                //Convert to byte array
                pass = fishBitmap;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                pass.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byteArray = stream.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                fishImage.setImageBitmap(Bitmap.createScaledBitmap(bmp,360, 200, false));
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, 1000);
    }

    private void cropEyeImage(Mat mat, Rect rect) {
        Mat croppedMat = new Mat(mat, rect);
        Bitmap croppedImage = Bitmap.createBitmap(croppedMat.cols(), croppedMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(croppedMat, croppedImage);
        eyeImage.setImageBitmap(Bitmap.createScaledBitmap(croppedImage,100, 100, false));
        eyeBitMap = croppedImage;
    }

    public Mat drawRect(Mat rgba) {
        Imgproc.cvtColor(rgba, grayscaleImage, Imgproc.COLOR_RGBA2RGB); // Convert colors (not really sure how and why it's here)
        if (absoluteEyeSize == 0) {
            int height = grayscaleImage.rows();
            if (Math.round(height * mRelativeEyeSize) > 0) {
                absoluteEyeSize = Math.round(height * mRelativeEyeSize);
            }
        }
        if (mDetectorType == 0) {
            if (cascadeClassifier != null) {
                MatOfRect eyeDetection = new MatOfRect();
                cascadeClassifier.detectMultiScale(rgba, eyeDetection, 1.1, 7, 2);
                for (Rect rect : eyeDetection.toArray()) {
                    hasEye = true;
                    cropEyeImage(rgba,rect);
                }
            }
        }
        return rgba;
    }

    private int getMax(float[] arr) {
        int max = 0;
        for (int i = 0; i<arr.length;i++)
            if (arr[i]>arr[max]) max=i;
        return max;
    }

    private float getAccuracy(float[] arr) {
        float max = Float.MIN_VALUE;

        for (float value : arr) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    ActivityResultLauncher getImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        filePath = uri;
                        try {
                            fishBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),uri);
                            fishImage.setImageBitmap(Bitmap.createScaledBitmap(fishBitmap,360, 200, false));
                            predictFish();
                            if (!isFish) {
                                tvFreshOrNon.setText("Đây không là cá");
                                eyeImage.setImageResource(R.drawable.clown_fish_eye);
                                return;
                            }
                            detectFishEye();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

    ActivityResultLauncher captureImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        filePath = uri;
                        fishBitmap = (Bitmap) data.getExtras().get("data");
                        fishImage.setImageBitmap(Bitmap.createScaledBitmap(fishBitmap,360, 200, false));
                        predictFish();
                        if (!isFish) {
                            tvFreshOrNon.setText("Đây không là cá");
                            eyeImage.setImageResource(R.drawable.clown_fish_eye);
                            return;
                        }
                        detectFishEye();
                    }
                }
            });
}