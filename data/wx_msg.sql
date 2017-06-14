/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : db_wxbot

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2017-06-14 18:03:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for wx_msg
-- ----------------------------
DROP TABLE IF EXISTS `wx_msg`;
CREATE TABLE `wx_msg` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createTime` varchar(16) DEFAULT NULL,
  `msgType` int(8) DEFAULT NULL,
  `groupMsg` varchar(10) DEFAULT NULL,
  `nickName` varchar(128) DEFAULT NULL,
  `remarkName` varchar(128) DEFAULT NULL,
  `msgId` varchar(128) DEFAULT NULL,
  `newMsgId` varchar(128) DEFAULT NULL,
  `text` text,
  `content` text,
  `fileUrl` varchar(512) DEFAULT NULL,
  `fromUserName` varchar(128) DEFAULT NULL,
  `toUserName` varchar(128) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `originalMsg` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1727 DEFAULT CHARSET=utf8;
