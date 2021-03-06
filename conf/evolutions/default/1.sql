# --- !Ups

CREATE TABLE news (
  uid       INT(11)      NOT NULL AUTO_INCREMENT,
  school_id VARCHAR(20)  NOT NULL,
  class_id INT(11) NOT NULL DEFAULT 0,
  title     VARCHAR(255) NOT NULL,
  content   TEXT         NOT NULL,
  image   VARCHAR(255),
  images   TEXT,
  sms   VARCHAR(200),
  update_at BIGINT         NOT NULL DEFAULT 0,
  published INT          NOT NULL DEFAULT 0,
  publisher_id VARCHAR(40),
  feedback_required INT DEFAULT 0,
  status    INT          NOT NULL DEFAULT 1,
  PRIMARY KEY (uid)
);

INSERT INTO news (uid, school_id, title, content, update_at, published, publisher_id) VALUES
  (1, '93740362', '通知1',
   '缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知缴费通知',
   1388634400000, 1, '3_93740362_1122'),
  (2, '93740362', '通知2',
   '学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电学校停电',
   1388634400002, 1, '3_93740362_1122'),
  (3, '93740362', '通知11',
   '测试信息',
   1388634400003, 1, '3_93740362_1122'),
  (4, '93740362', '通知12',
   '测试信息',
   1388634400004, 1, '3_93740362_9972'),
  (5, '93740362', '通知13',
   '测试信息',
   1388634400005, 1, '3_93740362_9977'),
  (6, '93740362', '通知14',
   '测试信息',
   1388634400006, 1, '3_93740362_3344'),
  (7, '93740362', '通知3',
   '恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击恐怖分子袭击',
   1388634400007, 0, '3_93740362_1122'),
  (8, '93740362', '苹果班班级通知',
   '苹果班班级通知',
   1388634400007, 0, '3_93740362_3344'),
  (9, '93740362', '香蕉班班级通知',
   '香蕉班班级通知',
   1388634400007, 0, '3_93740362_3344');

update news set image='http://www.jslfgz.com.cn/UploadFiles/xxgl/2013/4/201342395834.jpg' where uid=7;
update news set feedback_required=1 where uid=6;
update news set class_id=777888, images='http://attachment.van698.com/forum/201305/07/181354wxq8xkv8tccbwc9v.jpg,http://stock.591hx.com/images/hnimg/201401/27/80/14517761501597144164.jpg' where uid=8;
update news set class_id=777999, sms='sms content for news 9' where uid=9;

# --- !Downs

DROP TABLE IF EXISTS news;