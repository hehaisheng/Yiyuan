package com.shawnway.nav.app.yylg.fragment;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.shawnway.nav.app.yylg.adapter.ShareItemAdapter;
import com.shawnway.nav.app.yylg.bean.ShareListWrapper;
import com.shawnway.nav.app.yylg.net.VolleyTool;
import com.shawnway.nav.app.yylg.tool.Constants;
import com.shawnway.nav.app.yylg.tool.ToastUtil;
import com.shawnway.nav.app.yylg.tool.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eiffel on 2015/11/7.
 */
public class ShareContentFragment extends ListFragment {

    private static final String TAG = "ShareContentFragment";

    /*
   *需要重载getAdapter以实现在onLoadMore中添加数据
   * 返回类型需要强转
   */
    @Override
    protected ShareItemAdapter getAdapter() {
        if (mAdapter == null)
            mAdapter = new ShareItemAdapter(getActivity());
        return (ShareItemAdapter) mAdapter;
    }

    @Override
    public void onLoadMore() {
        Map<String, String> param = new HashMap<>();
        param.put("page", mNextPage + "");
        param.put("pageSize", Constants.DEF_PAGE_SIZE + "");
        VolleyTool.getInstance(getContext()).sendGsonRequest(this,ShareListWrapper.class, Constants.ALL_SHARE_URL, param, new Response.Listener<ShareListWrapper>() {
            @Override
            public void onResponse(ShareListWrapper response) {

                if (!Utils.handleResponseError(getActivity(), response)) {
                    Log.d(TAG, "new list size is:" + response.getBody().prizeShowItems.size());
                    ShareListWrapper.ShareListBody body = response.getBody();
                    //检测是否所需页
                    if (mNextPage != body.page)
                        return;
                    //先处理错误，无错误则进行成功操作
                    if (!Utils.noListData(body.page, body.totalPage, body.prizeShowItems)) {
                        int beflength = mAdapter.getListSize();
                        Log.d(TAG, "new list size is:" + body.prizeShowItems.size());
                        //需要通过getAdapter来添加数据，否则需要自行强转
                        getAdapter().addAll(response.getBody().prizeShowItems);
                        getAdapter().notifyItemRangeInserted(beflength + 1, mAdapter.getListSize() - beflength);
                        mNextPage = response.getBody().page + 1;
                    }

                }
                //数据加载完毕
                onRefreshComplete();
                onLoadmoreComplete();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.showNetError(getContext());
                //数据加载完毕
                onRefreshComplete();
                onLoadmoreComplete();
            }
        });
    }


}
