package cn.zhouyafeng.itchat4j.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class MsgModel {

	private String createTime;
	private int msgType;
	private String groupMsg;
	private String msgId;
	private String newMsgId;
	private String nickName;
	private String remarkName;
	private String text;
	private String content;
	private String fromUserName;
	private String toUserName;
	private String status;
	private String type;
	
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getRemarkName() {
		return remarkName;
	}
	public void setRemarkName(String remarkName) {
		this.remarkName = remarkName;
	}
	public String getGroupMsg() {
		return groupMsg;
	}
	public void setGroupMsg(String groupMsg) {
		this.groupMsg = groupMsg;
	}
	
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getNewMsgId() {
		return newMsgId;
	}
	public void setNewMsgId(String newMsgId) {
		this.newMsgId = newMsgId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public List<MsgModel> selectWxMsg(Connection conn,String sql) throws Exception {
		List<MsgModel> list=new ArrayList<MsgModel>();
		PreparedStatement psmt=conn.prepareStatement(sql);
		ResultSet rs=psmt.executeQuery();
		while(rs.next()){
			MsgModel msgModel = new MsgModel();
			msgModel.setText(rs.getString("text"));
			
			list.add(msgModel);
		}
		psmt.close();
		DbUtil.closeRs(rs);
		
		return list;
	}
	
	public void Insert(Connection conn) {
		String sql="insert into wx_msg(createTime,msgType,groupMsg,nickName,remarkName,msgId,newMsgId,text,content,fromUserName,toUserName,status,type) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement psmt = null;
		try{
			psmt=conn.prepareStatement(sql);
			psmt.setString(1, this.getCreateTime());
			psmt.setInt(2, this.getMsgType());
			psmt.setString(3, this.getGroupMsg());
			psmt.setString(4, this.getNickName());
			psmt.setString(5, this.getRemarkName());
			psmt.setString(6, this.getMsgId());
			psmt.setString(7, this.getNewMsgId());
			psmt.setString(8, this.getText());
			psmt.setString(9, this.getContent());
			psmt.setString(10, this.getFromUserName());
			psmt.setString(11, this.getToUserName());
			psmt.setString(12, this.getStatus());
			psmt.setString(13, this.getType());
			psmt.executeUpdate();
			psmt.close();
			
			conn.commit();
		}catch (Exception e) {
			try {
				if(conn!=null)
				{
					
				      conn.rollback(); //SQL语句出现异常 事务回滚（取消在当前事务中进行的所有更改）
				     // conn.setAutoCommit( true);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}
}
