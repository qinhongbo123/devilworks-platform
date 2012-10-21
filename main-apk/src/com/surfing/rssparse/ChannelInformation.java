package com.surfing.rssparse;

import java.util.ArrayList;
import java.util.List;

public class ChannelInformation
{

    private List<ChannelItem> mChannelItemList;

    public ChannelInformation()
    {
        mChannelItemList = new ArrayList<ChannelItem>();
    }

    public void addChannelItem(ChannelItem item)
    {
        mChannelItemList.add(item);
    }

    public List<ChannelItem> getmChannelItemList()
    {
        return mChannelItemList;
    }

}
