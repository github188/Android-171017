package com.mapgis.mmt.config;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.entity.MenuItem;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 九宫格模块和地图工具条以及产品标题等产品相关的配置信息加载类
 *
 * @author Zoro
 */
public class Product {
    public String Title;

    public ArrayList<MenuItem> MapToolBars;
    public ArrayList<MenuItem> MapMoreMenus;

    public ArrayList<MenuItem> MainMenus;

    private static Product instance;

    public static Product getInstance() {
        if (instance == null) {
            instance = new Product();
        }

        return instance;
    }

    public static final String LOAD_FROM_SD_CARD_FAILED = "离线模式下加载SD卡下的产品文档失败，已转为加载内置文档";
    public static final String LOAD_FROM_WEB_FAILED = "在线模式下加载服务端的产品文档失败，已转为加载内置文档";
    public static final String LOAD_FAILED = "产品文档加载失败";
    public static final String LOAD_SUCCESS = "产品文档正常加载成功";

    public String loadByNetState(boolean isOffline) {
        String result;
        try {
            if (isOffline) {
                result = loadFromSDCard();
            } else {
                result = loadFromWeb();

//                //留给管网包用，暂时不删
//                if (result.equals(LOAD_SUCCESS)) {
//                    FileUtil.writeFile(GlobalPathManager.getLocalConfigPath() + GlobalPathManager.PRODUCT_FILE, instance);
//                }
            }
            // 过滤android上未实现的菜单
            if (LOAD_SUCCESS.equals(result)) {
                filterUnsupportMenu();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = LOAD_FAILED;
        }
        return result;
    }

    private void filterUnsupportMenu() {
        filterMainMenu();
        filterMapMoreMenu();
        filterMapToolBars();
    }

    private void filterMapToolBars() {
        if (instance.MapToolBars == null || instance.MapToolBars.size() == 0) {
            return;
        }
        List<MenuItem> delMenus = new ArrayList<>();
        for (MenuItem menu : instance.MapToolBars) {
            if (MapMenuRegistry.getInstance().containMenu(menu.Name)) {
                continue;
            }
            delMenus.add(menu);
        }
        instance.MapToolBars.removeAll(delMenus);
    }

    private void filterMapMoreMenu() {
        if (instance.MapMoreMenus == null || instance.MapMoreMenus.size() == 0) {
            return;
        }
        List<MenuItem> delMenus = new ArrayList<>();
        for (MenuItem menu : instance.MapMoreMenus) {
            if (MapMenuRegistry.getInstance().containMenu(menu.Name)) {
                continue;
            }
            delMenus.add(menu);
        }
        instance.MapMoreMenus.removeAll(delMenus);
    }

    private void filterMainMenu() {
        if (instance.MainMenus == null || instance.MainMenus.size() == 0) {
            return;
        }
        List<MenuItem> delMenus = new ArrayList<>();
        for (MenuItem menu : instance.MainMenus) {
            if (menu.Name.contains("/") || menu.Name.contains(".")) {
                //web不能移除
                continue;
            }
            if (NavigationMenuRegistry.getInstance().containMenu(menu.Name)) {
                continue;
            }
            delMenus.add(menu);
        }
        instance.MainMenus.removeAll(delMenus);
    }


    private String loadFromWeb() throws Exception {
        String menuStr = loadMenuConfigFromCityInterface();

        boolean getMenuFromServiceSuccess = false;
        ResultData<Product> resultData = null;
        if (!TextUtils.isEmpty(menuStr)) {
            resultData = new Gson().fromJson(menuStr, new TypeToken<ResultData<Product>>() {
            }.getType());
            if (resultData != null && resultData.ResultCode > 0 && resultData.DataList.size() > 0) {
                getMenuFromServiceSuccess = true;
            }
        }

        if (getMenuFromServiceSuccess) {
            instance = resultData.getSingleData();
            resolveModuleName(instance);

            return LOAD_SUCCESS;
        }

        String role = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).Role;
        if (TextUtils.isEmpty(role)) {
            return (resultData != null && !TextUtils.isEmpty(resultData.ResultMessage)) ? resultData.ResultMessage : LOAD_FAILED;
        }

        String result = NetUtil.downloadStringResource("Roles/role_" + role + ".json");
        if (TextUtils.isEmpty(result)) {
            return (resultData != null && !TextUtils.isEmpty(resultData.ResultMessage)) ? resultData.ResultMessage : LOAD_FAILED;
        }

        instance = new Gson().fromJson(result, Product.class);
        resolveModuleName(instance);

        return LOAD_SUCCESS;

    }

    private String loadFromSDCard() throws Exception {
        File file = new File(GlobalPathManager.getLocalConfigPath() + GlobalPathManager.PRODUCT_FILE);

        if (file.exists()) {
            loadFromInputStream(new FileInputStream(file));

            return LOAD_SUCCESS;
        } else {
            loadFromAsset();

            return LOAD_FROM_SD_CARD_FAILED;
        }
    }

    private void loadFromAsset() throws IOException {
        loadFromInputStream(MyApplication.getInstance().getAssets().open("cfg/" + GlobalPathManager.PRODUCT_FILE));
    }

    private void loadFromInputStream(InputStream is) throws IOException {
        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(is);

            instance = new Gson().fromJson(reader, Product.class);

            resolveModuleName(instance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 调用CityInterface服务加载地图配置信息
     */
    private String loadMenuConfigFromCityInterface() {
        try {
            //String uri = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetUserMenuList";
            String uri = "http://10.37.147.80/langfang/cityinterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/GetUserMenuList";


            return NetUtil.executeHttpGet(uri, "UserID", MyApplication.getInstance().getUserId() + "");
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 判断是否含有指定的功能模块
     *
     * @param module 模块名称
     * @return true含有，false不含有
     */
    public boolean hasNavFunction(String module) {

        if (MainMenus == null || module == null || module.length() == 0) {
            return false;
        }

        for (MenuItem item : MainMenus) {
            if (item.Name.equals(module)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 导航栏菜单"模块名称"允许配置参数，参数格式为"模块名称?key=value&key1=value1" (Url格式)
     */
    private void resolveModuleName(Product instance) {
        if (instance == null) {
            return;
        }
        final List<MenuItem> items = instance.MainMenus;

        if (items == null || items.size() == 0) {
            return;
        }
        for (MenuItem item : items) {
            if (TextUtils.isEmpty(item.Name)) {
                continue;
            }
            int separatorIndex = item.Name.indexOf("?");
            if (separatorIndex == -1) {
                separatorIndex = item.Name.indexOf("-");
            }
            // No ModuleParam configurated
            if (separatorIndex == -1) {
                item.ModuleParam = "";
            } else {
                // Extract the ModuleName and ModuleParam
                item.ModuleParam = item.Name.substring(separatorIndex + 1);
                item.Name = item.Name.substring(0, separatorIndex);
            }

        }
    }
}