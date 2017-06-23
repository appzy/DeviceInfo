# DeviceInfo


## 库介绍

### 功能

采集Android设备信息，以Json形式输出

目前包括Sim卡信息（支持对多张Sim卡的信息采集）、主板、CPU等各种Android设备上的软硬件信息（可拓展）

### 特性

- 通过`DeviceInfoManager`管理每个设备信息采集器（下简称：Collector），可自由添加Collector，以同时采集N多种软硬件设备信息
- Collector分为自动采集和手动采集两种采集方式。
	- 自动采集：Manager控制自发进行的采集
	- 手动采集：需要用户参与交互的数据采集过程
- Manager管理的多个Collector做并发自动采集，手动采集可以配置在自动采集动作结束后自行开始
- 每个Collector独立管理各自所需要的权限，在Manager中统一申请（SDK_VERSION >= 23）
- 继承`BaseDeviceInfoCollector`类以拓展可采集的设备信息
- 可选择获取所有的模块的设备信息（Json），也可以选择只输出单一模块
- 提供丰富的状态回调接口`DeviceInfoCollectListener`

## 输出示例

- 默认输出：

```
{
    "board": {"board_name": "MSM8939"},
    "sim": [{
        "dataState": "0",
        "imsi": "460036820263837",
        "isNetworkRoaming": "false",
        "networkOperatorName": "China Telecom",
        "networkType": "14",
        "phoneType": "2",
        "simCountryIso": "cn",
        "simOperator": "46003",
        "simSerialNumber": "89860315844110607274",
        "simState": "0"
    }]
}
```
- 当然也可以通过重写每个Collector的`public String getJsonInfo();`以自定义json的输出内容

## 目前可获取的设备信息

- Sim卡信息
- Board主板信息

## 如何使用

### 添加依赖库

[![](https://jitpack.io/v/guyuepeng/DeviceInfo.svg)](https://jitpack.io/#guyuepeng/DeviceInfo)

#### Gradle

- Step 1.Add it in your root build.gradle at the end of repositories

	```
		allprojects {
			repositories {
				...
				maven { url 'https://jitpack.io' }
			}
		}
	```
- Step 2.Add the dependency

	```
		dependencies {
	        	compile 'com.github.guyuepeng:DeviceInfo:v1.0'
		}
	```
	
#### Maven

- Step 1.Add it in your root build.gradle at the end of repositories

	```
		<repositories>
			<repository>
			    <id>jitpack.io</id>
			    <url>https://jitpack.io</url>
			</repository>
		</repositories>
	```
- Step 2.Add the dependency

	```
		<dependency>
		    <groupId>com.github.guyuepeng</groupId>
		    <artifactId>DeviceInfo</artifactId>
		    <version>v1.0</version>
		</dependency>

	```

### 使用方法

#### 通过Manager管理多个Collector

为了代码简洁易读，我把它设计成这样：（不知道这样写会不会违背什么设计模式，如果有请issues告诉我，谢谢:D）

```
DeviceInfoManager.NewInstance(this)
                .addCollector(new BoardInfoCollector(MainActivity.this, "board"))
                .addCollector(new SimInfoCollector(MainActivity.this, "sim"))
                .autoStartManualCollection(true)
                .bindListener(mDeviceInfoCollectListener)
                .start();
```
- `addCollector(BaseDeviceInfoCollector)`添加一个新的Collector到Manager中
- `autoStartManualCollection(boolean)`默认是true，自动采集全部完成后立即开启手动采集队列

在DeviceInfoCollectListener中监听采集状态：

- `mDeviceInfoManager.getDeviceJsonInfo();`方法获取到Manager中所有Collector采集到的信息
- `mCollector.getJsonInfo();`方法获取单个Collector中的信息
- `void onStart();`Manager调用start()时回调
- `void onSingleSuccess(BaseDeviceInfoCollector mCollector);`当有某个Collector成功采集到信息后回调
- `void onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo);`当有某个Collector采集信息失败后回调（并不一定没有采集到数据，已经采集到的信息仍会被以Json格式输出）
- `void onAllDone(DeviceInfoManager mDeviceInfoManager);`通过addCollector(...)方法添加到Manager中的所有Collector均完成采集动作后回调，需要说明的是，不一定是全部采集成功了，只是采集动作完成了
- `void onAutoAllDone(DeviceInfoManager mDeviceInfoManager);`当抛开需要手动收集的收集器，其他的收集器全部完成采集动作后回调

> 注意：`onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo)`中也可能存在数据，即`mCollector.getJsonInfo();`也可能会有数据，具体原因可参考源码和下方**注意事项**

```
	private DeviceInfoCollectListener mDeviceInfoCollectListener = new DeviceInfoCollectListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onSingleSuccess(BaseDeviceInfoCollector mCollector) {
        	//mCollector.getJsonInfo();
        }

        @Override
        public void onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo) {
        	//mCollector.getJsonInfo();
        }

        @Override
        public void onAllDone(DeviceInfoManager mDeviceInfoManager) {
        	// mDeviceInfoManager.getDeviceJsonInfo();
        }

        @Override
        public void onAutoAllDone(DeviceInfoManager mDeviceInfoManager) {
        }
    };
```

#### Collector单独跑也是支持的

*（支持但不推荐）*

当然还是推荐使用Manager :D。就是想要自己单独跑也可以，接着~

> 特别说明：Collector单独跑需要调用者自行申请权限，因此方便起见，推荐使用Manager统一管理

以`BoardInfoCollector`为例：

```
BoardInfoCollector mCollector = new BoardInfoCollector(MainActivity.this, "board");
        mCollector.bindObserver(mStateObserver);
        mCollector.startCollectAutomatically();//启动自动采集
        mCollector.startCollectManually();//启动手动采集
```

在`CollectorStateObserver `中监听Collector状态：

- `void onCollectionSuccess(BaseDeviceInfoCollector mCollector);`当手动采集和自动收集均成功完成采集时调用
- `void onCollectionFailure(BaseDeviceInfoCollector mCollector,String mErrorInfo);`
- `void onManualCollectionSuccess(BaseDeviceInfoCollector mCollector,boolean startNext);` 当手动参与的信息收集成功时调用。 **P.s.** 此方法回调时会同时回调onCollectionSuccess()，具体看源码
- `void onManualCollectionFailure(BaseDeviceInfoCollector mCollector,String mErrorInfo,boolean startNext);` 当手动参与的信息收集失败时调用。 **P.s.** 此方法回调时会同时回调onCollectionFailure(...)
- `void onNeedManualCollect(BaseDeviceInfoCollector mCollector);`该采集器需要手动采集时回调，具体回调时间请看源码

```
    private CollectorStateObserver mStateObserver=new CollectorStateObserver() {
        @Override
        public void onCollectionSuccess(BaseDeviceInfoCollector mCollector) {

        }

        @Override
        public void onCollectionFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo) {

        }

        @Override
        public void onManualCollectionSuccess(BaseDeviceInfoCollector mCollector, boolean startNext) {

        }

        @Override
        public void onManualCollectionFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo, boolean startNext) {

        }

        @Override
        public void onNeedManualCollect(BaseDeviceInfoCollector mCollector) {

        }
    };
```

#### 拓展自定义Collector

通过继承`BaseDeviceInfoCollector`抽象类进行功能拓展，以采集更多信息，或输出自定义的Json内容

- `public DemoCollector(Context context, String collectorName)`中的`collectorName`
- `public abstract boolean needCollectManually();`返回自定义Collector是否需要手动收集
- `public abstract String[] getRequiredPermissions();`返回自定义Collector所需要的权限（每个Collector独立管理权限，Manager统一申请）
- `protected abstract void doCollectAutomatically();`Collector做自动采集的方法
- `protected abstract void doCollectManually();`Collector做手动采集的方法，若采用Manager统一管理，需要`needCollectManually()`的返回值为`true`此方法才会被调用

```
package ltns.deviceinfolib.collector;

import android.content.Context;

/**
 * Created by guyuepeng on 2017/6/23.
 * Email: gu.yuepeng@foxmail.com
 */

public class DemoCollector extends BaseDeviceInfoCollector {
    public DemoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    @Override
    public boolean needCollectManually() {
        return false;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    protected void doCollectAutomatically() {

    }

    @Override
    protected void doCollectManually() {

    }
}

```

## 注意事项

- 多个Collector的自动采集过程是并发的，运行在子线程中；手动采集方法运行在主线程中
- Manager的监听回调不一定在主线程，因此若操作涉及线程安全，如更新UI，建议使用`Handler`
- Manager的`boolean isManualCollectionStartAutomatically`默认为true，若要更改需要手动配置`autoStartManualCollection(boolean)`为false。若设置为false，需要调用`startCollectByHand`开启手动采集队列；当然也可以单独开启某个Collector的手动采集
- `onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo)`回调中通过调用`mCollector.getJsonInfo();`方法仍可以获取到`doCollectAutomatically();`方法中采集到的自动收集部分的设备信息
- 库内内置了[AndPermission](https://github.com/yanzhenjie/AndPermission)库以处理权限相关

## 更新日志

- 上传库，提供Sim&Board信息采集支持，更新README（2017.06.23）

## 感谢

- 库内使用了 **严振杰** 的[AndPermission](https://github.com/yanzhenjie/AndPermission)库
- 感谢 *Tencent* 的 **Sven** 和 **Elvis** 的帮助指导

## 扯扯淡

写得过程很奇葩，各种诡异的方式去实现，最后感觉这样做还不错，用起来挺顺手的。不过不知道这Manager用的对不对，略微担心: G

另外如果你写了Collector并愿意分享，欢迎Commit : D
