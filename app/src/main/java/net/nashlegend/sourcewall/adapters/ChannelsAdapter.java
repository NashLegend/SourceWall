package net.nashlegend.sourcewall.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import net.nashlegend.sourcewall.db.AskTagHelper;
import net.nashlegend.sourcewall.db.BasketHelper;
import net.nashlegend.sourcewall.db.GroupHelper;
import net.nashlegend.sourcewall.model.Basket;
import net.nashlegend.sourcewall.model.SubItem;
import net.nashlegend.sourcewall.request.RequestObject;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.api.FavorAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.util.ChannelHelper;
import net.nashlegend.sourcewall.view.GroupItemView;
import net.nashlegend.sourcewall.view.SubItemView;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/10/31 0031
 */
public class ChannelsAdapter extends BaseExpandableListAdapter {

    public ArrayList<SubItem> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<SubItem> groupList) {
        this.groupList = groupList;
    }

    public ArrayList<ArrayList<SubItem>> getSubLists() {
        return subLists;
    }

    public void setSubLists(ArrayList<ArrayList<SubItem>> subLists) {
        this.subLists = subLists;
    }

    private ArrayList<SubItem> groupList = new ArrayList<>();
    private ArrayList<ArrayList<SubItem>> subLists = new ArrayList<>();
    private Context mContext;

    public ChannelsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return subLists.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return subLists.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupList.get(groupPosition).getName().hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (groupList.get(groupPosition).getName() + "_/_" + subLists.get(groupPosition).get(childPosition).getName()).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new GroupItemView(mContext);
        }
        ((GroupItemView) convertView).setData(groupList.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new SubItemView(mContext);
        }
        ((SubItemView) convertView).setData(subLists.get(groupPosition).get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setDefaultChannels() {
        ArrayList<SubItem> groups = ChannelHelper.getSections();
        ArrayList<ArrayList<SubItem>> cols = new ArrayList<>();
        //添加科学人的所有栏目
        cols.add(ChannelHelper.getArticles());
        //添加小组
        cols.add(getGroupSections());
        //添加问答
        cols.add(getAskSections());
        if (UserAPI.isLoggedIn()) {
            //添加收藏
            ArrayList<SubItem> basketSubItems = getBasketSections();
            if (basketSubItems.size() > 0) {
                groups.add(ChannelHelper.getBasketGroup());
                cols.add(basketSubItems);
            } else {
                loadBaskets();
            }
        }
        setGroupList(groups);
        setSubLists(cols);
        notifyDataSetChanged();
    }

    private ArrayList<SubItem> getGroupSections() {
        //重新加载小组数据库
        ArrayList<SubItem> groupSubItems = new ArrayList<>();
        if (UserAPI.isLoggedIn()) {
            groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Private_Channel, "我的小组", "user_group"));
        }
        if (GroupHelper.getMyGroupsNumber() > 0) {
            //如果已经加载了栏目
            groupSubItems.add(new SubItem(SubItem.Section_Post, SubItem.Type_Collections, "小组热贴", "hot_posts"));
            groupSubItems.addAll(GroupHelper.getSelectedGroupSubItems());
        } else {
            groupSubItems.addAll(ChannelHelper.getPosts());
        }
        return groupSubItems;
    }

    private ArrayList<SubItem> getAskSections() {
        //重新加载标签数据库
        ArrayList<SubItem> questionSubItems = new ArrayList<>();
        if (AskTagHelper.getAskTagsNumber() > 0) {
            //如果已经加载了栏目
            questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "热门问答", "hottest"));
            questionSubItems.add(new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "精彩回答", "highlight"));
            questionSubItems.addAll(AskTagHelper.getSelectedQuestionSubItems());
        } else {
            questionSubItems.addAll(ChannelHelper.getQuestions());
        }
        return questionSubItems;
    }

    private ArrayList<SubItem> getBasketSections() {
        //重新加载标签数据库
        ArrayList<SubItem> basketSubItems = new ArrayList<>();
        if (BasketHelper.getBasketsNumber() > 0) {
            //如果已经加载了栏目
            basketSubItems.addAll(BasketHelper.getAllMyBasketsSubItems());
        }
        return basketSubItems;
    }

    private void loadBaskets() {
        FavorAPI.getBaskets(new RequestObject.CallBack<ArrayList<Basket>>() {
            @Override
            public void onFailure(@Nullable Throwable e, @NonNull ResponseObject<ArrayList<Basket>> result) {

            }

            @Override
            public void onSuccess(@NonNull ArrayList<Basket> result, @NonNull ResponseObject<ArrayList<Basket>> detailed) {
                onGetBaskets(result);
            }
        });
    }

    synchronized private void onGetBaskets(ArrayList<Basket> baskets) {
        BasketHelper.putAllBaskets(baskets);
        if (groupList.size() != 4) {
            if (baskets.size() > 0) {
                ArrayList<SubItem> basketSubItems = new ArrayList<>();
                for (int i = 0; i < baskets.size(); i++) {
                    Basket basket = baskets.get(i);
                    SubItem basketSubItem = new SubItem(SubItem.Section_Favor, SubItem.Type_Single_Channel, basket.getName(), basket.getId());
                    basketSubItems.add(basketSubItem);
                }
                groupList.add(ChannelHelper.getBasketGroup());
                subLists.add(basketSubItems);
            }
        }
    }
}
