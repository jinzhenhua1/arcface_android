package com.jzh.arcfaceandroid.plugins;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectModel;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.jzh.arcfaceandroid.common.Constants;
import com.jzh.arcfaceandroid.utils.ThreadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.Log;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * <p></p>
 * <p></p>
 *
 * @author jinzhenhua
 * @version 1.0  ,create at:2020/7/23 10:02
 */

@SuppressWarnings("unchecked")
public class ArcfaceCheckImageHandler implements MethodChannel.MethodCallHandler {
    public static final String CHANNEL = "Arcface_Android";

    private FaceEngine faceEngine;
    private int faceEngineCode = -1;
    private final Context context;

    private boolean isInit = true;

    /**
     * 被处理的图片
     */
    private Bitmap mBitmap = null;

    public ArcfaceCheckImageHandler(Activity activity, Context context, BinaryMessenger messenger){
        this.context = context;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "findFaces": {
                process(result,call.argument("path"));
                break;
            }
            case "initEngine": {
                initEngine(result);
                break;
            }
            case "unInitEngine": {
                unInitEngine();
                break;
            }
            case "activeEngine": {
                activeEngine(result);
                break;
            }
        }
    }

    public void process(MethodChannel.Result result, String path) {
        processImage(path,result);
        //图像转化操作和部分引擎调用比较耗时，建议放子线程操作
//        Observable.create(new ObservableOnSubscribe<Object>() {
//            @Override
//            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
//
//                emitter.onComplete();
//            }
//        })
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Object>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(Object o) {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete(){
//                    }
//                });
    }


    /**
     * 主要操作逻辑部分
     */
    public void processImage(String path, MethodChannel.Result result) {
        if(!isInit){
            callResult(result,0,"初始化失败，无法检测！",null);
            return;
        }
        /**
         * 1.准备操作（校验，显示，获取BGR）
         */
        try{
            final InputStream stream = new FileInputStream(new File(path));
            mBitmap = BitmapFactory.decodeStream(stream, null, null);
//                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.faces);
        }catch (IOException e){

        }
        // 图像对齐
        Bitmap bitmap = ArcSoftImageUtil.getAlignedBitmap(mBitmap, true);


        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // bitmap转bgr24
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            callResult(result,0,"",transformCode);
            return;
        }

        List<FaceInfo> faceInfoList = new ArrayList<>();

        /**
         * 2.成功获取到了BGR24 数据，开始人脸检测
         */
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, DetectModel.RGB, faceInfoList);
        if (detectCode == ErrorInfo.MOK) {
            Log.e("ArcfaceCheckImageHandler","人脸检测成功:" + faceInfoList.size());
        }else{
            Log.e("ArcfaceCheckImageHandler","人脸检测失败:" + detectCode);
        }
        mBitmap.recycle();
        /**
         * 3.若检测结果人脸数量大于0，则在bitmap上绘制人脸框并且重新显示到ImageView，若人脸数量为0，则无法进行下一步操作，操作结束
         */
        callResult(result,faceInfoList.size(),null,null);
//        if (faceInfoList.size() > 0) {
//            callResult(result,faceInfoList.size()+"",null,null);
//        } else {
//            callResult(result,faceInfoList.size()+"",null,null);
//        }
    }

    /**
     * 初始化引擎
     */
    private void initEngine(MethodChannel.Result result) {
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                16, 10, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);

        if (faceEngineCode != ErrorInfo.MOK) {
            isInit = false;
            callResult(result,0,"引擎初始化失败",faceEngineCode);
        }else{
            isInit = true;
            callResult(result,0,null,faceEngineCode);
        }
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
        }
    }


    /**
     * 激活引擎
     *
     */
    public void activeEngine(MethodChannel.Result result) {
        Log.e("ArcfaceCheckImageHandler","引擎激活中");
        int activeCode = FaceEngine.activeOnline(context, Constants.APP_ID, Constants.SDK_KEY);
        if (activeCode == ErrorInfo.MOK) {
            isInit = true;
            Log.e("ArcfaceCheckImageHandler","引擎激活成功");
        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            isInit = true;
            Log.e("ArcfaceCheckImageHandler","引擎激活已经激活，无需重复激活");
        } else {
            isInit = false;
            callResult(result,0,"引擎激活失败",activeCode);
            Log.e("ArcfaceCheckImageHandler","引擎激活失败");
            return;
        }

        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        int res = FaceEngine.getActiveFileInfo(context, activeFileInfo);
        if (res == ErrorInfo.MOK) {

        }

        callResult(result,0,null,null);

//        Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> emitter) {
////                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
//                Log.e("ArcfaceCheckImageHandler","引擎激活中");
//                int activeCode = FaceEngine.activeOnline(context, Constants.APP_ID, Constants.SDK_KEY);
//                emitter.onNext(activeCode);
//            }
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Integer>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Integer activeCode) {
//                        if (activeCode == ErrorInfo.MOK) {
//                            isInit = true;
//                            Log.e("ArcfaceCheckImageHandler","引擎激活成功");
//                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            isInit = true;
//                            Log.e("ArcfaceCheckImageHandler","引擎激活已经激活，无需重复激活");
//                        } else {
//                            isInit = false;
//                            callResult(result,0,"引擎激活失败",activeCode);
//                            Log.e("ArcfaceCheckImageHandler","引擎激活失败");
//                        }
//
//                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
//                        int res = FaceEngine.getActiveFileInfo(context, activeFileInfo);
//                        if (res == ErrorInfo.MOK) {
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        callResult(result,0,e.getMessage(),null);
//                        isInit = false;
//                        Log.e("ArcfaceCheckImageHandler","引擎激活报错" + e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        callResult(result,0,null,null);
//                    }
//                });
    }

    private void callResult(MethodChannel.Result result, int count, String error, Object code) {
        HashMap<String, Object> ret = new HashMap<>(16);
        ret.put("count", count);
        ret.put("error", error);
        ret.put("code", code);
        ThreadUtils.runOnUiThread(() -> {
            try{
                result.success(ret);
            }catch (Exception e){
                Log.e("ArcfaceCheckImageHandler","报错了：" + e.getMessage());
            }
        });
    }
}
