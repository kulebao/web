# --- !Ups
-- --------------------------------------------------------

--
-- 表的结构 dailylog
--

CREATE TABLE dailylog (
  uid         INT(11)          NOT NULL AUTO_INCREMENT,
  school_id   VARCHAR(20) NOT NULL,
  child_id varchar(40) NOT NULL,
  parent_name VARCHAR(20) NOT NULL,
  record_url TEXT DEFAULT '',
  card_no varchar(20) NOT NULL,
  card_type INT DEFAULT 0,
  notice_type INT DEFAULT 0,
  check_at   BIGINT(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (uid)
);

insert into dailylog (school_id, child_id, parent_name, card_no, check_at) values
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405094400000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405180800000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405267200000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405353600000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405440000000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405526400000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405612800000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405699200000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405785600000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405866500000),
('93740362', '1_1391836223533', '袋鼠', '0001234567', 1405981000000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405094400000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405180800000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405267200000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405353600000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405440000000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405526400000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405612800000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405699200000),
('93740362', '1_93740362_456', '林玄', '0001234568', 1405781000000),
('93740362', '1_93740362_9982', '大象', '2323211233', 1405071300000),
('93740362', '1_93740362_9982', '大象', '2323211233', 1405184900000),
('93740362', '1_93740362_9982', '大象', '2323211233', 1405298500000),
('93740362', '1_93740362_9982', '大象', '2323211233', 1405312100000),
('93740362', '1_93740362_9982', '大象', '2323211233', 1405425700000),

('93740362', '1_93740362_778', '大象', '0001234570', 1405094400000),
('93740362', '1_93740362_778', '大象', '0001234570', 1405180800000),
('93740362', '1_93740362_778', '大象', '0001234570', 1405267200000),
('93740362', '1_93740362_778', '大象', '0001234570', 1405353600000),
('93740362', '1_93740362_778', '大象', '0001234570', 1405440000000);

# --- !Downs

DROP TABLE IF EXISTS dailylog;