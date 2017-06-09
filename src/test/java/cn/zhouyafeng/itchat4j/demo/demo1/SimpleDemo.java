package cn.zhouyafeng.itchat4j.demo.demo1;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import cn.zhouyafeng.itchat4j.Wechat;
import cn.zhouyafeng.itchat4j.api.WechatTools;
import cn.zhouyafeng.itchat4j.face.IMsgHandlerFace;
import cn.zhouyafeng.itchat4j.utils.DbUtil;
import cn.zhouyafeng.itchat4j.utils.MsgModel;

/**
 * 简单示例程序，收到文本信息自动回复原信息，收到图片、语音、小视频后根据路径自动保存
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年4月25日 上午12:18:09
 * @version 1.0
 *
 */
public class SimpleDemo implements IMsgHandlerFace {
	Logger LOG = Logger.getLogger(SimpleDemo.class);
	static Connection conn;
	
	public static void main(String[] args) {
		String qrPath = "E://itchat4j//login"; // 保存登陆二维码图片的路径
		IMsgHandlerFace msgHandler = new SimpleDemo(); // 实现IMsgHandlerFace接口的类
		Wechat wechat = new Wechat(msgHandler, qrPath); // 【注入】
		wechat.start(); // 启动服务，会在qrPath下生成一张二维码图片，扫描即可登陆，注意，二维码图片如果超过一定时间未扫描会过期，过期时会自动更新，所以你可能需要重新打开图片
		try {
			conn = DbUtil.getConn();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void insertMsg(Connection conn, JSONObject m) {
		if (conn != null) {
			JSONObject userInfo = null;
			MsgModel msgModel = new MsgModel();
			boolean isGroupMsg = m.getBoolean("groupMsg");
			msgModel.setCreateTime(m.getLongValue("CreateTime")+"");
			msgModel.setContent(m.getString("Content"));
			msgModel.setText(m.getString("Text"));
			msgModel.setMsgType(m.getInteger("MsgType"));
			msgModel.setType(m.getString("Type"));
			msgModel.setFromUserName(m.getString("FromUserName"));
			msgModel.setToUserName(m.getString("ToUserName"));
			msgModel.setMsgId(m.getString("MsgId"));
			msgModel.setNewMsgId(m.getString("NewMsgId"));
			msgModel.setGroupMsg(isGroupMsg+"");
			msgModel.setStatus(m.getInteger("Status")+"");
			
			userInfo = WechatTools.getUserInfoByUserName(m.getString("FromUserName"));
			
			if (userInfo != null) {
				msgModel.setNickName(userInfo.getString("NickName"));
				msgModel.setRemarkName(userInfo.getString("RemarkName"));
			}
			msgModel.Insert(conn);
			
		} else {
			LOG.info("数据库未连接，无法持久化!");
		}
		
	}
	
	@Override
	public String textMsgHandle(JSONObject msg) {
		insertMsg(conn, msg);
		// String docFilePath = "D:/itchat4j/pic/1.jpg"; // 这里是需要发送的文件的路径
//		if (!msg.getBoolean("groupMsg")) { // 群消息不处理
//			// String userId = msg.getString("FromUserName");
//			// MessageTools.sendFileMsgByUserId(userId, docFilePath); // 发送文件
//			// MessageTools.sendPicMsgByUserId(userId, docFilePath);
//			String text = msg.getString("Text"); // 发送文本消息，也可调用MessageTools.sendFileMsgByUserId(userId,text);
//			LOG.info(text);
//			if (text.equals("111")) {
//				WechatTools.logout();
//			}
//			if (text.equals("222")) {
//				WechatTools.remarkNameByNickName("yaphone", "Hello");
//			}
//			return text;
//		}
		return null;
	}

	@Override
	public String picMsgHandle(JSONObject msg) {
//		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());// 这里使用收到图片的时间作为文件名
//		String picPath = "D://itchat4j/pic" + File.separator + fileName + ".jpg"; // 调用此方法来保存图片
//		DownloadTools.getDownloadFn(msg, MsgTypeEnum.PIC.getType(), picPath); // 保存图片的路径
//		return "图片保存成功";
		return null;
	}

	@Override
	public String voiceMsgHandle(JSONObject msg) {
//		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
//		String voicePath = "D://itchat4j/voice" + File.separator + fileName + ".mp3";
//		DownloadTools.getDownloadFn(msg, MsgTypeEnum.VOICE.getType(), voicePath);
//		return "声音保存成功";
		return null;
	}

	@Override
	public String viedoMsgHandle(JSONObject msg) {
//		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
//		String viedoPath = "D://itchat4j/viedo" + File.separator + fileName + ".mp4";
//		DownloadTools.getDownloadFn(msg, MsgTypeEnum.VIEDO.getType(), viedoPath);
//		return "视频保存成功";
		return null;
	}

	@Override
	public String nameCardMsgHandle(JSONObject msg) {
//		return "收到名片消息";
		return null;
	}

	@Override
	public String recalledMsgHandle(JSONObject msg) {
		MsgModel msgModel = new MsgModel();
		try {
			String ss = msg.getString("Content");
			Pattern p=Pattern.compile("&lt;msgid&gt;(\\w+)&lt;/msgid&gt;");
		    Matcher m=p.matcher(ss);
		    String msgId = "";
		    if (m.find()) {
		    	msgId = m.group(1);
		    }
		    String replacemsg = "";
		    Pattern p2=Pattern.compile("&lt;replacemsg&gt;&lt;!\\[CDATA\\[(?<text>[^\\]]*)\\]\\]&gt;&lt;/replacemsg&gt;");
		    Matcher m2=p2.matcher(ss);
		    if (m2.find()) {
		    	replacemsg = m2.group(1);
		    }
			List<MsgModel> msgs = msgModel.selectWxMsg(conn, "select * from wx_msg where msgId = '"+msgId+"'");
			if (msgs != null && msgs.size()>0) {
				MsgModel msgM = msgs.get(0);
				if ("".equals(replacemsg)) {
					return "您撤回了消息：\""+msgM.getText()+"\"";
				} else {
					return replacemsg+"：\""+msgM.getText()+"\"";
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
