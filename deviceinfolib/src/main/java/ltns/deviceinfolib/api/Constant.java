package ltns.deviceinfolib.api;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description
 * @version
 */

public interface Constant {
    interface Error {
        String PERMISSION_APPLY_REFUSED = "有一个或多个该收集器所需的权限被申请驳回";
        String COLLECT_BY_HAND_ERROR = "手动收集时遇到未知错误";
        String UNKNOWN_ERROR = "未知错误";
    }

    interface ScreenInfoValue {
        String MULTI_TOUCH ="multiTouch";
    }

}
