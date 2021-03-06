# --- !Ups
-- --------------------------------------------------------
--
--
-- 表的结构 im_token
--

CREATE TABLE im_token (
  uid int(11) NOT NULL AUTO_INCREMENT,
  school_id VARCHAR(20),
  user_id VARCHAR(100),
  token VARCHAR(270),
  origin_id INT(11),
  origin_type CHAR(1),
  updated_at bigint(20) NOT NULL DEFAULT 0,
  created_at bigint(20) NOT NULL DEFAULT 0,
  status INT(4) DEFAULT 1,
  PRIMARY KEY (uid),
  KEY(school_id),
  KEY(school_id, user_id)
);

insert into im_token (school_id, user_id, token, origin_id, origin_type) VALUES
 ('93740362', 'p_93740362_Some(1)_13402815317', '8888', 1, 'p'),
 ('93740362', 't_93740362_Some(1)_e0001', '9999', 1, 't'),
 ('93740362', 'p_93740362_Some(5)_13333333333', '1024', 5, 'p');
# --- !Downs

DROP TABLE IF EXISTS im_token;