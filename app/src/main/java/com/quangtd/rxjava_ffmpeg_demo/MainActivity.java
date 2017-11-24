package com.quangtd.rxjava_ffmpeg_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.schedulers.IoScheduler;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoUtils.loadLibrary(this);
        List<Observable<String>> observables = new ArrayList<>();
        String inputTrim = "/storage/emulated/0/DCIM/Camera/aha.mp4";
        String outputTrim = "/storage/emulated/0/DCIM/Camera/ahatrim.mp4";
        String outputMute = "/storage/emulated/0/DCIM/Camera/ahamute.mp4";
        String outputSpeed = "/storage/emulated/0/DCIM/Camera/ahaspeed.mp4";
        String outputFinal = "/storage/emulated/0/DCIM/Camera/final.mp4";
        observables.add(trimVideo(inputTrim, outputTrim));
        observables.add(speedVideo(outputTrim, outputSpeed, VideoUtils.SPEED.X0_25));
        observables.add(removeAudio(outputSpeed, outputMute));
        observables.add(trimVideo(outputMute, outputFinal));
        Observable.concat(observables)
                .subscribeOn(new IoScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showProgress);
    }

    private void showProgress(String message) {
        Log.e("TAGG", message);
    }

    public Observable<String> speedVideo(String input, final String output, VideoUtils.SPEED speed) {
        final PublishSubject<String> subject = PublishSubject.create();
        VideoUtils.changeSpeed(this, input, output, speed, new FfmpegCallBack() {

            @Override public void onProgress(String message) {
                super.onProgress(message);
                subject.onNext(message);
            }

            @Override public void onFailure(String message) {
                super.onFailure(message);
                subject.onError(new Throwable(message));
            }

            @Override public void onFinish() {
                super.onFinish();
                Log.e("xxx", "xong speed video -> " + output);
                subject.onComplete();
            }
        });
        return subject;
    }

    public Observable<String> trimVideo(String input, final String output) {
        final PublishSubject<String> subject = PublishSubject.create();
        VideoUtils.trimMedia(this, input, output, 0, 13, new FfmpegCallBack() {
            @Override public void onProgress(String message) {
                super.onProgress(message);
                subject.onNext(message);
            }

            @Override public void onFinish() {
                super.onFinish();
                subject.onComplete();
                Log.e("xxx", "xong trimvideo->" + output);
            }

            @Override public void onFailure(String message) {
                super.onFailure(message);
                subject.onError(new Throwable(message));
            }
        });
        return subject;
    }

    public Observable<String> removeAudio(String input, final String output) {
        final PublishSubject<String> subject = PublishSubject.create();
        VideoUtils.removeAudioTrackOut(this, input, output, new FfmpegCallBack() {
            @Override public void onProgress(String message) {
                super.onProgress(message);
                subject.onNext(message);
            }

            @Override public void onFinish() {
                super.onFinish();
                subject.onComplete();
                Log.e("xxx", "xong remove audio->" + output);
            }

            @Override public void onFailure(String message) {
                super.onFailure(message);
                subject.onError(new Throwable(message));
            }
        });
        return subject;
    }
}
