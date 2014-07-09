# --- !Ups

CREATE TABLE accountinfo (
  uid int(11) NOT NULL AUTO_INCREMENT,
  accountid      varchar(16) NOT NULL,
  password varchar(32) NOT NULL,
  pushid   varchar(20) NOT NULL DEFAULT '',
  active  INT NOT NULL DEFAULT 0,
  pwd_change_time bigint(20) NOT NULL DEFAULT 0,
  device INT NOT NULL DEFAULT 3,
  PRIMARY KEY (uid)
);

INSERT INTO accountinfo (uid, accountid, password, pushid, active, pwd_change_time, device) VALUES
(1, '13402815317', '3FDE6BB0541387E4EBDADF7C2FF31123', '952021056346783736', 1, 1386425935574, 3),
(2, '18980030903', '3FDE6BB0541387E4EBDADF7C2FF31123', '963386802751977894', 1, 1386425935574, 3),
(3, '13408654680', '96E79218965EB72C92A549DD5A330112', '963386802751977894', 1, 1386849160798, 3),
(13, '14880498549', '96E79218965EB72C92A549DD5A330112', '963386802751977894', 1, 1386849160798, 3),
(4, '13333333333', '5F4DCC3B5AA765D61D8327DEB882CF99', '', 0, 0, 4),
(5, '13279491366', '5F4DCC3B5AA765D61D8327DEB882CF99', '1048149352597220995', 1, 1386849160798, 3),
(6, '13227882591', '991F52446AB4B4DC28BF4256B45F0CA8', '757889657634442744', 1, 1386849160798, 4),
(7, '13408654683', '991F52446AB4B4DC28BF4256B45F0CA8', '', 0, 0, 3),
(8, '22222222222', '991F52446AB4B4DC28BF4256B45F0CA8', '123', 0, 2, 3),
(118, '22222222223', '991F52446AB4B4DC28BF4256B45F0CA8', '123', 1, 5, 3),
(109, '13402815318', '991F52446AB4B4DC28BF4256B45F0CA8', '123', 1, 5, 3),
(119, '22222222224', '991F52446AB4B4DC28BF4256B45F0CA8', '123', 1, 5, 3),
(120, '22222222225', '991F52446AB4B4DC28BF4256B45F0CA8', '123', 1, 5, 3);


# --- !Downs

DROP TABLE IF EXISTS accountinfo;