package ltns.deviceinfolib.collector;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/26.
 * Email: gu.yuepeng@foxmail.com
 */


public class CameraInfoCollector extends BaseDeviceInfoCollector {
    private boolean onlyNeedNormalInfo = true;

    /**
     * @param context
     * @param collectorName
     * @param onlyNeedNormalInfo 是否只需要基础信息（若选择true，则只返回摄像头数量和每个摄像头的像素）
     */
    public CameraInfoCollector(Context context, String collectorName, boolean onlyNeedNormalInfo) {
        super(context, collectorName);
        this.onlyNeedNormalInfo = onlyNeedNormalInfo;
    }

    private int cameraCount;
    //详细信息集合
    private List<CameraBean> mCameras = new ArrayList<>();
    //基础信息集合
    private List<CameraBean2> cameras = new ArrayList<>();
    private String permissions[] = {Manifest.permission.CAMERA};

    /**
     * 用Parameters和CameraInfo描述摄像头属性
     */
    class CameraBean {
        /*
        facing:
        CAMERA_FACING_FRONT前置;
        CAMERA_FACING_BACK后置;
        */
        private Camera.CameraInfo cameraInfo;
        private Camera.Parameters parameters;

        public Camera.Parameters getParameters() {
            return parameters;
        }

        public void setParameters(Camera.Parameters parameters) {
            this.parameters = parameters;
        }

        public Camera.CameraInfo getCameraInfo() {
            return cameraInfo;
        }

        public void setCameraInfo(Camera.CameraInfo cameraInfo) {
            this.cameraInfo = cameraInfo;
        }

    }

    /**
     * 基础信息
     */
    class CameraBean2 {
        //支持的预览状态的图片像素
        private List<Camera.Size> supportedPreviewSizes;
        //支持的实际拍摄的图片像素
        private List<Camera.Size> supportedPictureSizes;
        private Camera.CameraInfo cameraInfo;


        public List<Camera.Size> getSupportedPreviewSizes() {
            return supportedPreviewSizes;
        }

        public void setSupportedPreviewSizes(List<Camera.Size> supportedPreviewSizes) {
            this.supportedPreviewSizes = supportedPreviewSizes;
        }

        public List<Camera.Size> getSupportedPictureSizes() {
            return supportedPictureSizes;
        }

        public void setSupportedPictureSizes(List<Camera.Size> supportedPictureSizes) {
            this.supportedPictureSizes = supportedPictureSizes;
        }

        public Camera.CameraInfo getCameraInfo() {
            return cameraInfo;
        }

        public void setCameraInfo(Camera.CameraInfo cameraInfo) {
            this.cameraInfo = cameraInfo;
        }

    }

    @Override
    public boolean needCollectManually() {
        return false;
    }

    @Override
    public String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected void doCollectAutomatically() {
        cameraCount = Camera.getNumberOfCameras();
        CameraBean mCamera;
        CameraBean2 camera;
        Camera.CameraInfo cameraInfo;
        for (int i = 0; i < cameraCount; i++) {
            Camera c = Camera.open(i);
            cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (onlyNeedNormalInfo) {
                camera = new CameraBean2();
                camera.setSupportedPreviewSizes(c.getParameters().getSupportedPreviewSizes());
                camera.setSupportedPictureSizes(c.getParameters().getSupportedPictureSizes());
                camera.setCameraInfo(cameraInfo);
                cameras.add(camera);
            } else {
                mCamera = new CameraBean();
                mCamera.setCameraInfo(cameraInfo);
                mCamera.setParameters(c.getParameters());
                mCameras.add(mCamera);
            }
            c.stopPreview();
            c.release();
            c = null;
        }
    }

    @Override
    public String getJsonInfo() {
        if (onlyNeedNormalInfo)
            return new Gson().toJson(cameras);
        return new Gson().toJson(mCameras);
    }

    @Override
    protected void doCollectManually() {

    }
}
