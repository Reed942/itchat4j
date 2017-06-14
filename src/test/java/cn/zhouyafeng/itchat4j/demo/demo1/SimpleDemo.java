package cn.zhouyafeng.itchat4j.demo.demo1;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.zhouyafeng.itchat4j.Wechat;
import cn.zhouyafeng.itchat4j.api.MessageTools;
import cn.zhouyafeng.itchat4j.api.WechatTools;
import cn.zhouyafeng.itchat4j.core.Core;
import cn.zhouyafeng.itchat4j.face.IMsgHandlerFace;
import cn.zhouyafeng.itchat4j.utils.DbUtil;
import cn.zhouyafeng.itchat4j.utils.MsgModel;
import cn.zhouyafeng.itchat4j.utils.MyHttpClient;
import cn.zhouyafeng.itchat4j.utils.enums.MsgTypeEnum;
import cn.zhouyafeng.itchat4j.utils.tools.DownloadTools;

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
	MyHttpClient myHttpClient = Core.getInstance().getMyHttpClient();
	String apiKey = "5593f526f892ccbbcf0d7f4b1aba9002"; // 这里是我申请的图灵机器人API接口，每天只能5000次调用，建议自己去申请一个，免费的:)

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
			msgModel.setFileUrl(m.getString("FileUrl"));
			msgModel.setText(m.getString("Text"));
			msgModel.setMsgType(m.getInteger("MsgType"));
			msgModel.setType(m.getString("Type"));
			msgModel.setFromUserName(m.getString("FromUserName"));
			msgModel.setToUserName(m.getString("ToUserName"));
			msgModel.setMsgId(m.getString("MsgId"));
			msgModel.setNewMsgId(m.getString("NewMsgId"));
			msgModel.setGroupMsg(isGroupMsg+"");
			msgModel.setStatus(m.getInteger("Status")+"");
			msgModel.setOriginalMsg(m.toJSONString());
			
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
//		String userId = msg.getString("FromUserName");
		String text = msg.getString("Text"); // 发送文本消息，也可调用MessageTools.sendFileMsgByUserId(userId,text);
		String sourceText = "";
		String result = null;
		if (!msg.getBoolean("groupMsg")) { // 群消息不处理
			sourceText = text;
		} else {
			if ("@@62e752e655f6fc76461d2fe188a5e698fff3976f2a8300ec51e5eb0d1a7adf39".equals(msg.getString("FromUserName")) || "@@62e752e655f6fc76461d2fe188a5e698fff3976f2a8300ec51e5eb0d1a7adf39".equals(msg.getString("ToUserName"))) {
				//阳光正好群
				sourceText = text;
			} else {
				if (text.contains("@范羊")) {
					sourceText = text.replaceAll("@范羊", "");
				} else if (text.contains("@范羊-技术好")) {
					sourceText = text.replaceAll("@范羊-技术好", "");
				}
				if (text.contains("@郑明青－技术部")) {
					MessageTools.sendMsgByNickName("群消息提醒：\""+text.replaceAll("@郑明青－技术部", "")+"\"", "cosmo");
					MessageTools.sendMsgById("已帮您微信通知@郑明青", msg.getString("FromUserName"));
				}
				if (text.contains("@所有人")) {
					sourceText = text.replaceAll("@所有人", "");
				}
				if (text.contains("机器人")) {
					sourceText = text;
				}
			}
			
		}
		if (!"".equals(sourceText)) {
			result = getByTuling(sourceText);
		}
		
		return result;
	}

	/**
	 * 获取图灵智能问答结果
	 * @param sourceText
	 * @return
	 */
	private String getByTuling(String sourceText) {
		String result = "";
		String url = "http://www.tuling123.com/openapi/api";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("key", apiKey);
		paramMap.put("info", sourceText);
		paramMap.put("userid", "123456");
		String paramStr = JSON.toJSONString(paramMap);
		try {
			HttpEntity entity = myHttpClient.doPost(url, paramStr);
			result = EntityUtils.toString(entity, "UTF-8");
			JSONObject obj = JSON.parseObject(result);
			if (obj.getString("code").equals("100000")) {
				result = obj.getString("text");
			} else {
				result = "消息处理有误";
			}
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
		return result;
	}
	
	@Override
	public String picMsgHandle(JSONObject msg) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());// 这里使用收到图片的时间作为文件名
		String picPath = "E://itchat4j/pic" + File.separator + fileName + ".jpg"; // 调用此方法来保存图片
		DownloadTools.getDownloadFn(msg, MsgTypeEnum.PIC.getType(), picPath); // 保存图片的路径
		msg.put("FileUrl", picPath);
		insertMsg(conn, msg);
		return null;
	}

	@Override
	public String voiceMsgHandle(JSONObject msg) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String voicePath = "E://itchat4j/voice" + File.separator + fileName + ".mp3";
		DownloadTools.getDownloadFn(msg, MsgTypeEnum.VOICE.getType(), voicePath);
		msg.put("FileUrl", voicePath);
		insertMsg(conn, msg);
		return null;
	}

	@Override
	public String viedoMsgHandle(JSONObject msg) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String viedoPath = "E://itchat4j/viedo" + File.separator + fileName + ".mp4";
		DownloadTools.getDownloadFn(msg, MsgTypeEnum.VIEDO.getType(), viedoPath);
		msg.put("FileUrl", viedoPath);
		insertMsg(conn, msg);
		return null;
	}

	@Override
	public String nameCardMsgHandle(JSONObject msg) {
//		return "收到名片消息";
		return null;
	}

	@Override
	public String recalledMsgHandle(JSONObject msg) {
		String result = null;
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
				String text = "";
				if ("".equals(msgM.getText()) || "null".equals(msgM.getText()) || null == msgM.getText()) {
					
				} else {
					text = "\""+msgM.getText()+"\"";
				}
				if ("".equals(replacemsg)) {
					result = "您撤回了消息：" + text;
				} else {
					result = replacemsg + "：" + text;
				}
				//调用发送文本消息接口
				MessageTools.sendMsgById(result, msg.getString("FromUserName"));
				if (msgM.getType().equals(MsgTypeEnum.TEXT.getType())) {
					
				} else if (msgM.getType().equals(MsgTypeEnum.PIC.getType())) {
					MessageTools.sendPicMsgByUserId(msg.getString("FromUserName"), msgM.getFileUrl());
				} else if (msgM.getType().equals(MsgTypeEnum.VOICE.getType())) {
					MessageTools.sendPicMsgByUserId(msg.getString("FromUserName"), msgM.getFileUrl());
				} else if (msgM.getType().equals(MsgTypeEnum.VIEDO.getType())) {
					MessageTools.sendPicMsgByUserId(msg.getString("FromUserName"), msgM.getFileUrl());
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
