# --- !Ups
-- --------------------------------------------------------

--
-- 表的结构 sessionlog
--

CREATE TABLE sessionlog (
  uid         INT(11)          NOT NULL AUTO_INCREMENT,
  school_id   VARCHAR(20) NOT NULL,
  session_id varchar(40) NOT NULL,
  content   TEXT NOT NULL,
  media_url TEXT DEFAULT '',
  media_type VARCHAR(200) DEFAULT 'image',
  sender VARCHAR(40) NOT NULL DEFAULT '',
  sender_type CHAR(1) NOT NULL DEFAULT 't',
  status INT default 1,
  update_at BIGINT(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (uid)
);

INSERT INTO sessionlog (school_id, session_id, content, media_url, sender, update_at, sender_type)
VALUES
('93740362', 'h_1_93740362_9982', '2014年4月发一条成长记录', 'http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK', '2_93740362_792', 1397131200000, 'p'),
('93740362', '1_93740362_9982', '老师你好，我们家王大侠怎么样。', 'http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK', '2_93740362_792', 1401120055960, 'p'),
('93740362', '1_93740362_9982', '家长你好，你家王大侠最近没有来。', 'http://suoqin-test.u.qiniudn.com/Fget0Tx492DJofAy-ZeUg1SANJ4X', '3_93740362_1122', 1401130055961, 't'),
('93740362', '1_93740362_9982', '娃他妈，怎么回事。', '', '2_93740362_790', 1401140055960, 'p'),
('93740362', 'h_1_93740362_9982', '小孩历史测试无图片', '', '2_93740362_790', 1401150055960, 'p'),
('93740362', 'h_1_93740362_9982', '小孩历史测试一张图片', 'http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK', '2_93740362_790', 1401160055960, 'p'),
('93740362', 'h_1_93740362_9982', '小孩历史测试三张图片', 'http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK', '2_93740362_790', 1401170055960, 'p'),
('93740362', 'h_1_93740362_9982', '小孩历史测试五张图片', 'http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK', '2_93740362_790', 1401180055960, 'p'),
('93740362', 'h_1_93740362_9982', '小孩历史测试九张图片', 'http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK  http://suoqin-test.u.qiniudn.com/FgPmIcRG6BGocpV1B9QMCaaBQ9LK', '2_93740362_790', 1401190055960, 'p');


# --- !Downs

DROP TABLE IF EXISTS sessionlog;