package com.mapgis.mmt.common.attach;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.docment.Document;

import java.util.ArrayList;

/**
 * Created by Comclay on 2016/12/13.
 * 附件的文件列表的适配器
 */

public class AttachAdapter extends RecyclerView.Adapter<AttachHolder> {
    private Context mContext;
    // 接收的都是相对路径
    private ArrayList<Document> mItems;
    private OnItemTouchListener mOnItemTouchListener;

    public AttachAdapter(Context context,ArrayList<Document> items) {
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public AttachHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.item_attach_file, null);
        return new AttachHolder(view);
    }

    @Override
    public void onBindViewHolder(AttachHolder holder, int position) {
        final Document document = mItems.get(position);
        document.setIconToView(mContext,holder.ivItem);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mOnItemTouchListener == null){
                    return false;
                }else{
                    return mOnItemTouchListener.onItemTouch(v,event,document);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener == null){
                    return;
                }else{
                    mOnItemClickListener.onItemClick(v,document);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener == null){
                    return false;
                }else{
                    return mOnItemLongClickListener.onItemLongClick(v,document);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public void setOnItemTouchListener(OnItemTouchListener listener){
        this.mOnItemTouchListener = listener;
    }

    /**
     * 触摸事件
     */
    public interface OnItemTouchListener{
        boolean onItemTouch(View v, MotionEvent event, Document position);
    }

    /**
     * 长按时间
     */
    public interface OnItemLongClickListener{
        boolean onItemLongClick(View v, Document position);
    }
    private OnItemLongClickListener mOnItemLongClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mOnItemLongClickListener = listener;
    }

    /**
     * 点击事件
     */
    public interface OnItemClickListener{
        void onItemClick(View v, Document position);
    }
    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }
}
