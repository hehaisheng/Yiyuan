package com.shawnway.nav.app.yylg.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.shawnway.nav.app.yylg.adapter.WasteRecAdapter;
import com.shawnway.nav.app.yylg.bean.WasteRecordResponse;
import com.shawnway.nav.app.yylg.net.VolleyTool;
import com.shawnway.nav.app.yylg.tool.Constants;
import com.shawnway.nav.app.yylg.tool.ToastUtil;
import com.shawnway.nav.app.yylg.tool.Utils;
import com.shawnway.nav.app.yylg.tool.VolleyErrorHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eiffel on 2015/12/15.
 */
public class WasteDetailFragment extends ListFragmentNoPull {

    private static final String TAG = "WasteDetailFragment";

    private BroadcastReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Constants.ACTION_LOGIN);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_LOGIN)) {
                    onRefresh();
                }
            }
        };
        getActivity().registerReceiver(receiver, filter);
    }


    protected WasteRecAdapter getAdapter(){
        if (mAdapter==null)
            mAdapter=new WasteRecAdapter(getContext());
        return (WasteRecAdapter) mAdapter;
    }

    @Override
    public  void onLoadMore(){
        //FIXME MyRecyclerView的loadmore好像存在bug，当第一遍load的满屏幕的时候不会再触发onLoadmore
        //要避免这个Bug可以选择将每页的个数设置大一点
        Map<String, String> param = new HashMap<>();
        param.put("page", mNextPage + "");
        param.put("pageSize", Constants.DEF_BIG_PAGE_SIZE + "");
        param.put("balanceSimbol", "NEGATIVE");
        VolleyTool.getInstance(getContext()).sendGsonRequest(this,WasteRecordResponse.class, Constants.CHARGE_DETAIL, param, new Response.Listener<WasteRecordResponse>() {
            @Override
            public void onResponse(WasteRecordResponse response) {

                if (!Utils.handleResponseError(getActivity(), response)) {
                    WasteRecordResponse.WasteBody body = response.getBody();
                    getAdapter().setHeaderData(body);
                    //检测是否所需页
                    if (mNextPage != body.page)
                        return;
                    //先处理错误，无错误则进行成功操作
                    if (!Utils.noListData(body.page, body.totalPage, body.balanceHistoryList)) {
                        int beflength = mAdapter.getListSize();
                        Log.d(TAG, "new list size is:" + body.balanceHistoryList.size());
                        //需要通过getAdapter来添加数据，否则需要自行强转
                        getAdapter().addAll(response.getBody().balanceHistoryList);
                        getAdapter().notifyItemRangeInserted(beflength + 1, mAdapter.getListSize() - beflength);
                        mNextPage = response.getBody().page + 1;
                    }

                }
                //数据加载完毕
                onRefreshComplete();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.showShort(getContext(), VolleyErrorHelper.getMessage(volleyError, getContext()));

                //数据加载完毕
                onRefreshComplete();
            }
        });
    }

}
