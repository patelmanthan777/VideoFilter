package com.lyzirving.test.videofilter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.lyzirving.test.videofilter.util.DeviceUtils;
import com.ox.av.edit.VideoFilterDevice;
import com.ox.gpuimage.GPUImageFilterGroup;
import com.ox.gpuimage.GPUImageOESFilter;
import com.ox.gpuimage.GPUImageScaleFilter;
import com.ox.gpuimage.util.LocationUtil;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextureView mVideoView;
    private FrameLayout mContainer;
    private TextView mBtnPlay;
    private TextView mBtnOriginal;
    private TextView mBtn11;
    private TextView mBtn45;
    private TextView mBtn169;
    private TextView mBtn916;
    private LottieAnimationView mViewAnimation;

    private String mVideoPath;
    private Surface mVideoSurface;
    private int mVideoDegree;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mBitRate;
    private String[] mLocation;
    private MediaPlayer mMediaPlayer;

    private boolean mIsPrepare;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mVideoSurface = new Surface(surface);
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(mVideoPath);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mIsPrepare = true;
                        mMediaPlayer.setSurface(mVideoSurface);
                        mMediaPlayer.start();
                    }
                });
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mMediaPlayer.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mIsPrepare = false;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        boolean granted = checkPermission();
        if (granted) {
            showVideo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults != null) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showVideo();
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying() && mIsPrepare) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnPlay) {
            if (mIsPrepare) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mBtnPlay.setText("play");
                } else {
                    mMediaPlayer.start();
                    mBtnPlay.setText("pause");
                }
            }
        } else if (v == mBtn11) {
            if (mIsPrepare) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mBtnPlay.setText("play");
                }
                mViewAnimation.setVisibility(View.VISIBLE);
                mViewAnimation.playAnimation();
                saveVideoAtSpecificRatio(GPUImageScaleFilter.ResizeType.Type.TYPE_1_1);
            }
        } else if (v == mBtnOriginal) {
            if (mIsPrepare) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mBtnPlay.setText("play");
                }
                mViewAnimation.setVisibility(View.VISIBLE);
                mViewAnimation.playAnimation();
                saveOriginal();
            }
        } else if (v == mBtn45) {
            if (mIsPrepare) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mBtnPlay.setText("play");
                }
                mViewAnimation.setVisibility(View.VISIBLE);
                mViewAnimation.playAnimation();
                saveVideoAtSpecificRatio(GPUImageScaleFilter.ResizeType.Type.TYPE_4_5);
            }
        } else if (v == mBtn169) {
            if (mIsPrepare) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mBtnPlay.setText("play");
                }
                mViewAnimation.setVisibility(View.VISIBLE);
                mViewAnimation.playAnimation();
                saveVideoAtSpecificRatio(GPUImageScaleFilter.ResizeType.Type.TYPE_16_9);
            }
        } else if (v == mBtn916) {
            if (mIsPrepare) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mBtnPlay.setText("play");
                }
                mViewAnimation.setVisibility(View.VISIBLE);
                mViewAnimation.playAnimation();
                saveVideoAtSpecificRatio(GPUImageScaleFilter.ResizeType.Type.TYPE_9_16);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseVideo();
    }

    private void initView() {
        mContainer = findViewById(R.id.container);
        mBtnPlay = findViewById(R.id.btn_play);
        mBtnOriginal = findViewById(R.id.btn_original);
        mBtn11 = findViewById(R.id.btn_11);
        mBtn45 = findViewById(R.id.btn_45);
        mBtn169 = findViewById(R.id.btn_169);
        mBtn916 = findViewById(R.id.btn_916);
        mViewAnimation = findViewById(R.id.view_animation);

        mBtnPlay.setOnClickListener(this);
        mBtnOriginal.setOnClickListener(this);
        mBtn11.setOnClickListener(this);
        mBtn45.setOnClickListener(this);
        mBtn169.setOnClickListener(this);
        mBtn916.setOnClickListener(this);
    }

    private void saveVideoWithFilter(String inputPath, String outputPath, GPUImageFilterGroup filterGroup,
                                     int outputWidth, int outputHeight, int videoDegree, int bitRate,
                                     String[] location, boolean resize) {
        try {
            VideoFilterDevice device = new VideoFilterDevice(new File(inputPath), new File(outputPath), filterGroup,
                    outputWidth, outputHeight, videoDegree, bitRate, location, resize, new VideoFilterDevice.ResultCallback() {
                @Override
                public void onError() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mViewAnimation.pauseAnimation();
                            mViewAnimation.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "error occurs when saving", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onSuccess(File outputFile) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mViewAnimation.pauseAnimation();
                            mViewAnimation.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "save successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            device.start();
        } catch (IOException e) {
            e.printStackTrace();
            mViewAnimation.pauseAnimation();
            mViewAnimation.setVisibility(View.GONE);
            Toast.makeText(this, "error occurs", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveVideoAtSpecificRatio(GPUImageScaleFilter.ResizeType.Type ratioType) {
        GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();
        filterGroup.addFilter(new GPUImageOESFilter());
        GPUImageScaleFilter scaleFilter = new GPUImageScaleFilter();
        scaleFilter.setResize(ratioType, mVideoWidth, mVideoHeight);
        filterGroup.addFilter(scaleFilter);

        boolean resize = true;
        String outputPath = Environment.getExternalStorageDirectory() + File.separator + "TestResource" + File.separator + getVideoName(ratioType);
        int[] outputSize = getOutputSize(ratioType, mVideoWidth, mVideoHeight);

        saveVideoWithFilter(mVideoPath, outputPath, filterGroup, outputSize[0], outputSize[1], mVideoDegree,
                mBitRate, mLocation, resize);
    }

    private String getVideoName(GPUImageScaleFilter.ResizeType.Type ratioType) {
        StringBuilder sb = new StringBuilder("video_");
        if (mVideoWidth >= mVideoHeight) {
            sb.append("landscape_");
        } else {
            sb.append("portrait_");
        }
        switch (ratioType) {
            case TYPE_1_1:
                sb.append("1_1.mp4");
                break;
            case TYPE_4_5:
                sb.append("4_5.mp4");
                break;
            case TYPE_16_9:
                sb.append("16_9.mp4");
                break;
            case TYPE_9_16:
                sb.append("9_16.mp4");
                break;
        }
        return sb.toString();
    }

    private int[] getOutputSize(GPUImageScaleFilter.ResizeType.Type ratioType, int width, int height) {
        int[] result = new int[2];
        int tmp = 0;
        switch (ratioType) {
            case TYPE_1_1:
                tmp = Math.min(width, height);
                result[0] = tmp;
                result[1] = tmp;
                break;
            case TYPE_4_5:
                tmp = Math.min(width, height);
                result[0] = tmp;
                result[1] = (int) (tmp * 1f / 4f * 5f);
                break;
            case TYPE_16_9:
                tmp = Math.max(width, height);
                result[0] = tmp;
                result[1] = (int) (tmp * 1f / 16f * 9f);
                break;
            case TYPE_9_16:
                tmp = Math.min(width, height);
                result[0] = tmp;
                result[1] = (int) (tmp * 1f / 9f * 16f);
                break;
        }
        if (result[0] % 2 != 0) {
            result[0]++;
        }
        if (result[1] % 2 != 0) {
            result[1]++;
        }
        return result;
    }

    private void saveOriginal() {
        GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();
        filterGroup.addFilter(new GPUImageOESFilter());

        boolean resize = false;
        String outputPath = Environment.getExternalStorageDirectory() + File.separator + "TestResource" + File.separator + "video_original.mp4";

        saveVideoWithFilter(mVideoPath, outputPath, filterGroup, mVideoWidth, mVideoHeight, mVideoDegree,
                mBitRate, mLocation, resize);
    }

    private void showVideo() {
        retrievMetaData();
        addVideoViewAndAdjustSize();
    }

    private void retrievMetaData() {
        mVideoPath = Environment.getExternalStorageDirectory() + File.separator + "TestResource" + File.separator + "video_portrait.mp4";
        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
        retriver.setDataSource(mVideoPath);
        String degreesString = retriver.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (degreesString != null) {
            mVideoDegree = Integer.valueOf(degreesString);
        }
        String bitrateString = retriver.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_BITRATE);
        mBitRate = 4000000;
        if (bitrateString != null) {
            mBitRate = Integer.valueOf(bitrateString);
        }
        String widthString = retriver.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        mVideoWidth = Integer.valueOf(widthString);
        String heightString = retriver.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        mVideoHeight = Integer.valueOf(heightString);
        String locationString = retriver.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_LOCATION);
        mLocation = LocationUtil.parseLocation(locationString);
        retriver.release();

        if (mVideoDegree == 90 || mVideoDegree == 270) {
            int tmp = mVideoWidth;
            mVideoWidth = mVideoHeight;
            mVideoHeight = tmp;
        }
    }

    private void addVideoViewAndAdjustSize() {
        int containerHeight = (int) (DeviceUtils.getScreenHeightPx() * 2f / 3);
        int containerWidth = DeviceUtils.getScreenWidthPx();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mContainer.getLayoutParams();
        lp.height = containerHeight;
        mContainer.setLayoutParams(lp);

        mVideoView = new TextureView(this);
        mVideoView.setSurfaceTextureListener(mSurfaceTextureListener);

        FrameLayout.LayoutParams childLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childLp.gravity = Gravity.CENTER;
        float ratio = mVideoWidth * 1f / mVideoHeight;
        if (ratio >= 1) {
            childLp.width = containerWidth;
            childLp.height = (int) (containerWidth * 1f / ratio);
        } else {
            childLp.height = containerHeight;
            childLp.width = (int) (containerHeight * ratio);
        }
        mContainer.addView(mVideoView, 0, childLp);
    }

    private void releaseVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private boolean checkPermission() {
        int grant = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        return grant == PackageManager.PERMISSION_GRANTED;
    }

}