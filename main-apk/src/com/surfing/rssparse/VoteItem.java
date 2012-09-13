package com.surfing.rssparse;

import java.util.ArrayList;

public class VoteItem {
	String mId="";
	String mTitle="";
	String mSummary="";
	String mType="";
	String mIconLink="";
	Boolean mIsVoted=false;
	ArrayList<VoteOptions> mVoteOptions;
	
	public VoteItem(){
		mVoteOptions = new ArrayList<VoteOptions>();
	}
	public String getmId() {
		return mId;
	}
	public void setmId(String mId) {
		this.mId = mId;
	}
	public String getmTitle() {
		return mTitle;
	}
	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	public String getmSummary() {
		return mSummary;
	}
	public void setmSummary(String mSummary) {
		this.mSummary = mSummary;
	}
	public String getmType() {
		return mType;
	}
	public void setmType(String mType) {
		this.mType = mType;
	}
	public ArrayList<VoteOptions> getmVoteOptions() {
		return mVoteOptions;
	}
	public void setmVoteOptions(ArrayList<VoteOptions> mVoteOptions) {
		this.mVoteOptions = mVoteOptions;
	}
	public void addVoteOption(VoteOptions option){
		mVoteOptions.add(option);
	}
	public Boolean getmIsVoted() {
		return mIsVoted;
	}
	public void setmIsVoted(Boolean mIsVoted) {
		this.mIsVoted = mIsVoted;
	}
    public String getmIconLink()
    {
        return mIconLink;
    }
    public void setmIconLink(String mIconLink)
    {
        this.mIconLink = mIconLink;
    }
	
}

/*
 * 
 * <vote id=id title=title summary=summary type=type>
 * 		<options>
 * 			<option id=optionid desc=desc />
 * 		</options>
 * </vote>
 */
