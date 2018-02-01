package ltns.deviceinfolib.collector;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description 摄像头信息
 * @version
 */


public class CameraInfoCollector extends BaseDeviceInfoCollector {
    @SerializedName("camera_number")
    private static final String CAMERA_COUNT = "cameraCount";
    private static final String CAMERAS = "cameras";
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
     * 用Camera.Parameters类和Camera.CameraInfo类描述摄像头属性
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
        //支持的实际拍摄的图片像素
        private String maxPictureSize = "0*0";
        private Camera.CameraInfo cameraInfo;

        public String getMaxPictureSize() {
            return maxPictureSize;
        }

        public void setMaxPictureSize(String maxPictureSize) {
            this.maxPictureSize = maxPictureSize;
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
                List<Camera.Size> mSize = c.getParameters().getSupportedPictureSizes();
                if (mSize.size() != 0)
                    camera.setMaxPictureSize(mSize.get(0).height + "*" + mSize.get(0).width);
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
        if (onlyNeedNormalInfo) {
            put(CAMERA_COUNT, cameras.size());
            put(CAMERAS, cameras);
        } else {
            put(CAMERA_COUNT, mCameras.size());
            put(CAMERAS, mCameras);
        }
    }


    @Override
    protected void doCollectManually() {

    }
}
