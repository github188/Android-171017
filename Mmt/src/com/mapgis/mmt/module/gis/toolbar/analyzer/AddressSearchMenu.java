package com.mapgis.mmt.module.gis.toolbar.analyzer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.SearchHistoryFragment;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.GisAddressSearchResultCallback;
import com.zondy.mapgis.geometry.Dot;

/**
 * 地名搜索
 */
public class AddressSearchMenu extends BaseMapMenu {

    /**
     * 百度查询 或 本地db文件查询，当前查询的第几页
     */
    private int page = 0;

    // 查询历史界面
    private final SearchHistoryFragment historyFragment;

    public AddressSearchMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);

        historyFragment = SearchHistoryFragment.getInstance();
        FragmentManager fm = mapGISFrame.getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.otherFragment, historyFragment).hide(historyFragment).commit();
    }

    @Override
    public View initTitleView() {
        final View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.header_bar_search, null);

        view.findViewById(R.id.edittext_seach_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.findViewById(R.id.btn_result).getVisibility() == View.VISIBLE) {
                    mapView.getGraphicLayer().removeAllGraphics();
                    mapView.getAnnotationLayer().removeAllAnnotations();
                    mapView.refresh();
                    mapGISFrame.resetMenuFunction();
                } else {
                    InputMethodManager imm = (InputMethodManager) mapGISFrame.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    showMapFragment();
                    showResultListButton(view);
                }
            }
        });

        (view.findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String addr = ((EditText) view.findViewById(R.id.edittext_seach_str)).getText().toString().trim();
                    if (addr.length() == 0) {
                        Toast.makeText(mapGISFrame, "请填写位置信息", Toast.LENGTH_SHORT).show();

                    } else if (addr.contains(",") && BaseClassUtil.isNum(addr.split(",")[0])
                            && BaseClassUtil.isNum(addr.split(",")[1])) {

                        String[] positionStr = addr.split(",");
                        double x = Double.valueOf(positionStr[0]);
                        double y = Double.valueOf(positionStr[1]);

                        Dot dot = new Dot(x, y);

                        GisAddressSearchResultCallback callback = new GisAddressSearchResultCallback(dot);
                        MyApplication.getInstance().sendToBaseMapHandle(callback);

                        Toast.makeText(mapGISFrame, x + "," + y, Toast.LENGTH_SHORT).show();

                    } else {
                        new AddressSearchTask(mapView, mapGISFrame, addr, page).executeOnExecutor(MyApplication.executorService);

                        // 隐藏软键盘
                        InputMethodManager imm = (InputMethodManager) mapGISFrame.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        showResultListButton(view);
                        showMapFragment();
                    }
                } catch (Exception e) {
                    Toast.makeText(mapGISFrame, "填写信息有误,请确认信息是否填写正确!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        (view.findViewById(R.id.btn_result)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mapGISFrame, AddressSearchResultActivity.class);
                intent.putExtra("where", ((EditText) view.findViewById(R.id.edittext_seach_str)).getText().toString().trim());
                intent.putExtra("page", page);
                mapGISFrame.startActivityForResult(intent, 0);
                MyApplication.getInstance().startActivityAnimation(mapGISFrame);
            }
        });

        // 输入框触摸事件
        view.findViewById(R.id.edittext_seach_str).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    showHistoryListFragment();

                    showSearchButton(view);

                    historyFragment.parserAdapter(((EditText) v).getText().toString().trim());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });

        ((EditText) view.findViewById(R.id.edittext_seach_str)).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                historyFragment.parserAdapter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {
        return true;
    }

    /**
     * 显示列表按钮
     */
    private void showResultListButton(View view) {
        view.findViewById(R.id.btn_search).setVisibility(View.GONE);
        view.findViewById(R.id.btn_result).setVisibility(View.VISIBLE);
    }

    /**
     * 显示查询按钮
     */
    private void showSearchButton(View view) {
        view.findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btn_result).setVisibility(View.GONE);
    }

    /**
     * 显示历史记录
     */
    private void showHistoryListFragment() {
        FragmentManager fm = mapGISFrame.getSupportFragmentManager();
        fm.beginTransaction().hide(mapGISFrame.getFragment()).commit();
        fm.beginTransaction().show(historyFragment).commit();
    }

    /**
     * 显示地图
     */
    private void showMapFragment() {
        FragmentManager fm = mapGISFrame.getSupportFragmentManager();
        fm.beginTransaction().hide(historyFragment).commit();
        fm.beginTransaction().show(mapGISFrame.getFragment()).commit();
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        if (intent != null) {
            page = intent.getIntExtra("page", 0);
        }
        return false;
    }
}
