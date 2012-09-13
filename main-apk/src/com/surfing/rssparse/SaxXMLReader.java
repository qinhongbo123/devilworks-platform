package com.surfing.rssparse;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class SaxXMLReader {
	private static final String TAG = "SaxXMLReader";
	private ChannelItem channelitem = null;
	private String mCurrentItem = null;
	public  ChannelInformation readXML(Context context, InputStream instream) {
		
		SAXParserFactory factory=SAXParserFactory.newInstance();
		try{
			 SAXParser parser=factory.newSAXParser();
			//获取事件源
	         XMLReader xmlReader=parser.getXMLReader();
	        //设置处理器
            XmlHandle handler=new XmlHandle();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(instream));
            instream.close();
            return handler.getChannleinfo();
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch(SAXException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	public  class XmlHandle extends DefaultHandler{
		private ChannelInformation channleinfo = null;
		private String mChannelTag = "item";
		private String mTitleTag = "title";
		private String mLinkTag = "link";
		private String mDescTag = "description";
		private String mIconTag = "icon";
		private String mDateTag = "pubDate";
		
		public XmlHandle(){
			channleinfo = new ChannelInformation();
		}
		public ChannelInformation getChannleinfo() {
			return channleinfo;
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if(mChannelTag.equals(localName)){
				channleinfo.addChannelItem(channelitem);
				channelitem = null;
			}
		}

		@Override
		public void startDocument() throws SAXException {
			
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			mCurrentItem = localName;
			if(mChannelTag.equals(localName)){
				channelitem = new ChannelItem();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(mTitleTag.equals(mCurrentItem)){
				channelitem.setmTitle(new String(ch,start,length));
			}else if(mLinkTag.equals(mCurrentItem)){
				channelitem.setmLink(new String(ch,start,length));
			}else if(mDescTag.equals(mCurrentItem)){
				channelitem.setmDescription(new String(ch,start,length));
			}else if(mIconTag.equals(mCurrentItem)){
				channelitem.setmIcon(new String(ch,start,length));
			}else if(mDateTag.equals(mCurrentItem)){
				channelitem.setmDate(new String(ch,start,length));
			}
		}
		
	}
}
