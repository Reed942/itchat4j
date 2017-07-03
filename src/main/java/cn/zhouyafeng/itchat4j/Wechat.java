package cn.zhouyafeng.itchat4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhouyafeng.itchat4j.controller.LoginController;
import cn.zhouyafeng.itchat4j.core.Core;
import cn.zhouyafeng.itchat4j.core.MsgCenter;
import cn.zhouyafeng.itchat4j.face.IMsgHandlerFace;

public class Wechat {
	private static final Logger LOG = LoggerFactory.getLogger(Wechat.class);
	private IMsgHandlerFace msgHandler;
	private Core core = Core.getInstance();
	// 登陆
	LoginController login = new LoginController();
	
	public Wechat(IMsgHandlerFace msgHandler) {
		System.setProperty("jsse.enableSNIExtension", "false"); // 防止SSL错误
		this.msgHandler = msgHandler;

	}
	
	public void login(String qrPath) {
		login.login(qrPath);
	}
	
	public void createQR(String qrPath) {
		login.createQR(qrPath);
	}

	public boolean weLogin() {
		return login.weLogin();
	}
	
	public boolean weLoginOneTime() {
		return login.weLoginOneTime();
	}
	
	public void start() {
		LOG.info("+++++++++++++++++++开始消息处理+++++++++++++++++++++");
		new Thread(new Runnable() {
			@Override
			public void run() {
				MsgCenter.handleMsg(msgHandler);
			}
		}).start();
	}

}
