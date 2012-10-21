package com.surfing.rssparse;

/**
 * 
 * @author john
 * ��Ϸ�ű��Ľ���
 */
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.content.Context;
import android.util.Log;

public class DomXMLReader
{
    public static final String TAG = "DOMXMLREADER";

    public static ChannelInformation readXML(Context context, InputStream instream)
    {
        ChannelInformation channleinfo = null;
        ChannelItem channelitem = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(instream);
            // Document dom =
            // builder.parse(context.getResources().getAssets().open("test.xml"));
            Element root = dom.getDocumentElement();
            if (root == null)
            {
                return null;
            }
            //
            NodeList propertys = root.getElementsByTagName("title");
            Element property = (Element) propertys.item(0);

            //
            NodeList channellist = root.getElementsByTagName("item");
            channleinfo = new ChannelInformation();
            for (int i = 0; i < channellist.getLength(); i++)
            {
                channelitem = new ChannelItem();

                Element descNode = (Element) channellist.item(i);
                NodeList Items = descNode.getChildNodes();
                for (int j = 0; j < Items.getLength(); j++)
                {
                    if (Items.item(j).getNodeName().equalsIgnoreCase("title"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            Log.e(TAG, "the title is " + Items.item(j).getFirstChild().getNodeValue());
                            channelitem.setmTitle(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("link"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            channelitem.setmLink(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("description"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            channelitem.setmDescription(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("icon"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            channelitem.setmIcon(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("pubDate"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            channelitem.setmDate(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                }

                channleinfo.addChannelItem(channelitem);
            }
            instream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return channleinfo;
    }

    public static VoteItem readXMLVote(Context context, InputStream instream)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        VoteItem voteItem = null;
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(instream);
            // Document dom =
            // builder.parse(context.getResources().getAssets().open("test.xml"));
            Element root = dom.getDocumentElement();
            NodeList item = root.getElementsByTagName("item");
            for (int j = 0; j < item.getLength(); j++)
            {
                voteItem = new VoteItem();
                Element property = (Element) item.item(j);
                NodeList vote = property.getElementsByTagName("vote");

                Element ItemDesc = (Element) vote.item(0);

                voteItem.setmId(new String(ItemDesc.getAttribute("id")));
                voteItem.setmTitle(new String(ItemDesc.getAttribute("title")));
                voteItem.setmSummary(new String(ItemDesc.getAttribute("summary")));
                voteItem.setmType(new String(ItemDesc.getAttribute("type")));
                voteItem.setmIsVoted(new Boolean(ItemDesc.getAttribute("isvoted")));
                voteItem.setmIconLink(new String(ItemDesc.getAttribute("icon")));
                NodeList optionlist = property.getElementsByTagName("option");
                VoteOptions voteoption = null;
                for (int i = 0; i < optionlist.getLength(); i++)
                {
                    voteoption = new VoteOptions();
                    Element descNode = (Element) optionlist.item(i);
                    voteoption.setId(new String(descNode.getAttribute("id")));
                    voteoption.setDesc(new String(descNode.getAttribute("title")));
                    voteoption.setCount(new Integer(descNode.getAttribute("count")));
                    Log.i(TAG, "the count is == " + voteoption.getCount());
                    voteItem.addVoteOption(voteoption);
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return voteItem;
    }

    public static WeatherInfo readXMLWeather(Context context, InputStream instream)
    {
        Log.i(TAG, "readXMLWeather");
        WeatherInfo weatherInfo = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(instream);
            // Document dom =
            // builder.parse(context.getResources().getAssets().open("test.xml"));
            Element root = dom.getDocumentElement();
            Element descNode = null;
            NodeList Items = null;
            weatherInfo = new WeatherInfo();
            // current
            NodeList currentList = root.getElementsByTagName("current_conditions");
            Log.i(TAG, "currentList.getLength()==" + currentList.getLength());
            if (currentList.getLength() > 0)
            {
                WeatherCurrent weatherCurrent = new WeatherCurrent();
                descNode = (Element) currentList.item(0);
                Items = descNode.getChildNodes();
                for (int i = 0; i < Items.getLength(); i++)
                {
                    Element ItemDesc = (Element) Items.item(i);
                    if (ItemDesc.getNodeName().equalsIgnoreCase("condition"))
                    {
                        Log.i(TAG, "the condition is : " + new String(ItemDesc.getAttribute("data")));
                        weatherCurrent.setmCondition(new String(ItemDesc.getAttribute("data")));
                    }
                    else if (ItemDesc.getNodeName().equalsIgnoreCase("temp_f"))
                    {
                        weatherCurrent.setmTemp_f(new Integer(ItemDesc.getAttribute("data")));
                    }
                    else if (ItemDesc.getNodeName().equalsIgnoreCase("temp_c"))
                    {
                        weatherCurrent.setmTemp_c(new Integer(ItemDesc.getAttribute("data")));
                    }
                    else if (ItemDesc.getNodeName().equalsIgnoreCase("icon"))
                    {
                        weatherCurrent.setmIconUrl(new String(ItemDesc.getAttribute("data")));

                    }
                    else if (ItemDesc.getNodeName().equalsIgnoreCase("wind_condition"))
                    {
                        if (Items.item(i).getFirstChild() != null)
                        {
                            weatherCurrent.setmWind_condition(new String(ItemDesc.getAttribute("data")));
                        }
                    }
                }
                weatherInfo.setmWeatherCurrent(weatherCurrent);
            }

            // day weather
            NodeList dayList = root.getElementsByTagName("forecast_conditions");
            Log.i(TAG, "dayList.getLength()==" + dayList.getLength());
            WeatherDayInfo dayInfo = null;
            for (int j = 0; j < dayList.getLength(); j++)
            {
                dayInfo = new WeatherDayInfo();
                descNode = (Element) dayList.item(j);
                Items = descNode.getChildNodes();

                for (int i = 0; i < Items.getLength(); i++)
                {
                    Element ItemDesc = (Element) Items.item(i);
                    if (Items.item(i).getNodeName().equalsIgnoreCase("day_of_week"))
                    {
                        dayInfo.setmWeek(new String(ItemDesc.getAttribute("data")));

                    }
                    else if (Items.item(i).getNodeName().equalsIgnoreCase("low"))
                    {
                        dayInfo.setmTemp_low(Integer.parseInt(ItemDesc.getAttribute("data")));
                    }
                    else if (Items.item(i).getNodeName().equalsIgnoreCase("high"))
                    {
                        dayInfo.setmTemp_High(Integer.parseInt(ItemDesc.getAttribute("data")));
                    }
                    else if (Items.item(i).getNodeName().equalsIgnoreCase("icon"))
                    {
                        dayInfo.setmIconUrl(new String(ItemDesc.getAttribute("data")));
                    }
                    else if (Items.item(i).getNodeName().equalsIgnoreCase("condition"))
                    {
                        dayInfo.setmCondition(new String(ItemDesc.getAttribute("data")));
                    }
                }
                weatherInfo.addmWeatherDayList(dayInfo);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return weatherInfo;
    }

    public static ArrayList<ContactsInfo> readXMLContact(Context context, InputStream instream)
    {
        ArrayList<ContactsInfo> ContactsList = new ArrayList<ContactsInfo>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        ContactsInfo contactinfo = null;
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(instream);
            // Document dom =
            // builder.parse(context.getResources().getAssets().open("test.xml"));
            Element root = dom.getDocumentElement();

            //
            NodeList propertys = root.getElementsByTagName("title");
            Element property = (Element) propertys.item(0);

            //
            NodeList contactslist = root.getElementsByTagName("item");

            for (int i = 0; i < contactslist.getLength(); i++)
            {
                contactinfo = new ContactsInfo();
                Element descNode = (Element) contactslist.item(i);
                NodeList Items = descNode.getChildNodes();
                for (int j = 0; j < Items.getLength(); j++)
                {
                    if (Items.item(j).getNodeName().equalsIgnoreCase("tel_name"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            Log.e(TAG, "the tel_name is " + Items.item(j).getFirstChild().getNodeValue());
                            contactinfo.setmName(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("depart_name"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            contactinfo.setmDepartMentName(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("tel_1"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            contactinfo.setmPhone1(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("tel_2"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            contactinfo.setmPhone2(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("tel_3"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            contactinfo.setmPhone3(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("user_position"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            contactinfo.setmPosition(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    else if (Items.item(j).getNodeName().equalsIgnoreCase("user_work_position"))
                    {
                        if (Items.item(j).getFirstChild() != null)
                        {
                            contactinfo.setmTitle(Items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                }

                ContactsList.add(contactinfo);
            }
            instream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return ContactsList;
    }
}
