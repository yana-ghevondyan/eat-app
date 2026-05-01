package com.example.yanagh;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.yanagh.databinding.ActivityAnalyzeFoodBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;

public class AnalyzeFoodActivity extends BaseActivity {

    private ActivityAnalyzeFoodBinding binding;
    private Bitmap selectedImageBitmap;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            selectedImageBitmap = imageBitmap;
                            showImagePreview(imageBitmap);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            selectedImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            showImagePreview(selectedImageBitmap);
                        } catch (Exception e) {
                            Toast.makeText(this, R.string.error_load_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyzeFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupClickListeners();

        binding.tvTitle.setText(R.string.analyze_food_title);
        binding.tvInstructions.setText(R.string.analyze_food_instructions);
        binding.btnAnalyze.setText(R.string.btn_analyze_food);
        binding.btnAnalyze.setEnabled(false);
        binding.progressAnalyze.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setTitle(R.string.food_assistant_title);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupClickListeners() {
        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
        binding.btnChoosePhoto.setOnClickListener(v -> openGallery());
        binding.btnAnalyze.setOnClickListener(v -> startFoodAnalysis());
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, R.string.error_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void showImagePreview(Bitmap bitmap) {
        binding.ivPreview.setImageBitmap(bitmap);
        binding.ivPreview.setVisibility(View.VISIBLE);
        binding.ivUploadIcon.setVisibility(View.GONE);

        binding.tvTitle.setText(R.string.analyze_ready_title);
        binding.tvInstructions.setText(R.string.analyze_ready_instructions);
        binding.btnAnalyze.setEnabled(true);
        binding.btnAnalyze.setText(R.string.btn_analyze_food);
    }

    private void startFoodAnalysis() {
        if (selectedImageBitmap == null) {
            Toast.makeText(this, R.string.error_pick_image_first, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnAnalyze.setEnabled(false);
        binding.progressAnalyze.setVisibility(View.VISIBLE);
        binding.btnAnalyze.setText(R.string.analyzing);

        try {
            File cacheFile = new File(getCacheDir(), "food_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(cacheFile);
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            Intent intent = new Intent(this, FoodAnalysisChatActivity.class);
            intent.putExtra(FoodAnalysisChatActivity.EXTRA_IMAGE_PATH, cacheFile.getAbsolutePath());
            startActivity(intent);
            finish();
        } catch (Exception e) {
            binding.btnAnalyze.setEnabled(true);
            binding.progressAnalyze.setVisibility(View.GONE);
            Toast.makeText(this, R.string.error_prepare_image, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Snackbar.make(binding.getRoot(), R.string.permission_camera_required, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
