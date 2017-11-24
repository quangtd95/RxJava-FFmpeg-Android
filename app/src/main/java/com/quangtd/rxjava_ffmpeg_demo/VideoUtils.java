package com.quangtd.rxjava_ffmpeg_demo;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QuangTD on 10/3/2017.
 */

class VideoUtils {
    private final static String TAG = VideoUtils.class.getSimpleName();

    static void loadLibrary(Context context) {
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override public void onFailure() {
                    super.onFailure();
                    Log.e(TAG, "load fail");

                }

                @Override public void onSuccess() {
                    super.onSuccess();
                    Log.d(TAG, "load success");
                }

                @Override public void onStart() {
                    super.onStart();
                }

                @Override public void onFinish() {
                    super.onFinish();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    static void trimMedia(Context context, String inputPath, String outputPath, int start, int end, FFmpegExecuteResponseHandler callBack) {
        String cmd = String.format(Locale.getDefault(), "-y -i \"%s\" -ss %d -t %d -c:v copy -c:a copy \"%s\"",
                inputPath,
                start,
                end - start,
                outputPath);
        executeCommand(context, cmd, callBack);
    }

    static void removeAudioTrackOut(Context context, String inputPath, String outputPath, FFmpegExecuteResponseHandler callBack) {
        String cmd = String.format(Locale.getDefault(), "-y -i %s -c copy -an %s", inputPath, outputPath);
        executeCommand(context, cmd, callBack);
    }

    public enum SPEED {
        X0_25,
        X1
    }

    static void changeSpeed(Context context, String inputVideo, String outputVideo, SPEED speed, FfmpegCallBack callBack) {
        String format = "-i \"%s\" -filter_complex \"[0:v]%s[v];[0:a]%s[a]\" -map \"[v]\" -map \"[a]\" -preset ultrafast %s";
        String videoFilter;
        String audioFilter;
        switch (speed) {
            case X0_25:
                videoFilter = "setpts=4.0*PTS";
                audioFilter = "atempo=0.5,atempo=0.5";
                break;
            case X1:
                callBack.onSuccess("Nothing to do!");
                return;
            default:
                callBack.onSuccess("SpeedIllegalArgumentException");
                return;
        }
        String cmd = String.format(format, inputVideo, videoFilter, audioFilter, outputVideo);
        executeCommand(context, cmd, callBack);
    }

    private static void executeCommand(Context context, String cmd, FFmpegExecuteResponseHandler callBack) {
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        Log.e(TAG, cmd);
        try {
            ffmpeg.execute(buildCommand(cmd), callBack);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private static String[] buildCommand(String cmd) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
        while (m.find()) list.add(m.group(1).replace("\"", ""));
        String[] cmds = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            cmds[i] = list.get(i);
        }

        return cmds;
    }
}