package leon.colorful;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author WeiHongbo
 *
 * @version 0.1
 */
public class ColorfulHelper {
    //
    public static final int DEFAULT_COLORFUL = 0;
    private static boolean DEBUG = true;
    //
    private final SparseArray<ColorfulParameter> mColorfulMap;
    private final SparseArray<Resources> mResourcesMap;
    //
    private static ColorfulHelper mInstance;
    private String mPath;
    private Context mContext;
    private ExecutorService mExecutor;
    private int mColorfulWhich;
    private Handler mHandler;
    private List<ColorfulSwitchListener> mListeners;

    public interface ColorfulSwitchListener {
        void onSwitch();
    }

    public interface ColorfulSwitchProgress {
        void onSuccess(int which);

        void onFailure(int which);
    }

    private ColorfulHelper() {
        mColorfulMap = new SparseArray<ColorfulParameter>();
        mResourcesMap = new SparseArray<Resources>();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public static ColorfulHelper getColorfulHelper() {
        if (mInstance == null) {
            mInstance = new ColorfulHelper();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mPath = StorageUtils.getCacheDirectory(mContext, false).getPath();
        mColorfulMap.put(DEFAULT_COLORFUL, null);
        mResourcesMap.put(DEFAULT_COLORFUL, mContext.getResources());
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void close() {
        mColorfulMap.clear();
        mResourcesMap.clear();
        if (mListeners != null) {
            mListeners.clear();
        }
        mExecutor.shutdownNow();
        mHandler = null;
        mInstance = null;
    }

    public void addColorful(ColorfulParameter parameter) {
        if (parameter != null) {
            mColorfulMap.put(parameter.mColorful, parameter);
        }
    }

    public void addListener(ColorfulSwitchListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<ColorfulSwitchListener>();
        }
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(ColorfulSwitchListener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }


    public void switchColorful(final int which, final ColorfulSwitchProgress progress) {
        mExecutor.execute(new ColorfulLoadRunnable(which, new ColorfulSwitchProgress() {
            @Override
            public void onSuccess(final int which) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mColorfulWhich = which;
                        if (progress != null) {
                            progress.onSuccess(which);
                        }
                        if (mListeners != null) {
                            for (ColorfulSwitchListener listener : mListeners) {
                                listener.onSwitch();
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(final int which) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (progress != null) {
                            progress.onFailure(which);
                        }
                    }
                });
            }
        }));
    }

    public int getWhich() {
        return mColorfulWhich;
    }

    //=========================================================================
    public int getColor(int resId) {
        Resources resources = mResourcesMap.get(mColorfulWhich);
        ColorfulParameter parameter = mColorfulMap.get(mColorfulWhich);
        if (parameter != null && resources != null) {
            int id = resources.getIdentifier(mContext.getResources().getResourceEntryName(resId),
                                             "color", parameter.mPackageName);
            if (id != 0) { // not found resource
                return resources.getColor(id);
            }
        }
        return mContext.getResources().getColor(resId);
    }

    public ColorStateList getColorStateList(int resId) {
        Resources resources = mResourcesMap.get(mColorfulWhich);
        ColorfulParameter parameter = mColorfulMap.get(mColorfulWhich);
        if (parameter != null && resources != null) {
            int id = resources.getIdentifier(mContext.getResources().getResourceEntryName(resId),
                                             "color", parameter.mPackageName);
            if (id != 0) { // not found resource
                return resources.getColorStateList(id);
            }
        }
        return mContext.getResources().getColorStateList(resId);
    }

    public int getColor(String name) {
        Resources resources = mResourcesMap.get(mColorfulWhich);
        ColorfulParameter parameter = mColorfulMap.get(mColorfulWhich);
        if (parameter != null && resources != null) {
            int id = resources.getIdentifier(name, "color", parameter.mPackageName);
            if (id != 0) { // not found resource
                return resources.getColor(id);
            }
        }
        return mContext.getResources().getColor(
                mContext.getResources().getIdentifier(name, "color", mContext.getPackageName()));
    }

    public Drawable getDrawable(int resId) {
        Resources resources = mResourcesMap.get(mColorfulWhich);
        ColorfulParameter parameter = mColorfulMap.get(mColorfulWhich);
        if (parameter != null && resources != null) {
            int id = resources.getIdentifier(mContext.getResources().getResourceEntryName(resId),
                                             "drawable", parameter.mPackageName);
            if (id != 0) { // not found resource
                return resources.getDrawable(id);
            }
        }
        return mContext.getResources().getDrawable(resId);
    }

    public Drawable getDrawable(String name) {
        Resources resources = mResourcesMap.get(mColorfulWhich);
        ColorfulParameter parameter = mColorfulMap.get(mColorfulWhich);
        if (parameter != null && resources != null) {
            int id = resources.getIdentifier(name, "drawable", parameter.mPackageName);
            if (id != 0) { // not found resource
                return resources.getDrawable(id);
            }
        }
        return mContext.getResources().getDrawable(
                mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName()));
    }

    //========================================================================
    public class ColorfulLoadRunnable implements Runnable {
        private final int mWhich;
        private final ColorfulSwitchProgress mProgress;

        private ColorfulLoadRunnable(int which, ColorfulSwitchProgress progress) {
            mWhich = which;
            mProgress = progress;
        }

        @Override
        public void run() {
            Resources resources = mResourcesMap.get(mWhich);
            if (resources == null) {
                ColorfulParameter parameter = mColorfulMap.get(mWhich);
                // load resource
                // step 1: load sdcard
                File file = new File(mPath + File.separator + parameter.mAPKName);

                if (DEBUG) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (file.exists()) {
                    resources = loadResourcesFromAPK(file.getPath());
                    if (resources != null) {
                        mResourcesMap.put(mWhich, resources);
                    }
                }
                // step 2: load assets
                if (resources == null) {
                    boolean isExist = copyApkFromAssets(parameter.mAPKName, file.getPath());
                    if (isExist) {
                        resources = loadResourcesFromAPK(file.getPath());
                        if (resources != null) {
                            mResourcesMap.put(mWhich, resources);
                        }
                    }
                }
                // step 3: load from network
                if (resources == null) {
                    //TODO
                }
            }
            if (mProgress != null) {
                if (resources != null) {
                    mProgress.onSuccess(mWhich);
                } else {
                    mProgress.onFailure(mWhich);
                }
            }
        }

        private Resources loadResourcesFromAPK(String path) {
            Resources resources = null;
            try {
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPath = assetManager.getClass().getMethod("addAssetPath",
                                                                        String.class);
                addAssetPath.invoke(assetManager, path);
                Resources superRes = mContext.getResources();
                resources = new Resources(assetManager, superRes.getDisplayMetrics(),
                                          superRes.getConfiguration());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resources;
        }

        private boolean copyApkFromAssets(String filename, String path) {
            boolean copyIsFinish = false;
            try {
                InputStream is = mContext.getAssets().open(filename);
                File file = new File(path);
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] temp = new byte[1024];
                int i;
                while ((i = is.read(temp)) > 0) {
                    fos.write(temp, 0, i); // 写入到文件
                }
                fos.close();
                is.close();
                copyIsFinish = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return copyIsFinish;
        }
    }

    public static class ColorfulParameter {
        private final int mColorful;
        private final String mPackageName;
        private final String mAPKName;

        public ColorfulParameter(int colorful, String packageName, String APKName) {
            mColorful = colorful;
            mPackageName = packageName;
            mAPKName = APKName;
        }
    }

    // =================================================
    //            System.out.println("id = " + id + " id = " + 0x7f020001);
    //
    //            System.out.println("id = " + Integer.toHexString(id));
    //
    //            name = resources.getResourceName(id);
    //            System.out.println("name = " + name);
    //
    //            name = resources.getResourceName(id - 1);
    //            System.out.println("name = " + name);
    //
    //            name = resources.getResourceName(id + 1);
    //            System.out.println("name = " + name);
    //
    //            name = resources.getResourceName(0x7f020000);
    //            System.out.println("name = " + name);
    //
    //            name = resources.getResourceName(0x7f020001);
    //            System.out.println("name = " + name);
}
