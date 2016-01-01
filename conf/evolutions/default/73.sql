# --- !Ups

CREATE TABLE agentschool (
  uid         INT(11) NOT NULL AUTO_INCREMENT,
  agent_id   INT(11),
  school_id   VARCHAR(20) NOT NULL,
  name   VARCHAR(50) NOT NULL,
  address   TEXT,
  created_at   BIGINT,
  updated_at BIGINT,
  status INT(4) DEFAULT 1,
  PRIMARY KEY (uid),
  KEY (agent_id),
  KEY (agent_id, school_id)
);

INSERT INTO agentschool (agent_id, school_id, address, name, updated_at, created_at) VALUES
(1, '93740362', '黑龙江 哈尔滨', '孟菲斯大学', 1393395313123, 1393395313123),
(1, '93740562', '四川成都', '常春藤', 1393399313123, 1393399313123);

# --- !Downs

DROP TABLE IF EXISTS agentschool;