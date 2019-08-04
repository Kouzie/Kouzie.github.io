CREATE TABLE tbl_board (
  bno NUMBER NOT NULL PRIMARY KEY, 
  title VARCHAR2(200 BYTE) NOT NULL, 
  content VARCHAR2(2000 BYTE), 
  writer VARCHAR2(50 BYTE) NOT NULL, 
  regdate DATE DEFAULT SYSDATE, 
  viewcnt NUMBER DEFAULT 0
);

CREATE TABLE tbl_reply (	
  rno NUMBER NOT NULL PRIMARY KEY, 
  bno NUMBER DEFAULT 0 NOT NULL, 
  replytext VARCHAR2(1000 BYTE) NOT NULL, 
  replyer VARCHAR2(50 BYTE) NOT NULL, 
  regdate DATE DEFAULT sysdate NOT NULL, 
  updatedate DATE DEFAULT sysdate NOT NULL, 
  PRIMARY KEY (RNO)
);

CREATE TABLE tbl_user (
    "uid" VARCHAR2(50) PRIMARY KEY,
    upw VARCHAR2(50) NOT NULL,
    uname VARCHAR2(100) NOT NULL,
    upoint NUMBER DEFAULT 0 NOT NULL
);

CREATE TABLE tbl_message (
    mno NUMBER NOT NULL PRIMARY KEY,
    targetid VARCHAR2(50) NOT NULL,
    sender VARCHAR2(50) NOT NULL,
    message VARCHAR2(4000) NOT NULL,
    opendate DATE,
    senddate DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_usertarget FOREIGN KEY(targetid) REFERENCES tbl_user("uid"),
    CONSTRAINT fk_usersender FOREIGN KEY(sender) REFERENCES tbl_user("uid")
);

CREATE SEQUENCE seq_board;
CREATE SEQUENCE seq_reply;
CREATE SEQUENCE seq_message;

INSERT INTO tbl_user("uid", upw, uname) VALUES ('user00', 'user00', 'IRON MAN');
INSERT INTO tbl_user("uid", upw, uname) VALUES ('user01', 'user01', 'CAPTAIN');
INSERT INTO tbl_user("uid", upw, uname) VALUES ('user02', 'user02', 'HULK');
INSERT INTO tbl_user("uid", upw, uname) VALUES ('user03', 'user03', 'THOR');
commit;

SELECT * FROM tbl_user;