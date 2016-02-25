package com.eeontheway.android.applocker.feedback;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 反馈信息管理器
 * 用于发送/查看反馈列表
 *
 * @author lishutong
 * @version v1.0
 * @Time 2016-2-8
 */
public class BombFeedBackManager implements IFeedBack {
    private Context context;
    private SendStatusListener sendListener;
    private SendStatusListener updateListener;
    private QueryTopicStatusListener queryTopicListener;
    private QueryResponseStatusListener queryResponseListener;

    private static BombFeedBackManager manager;
    private static int instanceCount;

    /**
     * 初始化反馈接口
     * @return true 成功; false 失败
     */
    @Override
    public boolean init (Context context) {
        this.context = context;

        if (manager == null) {
            manager = new BombFeedBackManager();
        }
        instanceCount++;
        return true;
    }

    /**
     * 反初始化反馈接口
     */
    @Override
    public void unInit() {
        if (instanceCount > 0) {
            if (--instanceCount == 0) {
                manager = null;
            }
        }
    }

    /**
     * 发送反馈信息
     * @param info 反馈信息
     * @return true 发送成功; false 发送失败
     */
    @Override
    public void sendFeedBack (FeedBackInfo info) {
        // 转换信息结构
        BmobFeedBackInfo bmobFeedBackInfo = new BmobFeedBackInfo(info);

        // 开始调用
        if (sendListener != null) {
            sendListener.onStart();
        }

        // 将数据保存到服务器
        bmobFeedBackInfo.save(context, new SaveListener() {
            @Override
            public void onSuccess() {
                if (sendListener != null) {
                    sendListener.onSuccess();
                }
            }

            @Override
            public void onFailure(int i, String s) {
                if (sendListener != null) {
                    sendListener.onFail(s);
                }
            }
        });
    }

    /**
     * 更新反馈信息
     * @param info 反馈信息
     */
    public void updateFeedBack (FeedBackInfo info) {
        // 转换信息结构
        BmobFeedBackInfo bmobFeedBackInfo = new BmobFeedBackInfo(info);

        // 开始调用
        if (updateListener != null) {
            updateListener.onStart();
        }

        // 将数据保存到服务器
        bmobFeedBackInfo.update(context, bmobFeedBackInfo.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                if (updateListener != null) {
                    updateListener.onSuccess();
                }
            }

            @Override
            public void onFailure(int i, String s) {
                if (updateListener != null) {
                    updateListener.onFail(s);
                }
            }
        });
    }

    /**
     * 获取比指定时间更早的最多N条纪录
     * @param count 获取的纪录条数
     * @param cmpDate 比较的时间
     */
    public void queryFeedBackOlder (int count, Date cmpDate, Date updateDate) {
        if (queryTopicListener != null) {
            queryTopicListener.onStart();
        }

        List<BmobQuery<BmobFeedBackInfo>> queryList2 = new ArrayList<>();

        // 生成组合条件
        List<BmobQuery<BmobFeedBackInfo>> queryList1 = new ArrayList<>();
        queryList1.add(new BmobQuery<BmobFeedBackInfo>().addWhereLessThan("createdAt", new BmobDate(cmpDate)));
        queryList1.add(new BmobQuery<BmobFeedBackInfo>().addWhereEqualTo("isTopic", true));
        BmobQuery<BmobFeedBackInfo> createCmp = new BmobQuery<>();
        createCmp.and(queryList1);
        queryList2.add(createCmp);

        if (updateDate != null) {
            BmobQuery<BmobFeedBackInfo> updateCmp = new BmobQuery<>();
            updateCmp.addWhereGreaterThan("updatedAt", new BmobDate(updateDate));
            queryList2.add(updateCmp);
        }

        final BmobQuery<BmobFeedBackInfo> query = new BmobQuery<>();
        query.or(queryList2);

        query.order("-createdAt");
        query.setLimit(count);
        query.findObjects(context, new FindListener<BmobFeedBackInfo>() {
            @Override
            public void onSuccess(List<BmobFeedBackInfo> list) {
                // 获取反馈信息列表
                List<FeedBackTopic> infoList = new ArrayList<>();
                for (BmobFeedBackInfo info : list) {
                    FeedBackTopic topic = new FeedBackTopic();
                    topic.setTopic(info.toFeedBackInfo());
                    infoList.add(topic);
                }

                // 调用回调接口
                if (queryTopicListener != null) {
                    queryTopicListener.onSuccess(IFeedBack.QUERY_TIME_OLDER, infoList);
                }
            }

            @Override
            public void onError(int i, String s) {
                if (queryTopicListener != null) {
                    queryTopicListener.onFail(IFeedBack.QUERY_TIME_OLDER, s);
                }
            }
        });
    }

    /**
     * 获取比指定时间更新的最多N条纪录
     * @param count 获取的纪录条数
     * @param cmpDate 比较的时间
     */
    public void queryFeedBackNewer (int count, Date cmpDate, Date updateDate) {
        if (queryTopicListener != null) {
            queryTopicListener.onStart();
        }
        List<BmobQuery<BmobFeedBackInfo>> queryList2 = new ArrayList<>();

        // 生成组合条件
        List<BmobQuery<BmobFeedBackInfo>> queryList1 = new ArrayList<>();
        queryList1.add(new BmobQuery<BmobFeedBackInfo>().addWhereGreaterThan("createdAt", new BmobDate(cmpDate)));
        queryList1.add(new BmobQuery<BmobFeedBackInfo>().addWhereEqualTo("isTopic", true));
        BmobQuery<BmobFeedBackInfo> createCmp = new BmobQuery<>();
        createCmp.and(queryList1);
        queryList2.add(createCmp);

        if (updateDate != null) {
            BmobQuery<BmobFeedBackInfo> updateCmp = new BmobQuery<>();
            updateCmp.addWhereGreaterThan("updatedAt", new BmobDate(updateDate));
            queryList2.add(updateCmp);
        }

        final BmobQuery<BmobFeedBackInfo> query = new BmobQuery<>();
        query.or(queryList2);

        query.order("-createdAt");
        query.setLimit(count);
        query.findObjects(context, new FindListener<BmobFeedBackInfo>() {
            @Override
            public void onSuccess(List<BmobFeedBackInfo> list) {
                // 获取反馈信息列表
                List<FeedBackTopic> infoList = new ArrayList<>();
                for (BmobFeedBackInfo info : list) {
                    FeedBackTopic topic = new FeedBackTopic();
                    topic.setTopic(info.toFeedBackInfo());
                    infoList.add(topic);
                }

                // 调用回调接口
                if (queryTopicListener != null) {
                    queryTopicListener.onSuccess(IFeedBack.QUERY_TIME_NEWER, infoList);
                }
            }

            @Override
            public void onError(int i, String s) {
                if (queryTopicListener != null) {
                    queryTopicListener.onFail(IFeedBack.QUERY_TIME_NEWER, s);
                }
            }
        });
    }

    /**
     * 获取指定ID的反馈
     * @param id 获取的纪录条数
     */
    public void queryFeedBackById (String id) {
        if (queryTopicListener != null) {
            queryTopicListener.onStart();
        }

        // 生成组合条件
        final BmobQuery<BmobFeedBackInfo> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId", id);
        query.order("-createdAt");
        query.setLimit(1);
        query.findObjects(context, new FindListener<BmobFeedBackInfo>() {
            @Override
            public void onSuccess(List<BmobFeedBackInfo> list) {
                // 获取反馈信息列表
                List<FeedBackTopic> infoList = new ArrayList<>();
                for (BmobFeedBackInfo info : list) {
                    FeedBackTopic topic = new FeedBackTopic();
                    topic.setTopic(info.toFeedBackInfo());
                    infoList.add(topic);
                }

                // 调用回调接口
                if (queryTopicListener != null) {
                    queryTopicListener.onSuccess(IFeedBack.QUERY_TIME_NEWER, infoList);
                }
            }

            @Override
            public void onError(int i, String s) {
                if (queryTopicListener != null) {
                    queryTopicListener.onFail(IFeedBack.QUERY_TIME_NEWER, s);
                }
            }
        });
    }

    /**
     * 获取指定主题的所有回复
     */
    public void queryAllTopicResponse (final FeedBackTopic topic) {
        // 启动回调
        if (queryResponseListener != null) {
            queryResponseListener.onStart();
        }

        final BmobQuery<BmobFeedBackInfo> query = new BmobQuery<>();
        query.addWhereEqualTo("parentId", topic.getTopic().getId());
        query.order("-createdAt");
        query.setLimit(20);
        query.findObjects(context, new FindListener<BmobFeedBackInfo>() {
            @Override
            public void onSuccess(List<BmobFeedBackInfo> list) {
                // 获取反馈信息列表
                for (BmobFeedBackInfo info : list) {
                    topic.addResponse(info.toFeedBackInfo());
                }

                // 调用回调接口
                if (queryResponseListener != null) {
                    queryResponseListener.onSuccess();
                }
            }

            @Override
            public void onError(int i, String s) {
                if (queryResponseListener != null) {
                    queryResponseListener.onFail(s);
                }
            }
        });
    }

    /**
     * 设置发送状态的监听器
     * @param listener
     */
    @Override
    public void setSendListener(SendStatusListener listener) {
        sendListener = listener;
    }

    /**
     * 设置更新状态的监听器
     * @param listener
     */
    @Override
    public void setUpdateListener(SendStatusListener listener) {
        updateListener = listener;
    }

    /**
     * 设置查询状态的监听器
     * @param listener
     */
    @Override
    public void setQueryTopicListener(QueryTopicStatusListener listener) {
        queryTopicListener = listener;
    }

    /**
     * 设置查询状态的监听器
     * @param listener
     */
    @Override
    public void setQueryResponseicListener(QueryResponseStatusListener listener) {
        queryResponseListener = listener;
    }
}
