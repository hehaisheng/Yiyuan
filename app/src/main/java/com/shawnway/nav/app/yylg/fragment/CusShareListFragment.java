package com.shawnway.nav.app.yylg.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.shawnway.nav.app.yylg.adapter.ShareItemAdapter;
import com.shawnway.nav.app.yylg.bean.ShareListWrapper;
import com.shawnway.nav.app.yylg.net.VolleyTool;
import com.shawnway.nav.app.yylg.tool.Constants;
import com.shawnway.nav.app.yylg.tool.JsonUtil;
import com.shawnway.nav.app.yylg.tool.ToastUtil;
import com.shawnway.nav.app.yylg.tool.Utils;
import com.shawnway.nav.app.yylg.tool.VolleyErrorHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eiffel on 2015/12/14.
 */
public class CusShareListFragment extends ListFragment {

    private final String TAG = "ShareListFragment";
    private Map<String, String> param;
    private Long mCustomerId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomerId = getArguments().getLong("id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onLoadMore() {
        if (param==null)
            param=new HashMap<>();
        param.put("customerId",mCustomerId+"");
        param.put("page", mNextPage + "");
        param.put("pageSize", Constants.DEF_PAGE_SIZE + "");
        VolleyTool.getInstance(getContext()).sendGsonRequest(this, ShareListWrapper.class, Constants.CUSTOMER_INFO_PRIZESHOW, param, new Response.Listener<ShareListWrapper>() {
            @Override
            public void onResponse(ShareListWrapper response) {

                if (!Utils.handleResponseError(getActivity(), response)) {
//                    Log.d(TAG, "new list size is:" + response.getBody().prizeShowItems.size());
                    ShareListWrapper.ShareListBody body = response.getBody();
                    //晒单没有添加数据，会造成空指针异常，需要在没有数据的时候给它一个页面或者不做处理，已完成TODO
                    if(response.getBody() == null){
                        //造一个页面显示没有数据的时候
                        return;
                    }
                    //检测是否所需页
                    if (mNextPage != body.page)
                        return;
                    //先处理错误，无错误则进行成功操作
                    if (!Utils.noListData(body.page, body.totalPage, body.prizeShowItems)) {
                        int beflength = mAdapter.getListSize();
                        Log.d(TAG, "new list size is:" + body.prizeShowItems.size());
                        getAdapter().addAll(response.getBody().prizeShowItems);
                        getAdapter().notifyItemRangeInserted(beflength + 1, mAdapter.getListSize() - beflength);
                        mNextPage = response.getBody().page + 1;
                    }

                }

                onRefreshComplete();
                onLoadmoreComplete();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.showShort(getContext(), VolleyErrorHelper.getMessage(volleyError, getContext()));

                onRefreshComplete();
                onLoadmoreComplete();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.ACTION_REQUEST_LOGIN && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onLoginResult");
            onRefresh();
        } else if (requestCode == Constants.ACTION_REQUEST_LOGIN) {
            getActivity().finish();
        }
    }

    @Override
    protected ShareItemAdapter getAdapter() {
        if (mAdapter == null)
            mAdapter = new ShareItemAdapter(getActivity());
        return (ShareItemAdapter) mAdapter;
    }

    public static CusShareListFragment getInstance(long userId) {
        CusShareListFragment fragment = new CusShareListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("id", userId);
        fragment.setArguments(bundle);
        return fragment;
    }
}
