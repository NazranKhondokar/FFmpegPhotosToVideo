package com.nazran.ffmpegphotostovideo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class VideoViewActivity extends AppCompatActivity {

    private static final String TAG = VideoViewActivity.class.getSimpleName();
    private FFmpeg ffmpeg;
    private VideoView videoView;
    private TextView createTV;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        videoView = findViewById(R.id.generatedVideo);
        createTV = findViewById(R.id.createTV);
        progressDialog = new ProgressDialog(VideoViewActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        loadFFMpegBinary();
        //makeVideo();
        //makeSingleImageVideo();
        makeTextAnimationVideo();

        createTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VideoViewActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    /**
     * Load FFmpeg binary
     */
    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : era nulo");
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d(TAG, "EXception no controlada : " + e);
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(VideoViewActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoViewActivity.this.finish();
                    }
                })
                .create()
                .show();

    }

    /**
     * Command for making video from images
     */
    private void makeVideo() {
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

/*        String filePrefix = "extract_picture";
        String fileExtn = ".jpg";
        //File dir = new File(picDir, "Images");
        File src = new File(picDir, filePrefix + "%03d" + fileExtn);
        Log.e(TAG, src.getAbsolutePath());*/

        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String fileOutputPrefix = "output_video";
        String fileOutputExtn = ".mp4";
        File dest = new File(moviesDir, fileOutputPrefix + fileOutputExtn);
/*        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, fileOutputPrefix + fileNo + fileOutputExtn);
        }*/
/*
* For many images
* */
        String command[] = {"-y", "-r", "1/2", "-i", picDir + "/video_photo%03d.PNG",
                "-c:v", "libx264", "-vf", "fps=25", "-pix_fmt", "yuv420p", dest.getAbsolutePath()};

        /*
* For single image
* */
/*        String command[] = {"-loop", "1", "-i", images.get(12),
                "-c:v", "libx264", "-t", "3", "-pix_fmt", "yuv420p", dest.getAbsolutePath()};*/
        execFFmpegBinary(command);
    }

    private void makeSingleImageVideo() {
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String fileOutputPrefix = "input_video";
        String fileOutputExtn = ".mp4";
        File dest = new File(picDir, fileOutputPrefix + fileOutputExtn);

        String command[] = {"-loop", "1", "-i", picDir + "/video_photo001.PNG",
                "-c:v", "libx264", "-t", "4", "-pix_fmt", "yuv420p", dest.getAbsolutePath()};
        execFFmpegBinary(command);
    }

    private void makeTextAnimationVideo() {
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String fileOutputPrefix = "anim_video";
        String fileOutputExtn = ".mp4";
        File dest = new File(picDir, fileOutputPrefix + fileOutputExtn);

        //left to right
        //String command[] = {"-y", "-i", picDir + "/output_video.mp4", "-vf", "drawtext=fontfile=/system/fonts/DroidSerif-Regular.ttf:fontsize=40:fontcolor=white:x=h-350*t:y=700:text='START BEFORE YOU'RE READY'", "-t", "2", dest.getAbsolutePath()};

        //String command[] = {"-i", picDir + "/output_video.mp4", "-filter_complex", "[0:v]drawtext=fontfile=/system/fonts/DroidSerif-Regular.ttf:text='WELCOME':fontsize=60:fontcolor=ffffff:alpha='if(lt(t,1),0,if(lt(t,2),(t-1)/1,if(lt(t,3),1,if(lt(t,4),(1-(t-3))/1,0))))':x=(w-text_w)/2:y=(h-text_h)/2", dest.getAbsolutePath()};

        String command[] = {"-y",
                "-loop", "1", "-i", picDir + "/video_photo001.PNG",
                "-loop", "1", "-i", picDir + "/video_photo002.PNG",
                "-loop", "1", "-i", picDir + "/video_photo003.PNG",
                "-filter_complex",
                "[0:v]trim=duration=3,drawtext=fontfile=/system/fonts/DroidSerif-Regular.ttf:text='WELCOME':fontsize=60:fontcolor=ffffff:alpha='if(lt(t,1),0,if(lt(t,2),(t-1)/1,if(lt(t,3),1,if(lt(t,4),(1-(t-3))/1,0))))':x=225:y=60," +
                        "drawtext=text='START BEFORE YOU ARE READY':fontfile=/system/fonts/DroidSerif-Regular.ttf:fontsize=40:fontcolor=ffffff:x=w-225*t:y=700," +
                        "drawtext=fontfile=/system/fonts/DroidSerif-Regular.ttf:text='RISE':fontsize=60:fontcolor=ffffff:alpha='if(lt(t,1),0,if(lt(t,2),(t-1)/1,if(lt(t,3),1,if(lt(t,4),(1-(t-3))/1,0))))':x=400:y=340," +
                        "drawtext=fontfile=/system/fonts/DroidSerif-Regular.ttf:text='TOGETHER':fontsize=60:fontcolor=ffffff:alpha='if(lt(t,1),0,if(lt(t,2),(t-1)/1,if(lt(t,3),1,if(lt(t,4),(1-(t-3))/1,0))))':x=400:y=420,setsar=1/1[v0];" +
                        "[1:v]trim=duration=3,drawtext=text='T H E W O R L D I S':fontfile=/system/fonts/DroidSerif-Regular.ttf:fontsize=40:fontcolor=blue:x=250:y=100+t*30-100," +
                        "drawtext=text='beautiful':fontfile=/system/fonts/DroidSerif-Regular.ttf:fontsize=40:fontcolor=ffffff:x=250-20*t:y=150," +
                        "drawtext=fontfile=/system/fonts/DroidSerif-Regular.ttf:text='SAVE THE PLANET':fontsize=60:fontcolor=ffffff:alpha='if(lt(t,1),0,if(lt(t,2),(t-1)/1,if(lt(t,3),1,if(lt(t,4),(1-(t-3))/1,0))))':x=150:y=700,setsar=1/1[v1];" +
                        "[2:v]trim=duration=3,fade=t=in:st=0:d=1,fade=t=out:st=2:d=1,setsar=1/1[v2];" +
                        "[v0][v1][v2]concat=n=3:v=1:a=0,setsar=1/1[v]", "-map", "[v]", "-aspect", "1:1", "-r", "24", "-pix_fmt", "yuv420p", dest.getAbsolutePath()};
        execFFmpegBinary(command);
    }

    /**
     * Command execution for making video
     */
    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.e(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);

                    Log.d(TAG, "progress : " + s);
                    progressDialog.setMessage(s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                    playVideo();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void playVideo() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/output_video.mp4";
        File file = new File(path);
        if (file.exists()) {
            Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/output_video.mp4");
            videoView.stopPlayback();
            videoView.setVideoURI(uri);
            videoView.start();
        }
    }
}
