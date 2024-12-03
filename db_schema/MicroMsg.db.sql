-- ChatLiveInfo
CREATE TABLE ChatLiveInfo (
    RoomName TEXT,
    LiveId INTEGER,
    LiveName TEXT,
    AnchorName TEXT,
    Reserved1 INTEGER,
    Reserved2 INTEGER,
    Reserved3 TEXT,
    Reserved4 TEXT,
    Reserved5 BLOB,
    UNIQUE (RoomName, LiveId)
) -- TicketInfo
CREATE TABLE TicketInfo(
    UserName TEXT PRIMARY KEY,
    Ticket TEXT,
    Reserved1 INTEGER DEFAULT 0,
    Reserved2 TEXT,
    Reserved3 INTEGER DEFAULT 0,
    Reserved4 TEXT
) -- BizInfo
CREATE TABLE BizInfo(
    UserName TEXT PRIMARY KEY,
    Type INTEGER DEFAULT 0,
    Belong TEXT,
    AcceptType INTEGER DEFAULT 0,
    Reserved1 INTEGER DEFAULT 0,
    Reserved2 TEXT,
    BrandList TEXT,
    BrandFlag INTEGER DEFAULT 0,
    BrandInfo TEXT,
    BrandIconURL TEXT,
    UpdateTime INTEGER DEFAULT 0,
    ExtInfo TEXT,
    Reserved3 INTEGER DEFAULT 0,
    Reserved4 TEXT,
    Reserved5 INTEGER DEFAULT 0,
    Reserved6 TEXT,
    Reserved7 INTEGER DEFAULT 0,
    Reserved8 TEXT,
    Reserved9 BLOB
) -- FTSChatroomTrans
CREATE TABLE FTSChatroomTrans (
    username TEXT,
    groupUsername TEXT,
    displayName TEXT,
    nickname TEXT,
    operation INTEGER,
    reserve1 INTEGER,
    reserve2 TEXT
) -- ChatroomTool
CREATE TABLE ChatroomTool (
    ChatroomUsername TEXT,
    RoomToolsBuffer BLOB,
    Reserved1 INTEGER,
    Reserved2 TEXT,
    Reserved3 INTEGER,
    Reserved4 TEXT,
    Reserved5 BLOB,
    UNIQUE (ChatroomUsername)
) -- ChatInfo
CREATE TABLE ChatInfo (
    Username TEXT,
    LastReadedSvrId INTEGER,
    LastReadedCreateTime INTEGER,
    Reserved1 INTEGER,
    Reserved2 INTEGER,
    Reserved3 TEXT,
    Reserved4 TEXT,
    Reserved5 INTEGER,
    Reserved6 TEXT,
    Reserved7 BLOB
) -- BizName2ID
CREATE TABLE BizName2ID(UsrName TEXT PRIMARY KEY) -- RevokeMsgStorage
CREATE TABLE RevokeMsgStorage (
    CreateTime INTEGER PRIMARY KEY,
    MsgSvrID INTERGER,
    RevokeSvrID INTERGER
) -- MainConfig
CREATE TABLE MainConfig(
    Key TEXT PRIMARY KEY,
    Reserved0 INT,
    Buf BLOB,
    Reserved1 INT,
    Reserved2 TEXT
) -- BizSessionNewFeeds
CREATE TABLE BizSessionNewFeeds (
    TalkerId INTEGER PRIMARY KEY,
    BizName TEXT,
    Title TEXT,
    Desc TEXT,
    Type INTEGER,
    UnreadCount INTEGER,
    UpdateTime INTEGER,
    CreateTime INTEGER,
    BizAttrVersion INTEGER,
    Reserved1 INTEGER,
    Reserved2 INTEGER,
    Reserved3 TEXT,
    Reserved4 TEXT,
    Reserved5 BLOB
) -- BizProfileV2
CREATE TABLE BizProfileV2 (
    TalkerId INTEGER PRIMARY KEY,
    UserName TEXT,
    ServiceType INTEGER,
    ArticleCount INTEGER,
    FriendSubscribedCount INTEGER,
    IsSubscribed INTEGER,
    Offset TEXT,
    IsEnd INTEGER,
    TimeStamp INTEGER,
    Reserved1 INTEGER,
    Reserved2 INTEGER,
    Reserved3 TEXT,
    Reserved4 TEXT,
    RespData BLOB,
    Reserved5 BLOB
) -- OpLog
CREATE TABLE OpLog(ID INTEGER PRIMARY KEY, CMDItemBuffer BLOB) -- Contact
CREATE TABLE Contact(
    UserName TEXT PRIMARY KEY,
    Alias TEXT,
    EncryptUserName TEXT,
    DelFlag INTEGER DEFAULT 0,
    Type INTEGER DEFAULT 0,
    VerifyFlag INTEGER DEFAULT 0,
    Reserved1 INTEGER DEFAULT 0,
    Reserved2 INTEGER DEFAULT 0,
    Reserved3 TEXT,
    Reserved4 TEXT,
    Remark TEXT,
    NickName TEXT,
    LabelIDList TEXT,
    DomainList TEXT,
    ChatRoomType int,
    PYInitial TEXT,
    QuanPin TEXT,
    RemarkPYInitial TEXT,
    RemarkQuanPin TEXT,
    BigHeadImgUrl TEXT,
    SmallHeadImgUrl TEXT,
    HeadImgMd5 TEXT,
    ChatRoomNotify INTEGER DEFAULT 0,
    Reserved5 INTEGER DEFAULT 0,
    Reserved6 TEXT,
    Reserved7 TEXT,
    ExtraBuf BLOB,
    Reserved8 INTEGER DEFAULT 0,
    Reserved9 INTEGER DEFAULT 0,
    Reserved10 TEXT,
    Reserved11 TEXT
) -- AppInfo
CREATE TABLE AppInfo(
    InfoKey TEXT PRIMARY KEY,
    AppId TEXT,
    Version INT,
    IconUrl TEXT,
    StoreUrl TEXT,
    WatermarkUrl TEXT,
    HeadImgBuf BLOB,
    Name TEXT,
    Description TEXT,
    Name4EnUS TEXT,
    Description4EnUS TEXT,
    Name4ZhTW TEXT,
    Description4ZhTW TEXT
) -- PatInfo
CREATE TABLE PatInfo (
    username TEXT UNIQUE PRIMARY KEY,
    suffix TEXT,
    reserved1 INTEGER DEFAULT 0,
    reserved2 INTEGER DEFAULT 0,
    reserved3 INTEGER DEFAULT 0,
    reserved4 INTEGER DEFAULT 0,
    reserved5 TEXT,
    reserved6 TEXT,
    reserved7 TEXT,
    reserved8 TEXT,
    reserved9 TEXT
) -- ContactHeadImgUrl
CREATE TABLE ContactHeadImgUrl(
    usrName TEXT PRIMARY KEY,
    smallHeadImgUrl TEXT,
    bigHeadImgUrl TEXT,
    headImgMd5 TEXT,
    reverse0 INT,
    reverse1 TEXT
) -- BizProfileInfo
CREATE TABLE BizProfileInfo (
    tableIndex INTEGER PRIMARY KEY,
    tableVersion INTERGER,
    tableDesc TEXT
) -- ChatRoom
CREATE TABLE ChatRoom(
    ChatRoomName TEXT PRIMARY KEY,
    UserNameList TEXT,
    DisplayNameList TEXT,
    ChatRoomFlag int Default 0,
    Owner INTEGER DEFAULT 0,
    IsShowName INTEGER DEFAULT 0,
    SelfDisplayName TEXT,
    Reserved1 INTEGER DEFAULT 0,
    Reserved2 TEXT,
    Reserved3 INTEGER DEFAULT 0,
    Reserved4 TEXT,
    Reserved5 INTEGER DEFAULT 0,
    Reserved6 TEXT,
    RoomData BLOB,
    Reserved7 INTEGER DEFAULT 0,
    Reserved8 TEXT
) -- FTSContactTrans
CREATE TABLE FTSContactTrans (username TEXT, reserve1 INTEGER, reserve2 TEXT)
 -- ChatRoomInfo
CREATE TABLE ChatRoomInfo(
    ChatRoomName TEXT PRIMARY KEY,
    Announcement TEXT,
    InfoVersion INTEGER DEFAULT 0,
    AnnouncementEditor TEXT,
    AnnouncementPublishTime INTEGER DEFAULT 0,
    ChatRoomStatus INTEGER DEFAULT 0,
    Reserved1 INTEGER DEFAULT 0,
    Reserved2 TEXT,
    Reserved3 INTEGER DEFAULT 0,
    Reserved4 TEXT,
    Reserved5 INTEGER DEFAULT 0,
    Reserved6 TEXT,
    Reserved7 INTEGER DEFAULT 0,
    Reserved8 TEXT
) -- Session
CREATE TABLE Session(
    strUsrName TEXT PRIMARY KEY,
    nOrder INT DEFAULT 0,
    nUnReadCount INTEGER DEFAULT 0,
    parentRef TEXT,
    Reserved0 INTEGER DEFAULT 0,
    Reserved1 TEXT,
    strNickName TEXT,
    nStatus INTEGER,
    nIsSend INTEGER,
    strContent TEXT,
    nMsgTypeINTEGER,
    nMsgLocalID INTEGER,
    nMsgStatus INTEGER,
    nTime INTEGER,
    editContent TEXT,
    othersAtMe INT,
    Reserved2 INTEGER DEFAULT 0,
    Reserved3 TEXT,
    Reserved4 INTEGER DEFAULT 0,
    Reserved5 TEXT,
    bytesXml BLOB
) -- Contact
CREATE TABLE Contact(
    UserName TEXT PRIMARY KEY,
    Alias TEXT,
    EncryptUserName TEXT,
    DelFlag INTEGER DEFAULT 0,
    Type INTEGER DEFAULT 0,
    VerifyFlag INTEGER DEFAULT 0,
    Reserved1 INTEGER DEFAULT 0,
    Reserved2 INTEGER DEFAULT 0,
    Reserved3 TEXT,
    Reserved4 TEXT,
    Remark TEXT,
    NickName TEXT,
    LabelIDList TEXT,
    DomainList TEXT,
    ChatRoomType int,
    PYInitial TEXT,
    QuanPin TEXT,
    RemarkPYInitial TEXT,
    RemarkQuanPin TEXT,
    BigHeadImgUrl TEXT,
    SmallHeadImgUrl TEXT,
    HeadImgMd5 TEXT,
    ChatRoomNotify INTEGER DEFAULT 0,
    Reserved5 INTEGER DEFAULT 0,
    Reserved6 TEXT,
    Reserved7 TEXT,
    ExtraBuf BLOB,
    Reserved8 INTEGER DEFAULT 0,
    Reserved9 INTEGER DEFAULT 0,
    Reserved10 TEXT,
    Reserved11 TEXT
)